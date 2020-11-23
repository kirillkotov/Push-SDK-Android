package com.push.android.pushsdkandroid

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.push.android.pushsdkandroid.core.RequestAnswerHandler
import com.push.android.pushsdkandroid.add.Info
import com.push.android.pushsdkandroid.core.RewriteParams
import com.push.android.pushsdkandroid.core.*
import com.push.android.pushsdkandroid.core.Initialization.Companion.PushKDatabase
import com.push.android.pushsdkandroid.logger.PushSDKLogger
import com.push.android.pushsdkandroid.models.*
import com.push.android.pushsdkandroid.models.PushKDataApi
import com.push.android.pushsdkandroid.models.PushKDataApi2
import kotlin.properties.Delegates

/**
 * Main class, used for initialization. Only works with API v3.0
 * @see PushKFunAnswerGeneral
 * @param context the context you would like to use
 * @param baseApiUrl base api url, like "https://example.io/api"
 * @param log_level (optional) logging level
 */
@Suppress("unused")
class PushSDK(
    context: Context,
    baseApiUrl: String,
    log_level: LogLevels = LogLevels.PUSHSDK_LOG_LEVEL_ERROR
) {

    /**
     * Main class, used for initialization. Intended to work with API v3.0
     * @see PushKFunAnswerGeneral
     * @param context the context you would like to use
     * @param customApiParams custom api params for special occasions (mostly debugging)
     * @param log_level (optional) logging level
     */
    constructor(
        context: Context,
        customApiParams: ApiParams,
        log_level: LogLevels = LogLevels.PUSHSDK_LOG_LEVEL_ERROR
    ) : this(context, customApiParams.baseURL, log_level) {
        APIHandler.API_PARAMS.setFrom(customApiParams)
    }

    /**
     * Constants and public methods
     */
    companion object {

        /**
         * Logging tag
         */
        const val TAG_LOGGING = "PushPushSDK"

        /**
         * Current log level
         */
        var currentLogLevel = LogLevels.PUSHSDK_LOG_LEVEL_ERROR

        /**
         * Get SDK version
         * @return SDK version name
         */
        fun getSDKVersionName(): String {
            return BuildConfig.VERSION_NAME
        }

        const val INTENT_ACTION_QUEUE = "com.push.android.pushsdkandroid.pushqueue"

    }

    /**
     * Log levels
     */
    enum class LogLevels {
        PUSHSDK_LOG_LEVEL_ERROR,
        PUSHSDK_LOG_LEVEL_DEBUG
    }

    //any classes initialization
    private var context: Context by Delegates.notNull()
    private var initHObject: Initialization = Initialization(context)
    private var apiPushData: APIHandler = APIHandler()
    private var requestAnswerHandlerAny: RequestAnswerHandler =
        RequestAnswerHandler()
    private var rewriteParams: RewriteParams =
        RewriteParams(context)
    private var pushDeviceType: String = ""

    //main class initialization
    init {
        APIHandler.API_PARAMS.baseURL = baseApiUrl
        this.context = context
        currentLogLevel = log_level
        pushDeviceType = Info.getPhoneType(context)
        try {
            val localDataLoaded = initHObject.hSdkGetParametersFromLocal()
            if (localDataLoaded.registrationStatus) {
                this.updateRegistration()
            }
        } catch (e: Exception) {
            PushSDKLogger.error("PushSDK.init registration update problem $e")
        }
        updateToken()
    }

    private var answerNotRegistered: PushKFunAnswerGeneral =
        PushKFunAnswerGeneral(704, "Failed", "Registration data not found", "Not registered")
    private var answerNotKnown: PushKFunAnswerGeneral =
        PushKFunAnswerGeneral(710, "Failed", "Unknown error", "unknown")

    //answer codes
    //200 - Ok

    //answers from remote server
    //401 HTTP code – (Client error) authentication error, probably errors
    //400 HTTP code – (Client error) request validation error, probably errors
    //500 HTTP code – (Server error) 

    //sdk errors
    //700 - internal SDK error
    //701 - already exists
    //704 - not registered
    //705 - remote server error
    //710 - unknown error

    //network errors
    //901 - failed registration with firebase

    //{
    //    "result":"Ok",
    //    "description":"",
    //    "code":200,
    //    "body":{
    //}
    //}


    /**
     * Register the device
     * @param clientAPIKey API key that you would be provided with
     * @param appFingerprint APP fingerprint that you would be provided with
     * @param userMsisdn Device phone number
     * @param userPassword password, associated with device phone number
     */
    fun registerNewDevice(
        clientAPIKey: String,
        appFingerprint: String,
        userMsisdn: String,
        userPassword: String
    ): PushKFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            val xPushSessionId = PushKDatabase.firebase_registration_token
            PushSDKLogger.debug("Start push_register_new X_Push_Client_API_Key: ${clientAPIKey}, X_Push_App_Fingerprint: ${appFingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Push_Session_Id: $xPushSessionId")

            if (PushKDatabase.registrationStatus) {
                return requestAnswerHandlerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )
            } else {

                if (xPushSessionId != "" && xPushSessionId != " ") {
                    val respPush: PushKDataApi2 = apiPushData.hDeviceRegister(
                        clientAPIKey,
                        xPushSessionId,
                        appFingerprint,
                        Info.getDeviceName(),
                        pushDeviceType,
                        Info.getOSType(),
                        getSDKVersionName(),
                        userPassword,
                        userMsisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushSDKLogger.debug("push_register_new response: $respPush")
                    PushSDKLogger.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPush.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPush.body.deviceId,
                        userMsisdn,
                        userPassword,
                        respPush.body.token,
                        respPush.body.userId,
                        respPush.body.createdAt,
                        regStatus
                    )
                    return PushKFunAnswerRegister(
                        code = respPush.code,
                        result = respPush.body.result,
                        description = respPush.body.description,
                        deviceId = respPush.body.deviceId,
                        token = respPush.body.token,
                        userId = respPush.body.userId,
                        userPhone = respPush.body.userPhone,
                        createdAt = respPush.body.createdAt
                    )
                } else {
                    return requestAnswerHandlerAny.registerProcedureAnswer2(
                        "901",
                        "X_Push_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return requestAnswerHandlerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }

    /**
     * Register the device, with FCM token
     * @param clientAPIKey API key that you would be provided with
     * @param appFingerprint APP fingerprint that you would be provided with
     * @param userMsisdn Device phone number
     * @param userPassword password, associated with device phone number
     * @param firebaseToken your firebase cloud messaging token
     *
     * @return PushKFunAnswerRegister
     */
    fun registerNewDevice(
        clientAPIKey: String,    // APP API key on push platform
        appFingerprint: String,   // App Fingerprint key
        userMsisdn: String,               // User MSISDN
        userPassword: String,             // User Password
        firebaseToken: String                // FCM firebase token
    ): PushKFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            PushSDKLogger.debug("Start push_register_new: X_Push_Client_API_Key: ${clientAPIKey}, X_Push_App_Fingerprint: ${appFingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Push_Session_Id: $firebaseToken")

            if (PushKDatabase.registrationStatus) {
                return requestAnswerHandlerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )

            } else {
                initHObject.hSdkUpdateFirebaseManual(firebaseToken)
                if (firebaseToken != "" && firebaseToken != " ") {
                    val respPush: PushKDataApi2 = apiPushData.hDeviceRegister(
                        clientAPIKey,
                        firebaseToken,
                        appFingerprint,
                        Info.getDeviceName(),
                        pushDeviceType,
                        Info.getOSType(),
                        getSDKVersionName(),
                        userPassword,
                        userMsisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushSDKLogger.debug("push_register_new response: $respPush")
                    PushSDKLogger.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPush.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPush.body.deviceId,
                        userMsisdn,
                        userPassword,
                        respPush.body.token,
                        respPush.body.userId,
                        respPush.body.createdAt,
                        regStatus
                    )

                    return PushKFunAnswerRegister(
                        code = respPush.code,
                        result = respPush.body.result,
                        description = respPush.body.description,
                        deviceId = respPush.body.deviceId,
                        token = respPush.body.token,
                        userId = respPush.body.userId,
                        userPhone = respPush.body.userPhone,
                        createdAt = respPush.body.createdAt
                    )
                } else {
                    return requestAnswerHandlerAny.registerProcedureAnswer2(
                        "901",
                        "X_Push_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return requestAnswerHandlerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }

    /**
     * Unregister the current device from database (if registered)
     */
    fun unregisterCurrentDevice(): PushKFunAnswerGeneral {
        try {
            PushSDKLogger.debug("push_clear_current_device start")
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            val xPushSessionId = PushKDatabase.firebase_registration_token
            if (PushKDatabase.registrationStatus) {
                PushSDKLogger.debug("Start push_clear_current_device: firebase_registration_token: ${xPushSessionId}, push_registration_token: ${PushKDatabase.push_k_registration_token}, registrationstatus: ${PushKDatabase.registrationStatus}, deviceId: ${PushKDatabase.deviceId}")

                val pushAnswer: PushKDataApi = apiPushData.hDeviceRevoke(
                    "[\"${PushKDatabase.deviceId}\"]",
                    xPushSessionId,
                    PushKDatabase.push_k_registration_token
                )
                PushSDKLogger.debug("push_answer : $pushAnswer")

                if (pushAnswer.code == 200) {
                    PushSDKLogger.debug("start clear data")
                    val deviceId = PushKDatabase.deviceId
                    initHObject.clearData()
                    return requestAnswerHandlerAny.generalAnswer(
                        "200",
                        "{\"device\":\"$deviceId\"}",
                        "Success"
                    )
                } else {
                    if (pushAnswer.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return requestAnswerHandlerAny.generalAnswer(
                        pushAnswer.code.toString(),
                        "{\"body\":\"unknown\"}",
                        "Some problem"
                    )
                }
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Get message history
     * @param periodInSeconds amount of time to get message history for
     *
     * @return PushKFunAnswerGeneral
     */
    fun getMessageHistory(periodInSeconds: Int): PushKFunAnswerGeneral {
        try {
            PushSDKLogger.debug("push_get_message_history period_in_seconds: $periodInSeconds")
            updateToken()
            PushSDKLogger.debug("Start push_get_message_history request: firebase_registration_token: ${PushKDatabase.firebase_registration_token}, push_registration_token: ${PushKDatabase.push_k_registration_token}, period_in_seconds: $periodInSeconds")
            if (PushKDatabase.registrationStatus) {
                val messHistPush: PushKFunAnswerGeneral = apiPushData.hGetMessageHistory(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token,
                    periodInSeconds
                )
                PushSDKLogger.debug("push_get_message_history mess_hist_push: $messHistPush")

                if (messHistPush.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return requestAnswerHandlerAny.generalAnswer(
                    messHistPush.code.toString(),
                    messHistPush.body,
                    "Success"
                )
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Get a list of all devices registered with the current phone number
     *
     * @return PushKFunAnswerGeneral
     */
    fun getAllRegisteredDevices(): PushKFunAnswerGeneral {
        try {
            PushSDKLogger.debug("Start push_get_device_all_from_server request: firebase_registration_token: ${PushKDatabase.firebase_registration_token}, push_registration_token: ${PushKDatabase.push_k_registration_token}")

            updateToken()
            if (PushKDatabase.registrationStatus) {
                val deviceAllPush: PushKDataApi = apiPushData.hGetDeviceAll(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                PushSDKLogger.debug("deviceAllPush : $deviceAllPush")

                if (deviceAllPush.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return requestAnswerHandlerAny.generalAnswer(
                    deviceAllPush.code.toString(),
                    deviceAllPush.body,
                    "Success"
                )
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Update registration
     *
     * @return PushKFunAnswerGeneral
     */
    fun updateRegistration(): PushKFunAnswerGeneral {
        try {
            PushSDKLogger.debug("push_update_registration started")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val resss: PushKDataApi = apiPushData.hDeviceUpdate(
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    Info.getDeviceName(),
                    pushDeviceType,
                    Info.getOSType(),
                    getSDKVersionName(),
                    PushKDatabase.firebase_registration_token
                )
                if (resss.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return requestAnswerHandlerAny.generalAnswer(resss.code.toString(), resss.body, "Success")
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Send a message to the server and receive a callback
     * @param messageId id of the message
     * @param messageText text of the message
     *
     * @return PushKFunAnswerGeneral
     */
    fun sendMessageAndReceiveCallback(
        messageId: String,
        messageText: String
    ): PushKFunAnswerGeneral {
        try {
            PushSDKLogger.debug("push_send_message_callback message_id: $messageId, message_text: $messageText")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val respp: PushKDataApi = apiPushData.hMessageCallback(
                    messageId,
                    messageText,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                if (respp.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return requestAnswerHandlerAny.generalAnswer(respp.code.toString(), respp.body, "Success")
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Send a delivery report for a specific message
     * @param messageId message id to get the delivery report for
     *
     * @return PushKFunAnswerGeneral
     */
    fun getMessageDeliveryReport(messageId: String): PushKFunAnswerGeneral {
        try {
            PushSDKLogger.debug("push_message_delivery_report message_id: $messageId")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                if (PushKDatabase.push_k_registration_token != "" && PushKDatabase.firebase_registration_token != "") {
                    val respp1: PushKDataApi = apiPushData.hMessageDr(
                        messageId,
                        PushKDatabase.firebase_registration_token, //_xPushSessionId
                        PushKDatabase.push_k_registration_token
                    )
                    if (respp1.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return requestAnswerHandlerAny.generalAnswer(respp1.code.toString(), respp1.body, "Success")
                } else {
                    return requestAnswerHandlerAny.generalAnswer(
                        "700",
                        "{}",
                        "Failed. firebase_registration_token or push_registration_token empty"
                    )
                }
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }


    /**
     * Unregister all devices registered with the current phone number from database
     *
     * @return PushKFunAnswerGeneral
     */
    fun unregisterAllDevices(): PushKFunAnswerGeneral {
        try {
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val deviceAllPush: PushKDataApi = apiPushData.hGetDeviceAll(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                PushSDKLogger.debug("push_clear_all_device deviceAllPush: $deviceAllPush")
                //push_clear_all_device deviceList: ["1062", "1063"]
                val devices = Gson().fromJson(deviceAllPush.body, JsonObject::class.java).getAsJsonArray("devices")
                val deviceIds = JsonArray()
                for (device in devices) {
                    deviceIds.add(device.asJsonObject.getAsJsonPrimitive("id").asString)
                }
                PushSDKLogger.debug("push_clear_all_device deviceIds: $deviceIds")

                val pushAnswer: PushKDataApi = apiPushData.hDeviceRevoke(
                    deviceIds.toString(),
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )

                PushSDKLogger.debug("push_answer : $pushAnswer")

                if (pushAnswer.code == 200) {
                    PushSDKLogger.debug("start clear data")
                    initHObject.clearData()
                    return requestAnswerHandlerAny.generalAnswer("200", "{\"devices\":$deviceIds}", "Success")
                } else {
                    if (pushAnswer.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return requestAnswerHandlerAny.generalAnswer(
                        pushAnswer.code.toString(),
                        "{\"body\":\"unknown\"}",
                        "Some problem"
                    )

                }
            } else {
                return answerNotRegistered
            }

        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Change phone number
     * @param newMsisdn new phone number
     *
     * @return PushKFunAnswerGeneral
     */
    fun rewriteMsisdn(newMsisdn: String): PushKFunAnswerGeneral {
        PushSDKLogger.debug("rewrite_msisdn start: $newMsisdn")
        return try {
            if (PushKDatabase.registrationStatus) {
                rewriteParams.rewritePushUserMsisdn(newMsisdn)
                requestAnswerHandlerAny.generalAnswer("200", "{}", "Success")
            } else {
                answerNotRegistered
            }
        } catch (e: Exception) {
            answerNotKnown
        }
    }

    /**
     * Change password (temporary)
     * @param newPassword new password
     *
     * @return PushKFunAnswerGeneral
     */
    fun rewritePassword(newPassword: String): PushKFunAnswerGeneral {

        PushSDKLogger.debug("rewrite_password start: $newPassword")

        return if (PushKDatabase.registrationStatus) {
            rewriteParams.rewritePushUserPassword(newPassword)
            requestAnswerHandlerAny.generalAnswer("200", "{}", "Success")
        } else {
            answerNotRegistered
        }
    }

    /**
     * Checks undelivered message queue and sends delivery report for the messages;
     * Will also broadcast an intent with all the queued message
     *
     * @return PushKFunAnswerGeneral
     */
    fun checkMessageQueue(): PushKFunAnswerGeneral {
        try {
            updateToken()
            if (PushKDatabase.registrationStatus) {
                if (PushKDatabase.firebase_registration_token != "" && PushKDatabase.push_k_registration_token != "") {
                    //val queue = QueueProc2()
                    val answerData = apiPushData.getDevicePushMsgQueue(
                        PushKDatabase.firebase_registration_token,
                        PushKDatabase.push_k_registration_token, context
                    )

                    PushSDKLogger.debug("push_k_check_queue answerData: $answerData")

                    return requestAnswerHandlerAny.generalAnswer("200", "{}", "Success")
                } else {
                    return requestAnswerHandlerAny.generalAnswer(
                        "700",
                        "{}",
                        "Failed. firebase_registration_token or push_k_registration_token empty"
                    )
                }
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    /**
     * Update FCM token
     */
    private fun updateToken() {
        PushSDKLogger.debug("PushSDK.updateToken started2")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    PushSDKLogger.debug("PushSDK.updateToken experimental: failed")
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                task.result?.let {
                    if (it.token != "") {
                        if (it.token != PushKDatabase.firebase_registration_token) {
                            PushKDatabase.firebase_registration_token = it.token
                            PushSDKLogger.debug("PushSDK.updateToken token2: $it.token")
                            rewriteParams.rewriteFirebaseToken(it.token)
                        }
                    }
                }
            })
        PushSDKLogger.debug("PushSDK.updateToken finished2")
    }
}
