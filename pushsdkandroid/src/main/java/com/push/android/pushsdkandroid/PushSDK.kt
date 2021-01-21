package com.push.android.pushsdkandroid

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.push.android.pushsdkandroid.utils.Info
import com.push.android.pushsdkandroid.core.*
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import com.push.android.pushsdkandroid.models.*
import com.push.android.pushsdkandroid.models.PushKDataApi
import kotlin.properties.Delegates

/**
 * Main class, used for initialization. Only works with API v3.0
 * @see PushKFunAnswerGeneral
 * @param context the context you would like to use
 * @param baseApiUrl base api url, like "https://example.io/api/3.0/"
 * @param log_level (optional) logging level
 */
@Suppress("unused")
class PushSDK(
    context: Context,
    baseApiUrl: String,
    log_level: LogLevels = LogLevels.PUSHSDK_LOG_LEVEL_ERROR
) {

    /**
     * Constants and public methods
     */
    companion object {

        /**
         * Logging tag
         */
        const val TAG_LOGGING = "PushPushSDK"

        /**
         * Get SDK version
         * @return SDK version name
         */
        fun getSDKVersionName(): String {
            return BuildConfig.VERSION_NAME
        }

        /**
         * Intent action for sending queue messages
         */
        const val BROADCAST_QUEUE_INTENT_ACTION = "com.push.android.pushsdkandroid.pushqueue"

        /**
         * Name of the extra inside the intent that broadcasts message queue
         */
        const val BROADCAST_QUEUE_EXTRA_NAME = "queue"

        /**
         * Intent action when user clicks a notification
         */
        const val NOTIFICATION_CLICK_INTENT_ACTION = "pushsdk.intent.action.notification"

        /**
         * Name of the extra inside the intent that broadcasts push data
         */
        const val NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME = "data"

        /**
         * Action for intent that is broadcasted when a push is received
         */
        const val BROADCAST_PUSH_DATA_INTENT_ACTION = "com.push.android.pushsdkandroid.Push"

        /**
         * Name of the extra inside the intent that broadcasts push data
         */
        const val BROADCAST_PUSH_DATA_EXTRA_NAME = "data"

    }

    /**
     * Log levels
     */
    enum class LogLevels {
        PUSHSDK_LOG_LEVEL_ERROR,
        PUSHSDK_LOG_LEVEL_DEBUG
    }

    //init stuff
    private var context: Context by Delegates.notNull()
    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)
    private var apiHandler: APIHandler = APIHandler(context)
    private var pushDeviceType: String = ""

    //main class initialization
    init {
        this.context = context
        pushSdkSavedDataProvider.baseApiUrl = baseApiUrl
        pushSdkSavedDataProvider.logLevel = log_level.name
        pushDeviceType = Info.getPhoneType(context)
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                updateRegistration()
            }
        } catch (e: Exception) {
            PushSDKLogger.error("PushSDK.init registration update problem:\n${Log.getStackTraceString(e)}")
        }
    }

    /**
     * Update FCM token
     */
    private fun updateFCMToken() {
        //fixme listener will not be called instantly, so it won't update synchronously
        PushSDKLogger.debug(context, "Updating FCM token")
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let {
                    if (it.token != "" && it.token != pushSdkSavedDataProvider.firebase_registration_token) {
                        PushSDKLogger.debug(context, "FCM token updated successfully.")
                        PushSDKLogger.debug(context, "Old FCM token: ${pushSdkSavedDataProvider.firebase_registration_token}")
                        PushSDKLogger.debug(context, "New FCM token: ${it.token}")
                        pushSdkSavedDataProvider.firebase_registration_token = it.token
                    }
                    else {
                        PushSDKLogger.debug(context, "New FCM token is either empty or up-to-date")
                        PushSDKLogger.debug(context, "Current FCM token: ${pushSdkSavedDataProvider.firebase_registration_token}")
                        PushSDKLogger.debug(context, "Received FCM token: ${it.token}")
                    }
                }
            }
            else {
                PushSDKLogger.error("Unable to update FCM token, task is not successful")
            }
        }
    }

    /**
     * Clear local data
     */
    private fun clearData() {
        PushSDKLogger.debug(context, "Attempting to clear local registration data")
        pushSdkSavedDataProvider.registrationStatus = false
        pushSdkSavedDataProvider.deviceId = ""
        pushSdkSavedDataProvider.push_k_user_id = ""
    }

    /**
     * Save various params locally
     */
    private fun hSdkInitSaveToLocal(
            deviceId: String,
            push_k_user_msisdn: String,
            push_k_user_Password: String,
            push_k_registration_token: String,
            push_k_user_id: String,
            push_k_registration_createdAt: String,
            registrationStatus: Boolean
    ) {
        PushSDKLogger.debug(context, "Saving params locally")
        pushSdkSavedDataProvider.deviceId = deviceId
        pushSdkSavedDataProvider.push_k_user_msisdn = push_k_user_msisdn
        pushSdkSavedDataProvider.push_k_user_Password = push_k_user_Password
        pushSdkSavedDataProvider.push_k_registration_createdAt = push_k_registration_createdAt
        pushSdkSavedDataProvider.push_k_user_id = push_k_user_id
        pushSdkSavedDataProvider.push_k_registration_token = push_k_registration_token
        pushSdkSavedDataProvider.registrationStatus = registrationStatus
    }

    //answer codes
    //200 - OK

    //answers from remote server
    //401 HTTP code – Dead token | (Client error) authentication error, probably errors
    //400 HTTP code – (Client error) request validation error, probably errors
    //500 HTTP code – (Server error) 

    //sdk errors
    //700 - internal SDK error
    //701 - already exists
    //704 - not registered (locally)
    //710 - unknown error

    //network errors
    //901 - failed registration with firebase

    //{
    //    "result":"Ok",
    //    "description":"",
    //    "code":200,
    //    "body":"{}"
    //}


    /**
     * Register the device
     * @param clientAPIKey API key that you would be provided with
     * @param appFingerprint APP fingerprint that you would be provided with
     * @param userMsisdn Device phone number
     * @param userPassword password, associated with device phone number
     * @param firebaseToken (Optional) your firebase cloud messaging token
     */
    fun registerNewDevice(
        clientAPIKey: String,
        appFingerprint: String,
        userMsisdn: String,
        userPassword: String,
        firebaseToken: String = ""
    ): PushKFunAnswerRegister {
        PushSDKLogger.debug(context, "calling registerNewDevice() with params:\n" +
                "clientAPIKey $clientAPIKey\n" +
                "appFingerprint $appFingerprint\n" +
                "userMsisdn $userMsisdn\n" +
                "userPassword $userPassword" +
                "firebaseToken $firebaseToken")
        try {
            updateFCMToken()
            val firebaseTokenToUse = when (firebaseToken) {
                "" -> {
                    pushSdkSavedDataProvider.firebase_registration_token
                }
                else -> {
                    firebaseToken
                }
            }
            if (pushSdkSavedDataProvider.registrationStatus) {
                return PushKFunAnswerRegister(
                        code = 701,
                        deviceId = pushSdkSavedDataProvider.deviceId,
                        token = pushSdkSavedDataProvider.push_k_registration_token,
                        userId = pushSdkSavedDataProvider.push_k_user_id,
                        userPhone = pushSdkSavedDataProvider.push_k_user_msisdn,
                        createdAt = pushSdkSavedDataProvider.push_k_registration_createdAt,
                        result = PushSDKRegAnswerResult.EXISTS,
                        description = "Device already registered. Nothing to do"
                )
            }
            else if (firebaseTokenToUse == "" || firebaseTokenToUse == " ") {
                return PushKFunAnswerRegister(
                        code = 901,
                        description = "X_Push_Session_Id is empty. Maybe firebase registration problem",
                        result = PushSDKRegAnswerResult.FAILED,
                        deviceId = "unknown",
                        token = "unknown",
                        userId = "unknown",
                        userPhone = "unknown",
                        createdAt = "unknown"
                )
            }
            else {
                val requestResponse = apiHandler.hDeviceRegister(
                        clientAPIKey,
                        firebaseTokenToUse,
                        appFingerprint,
                        Info.getDeviceName(),
                        pushDeviceType,
                        Info.getOSType(),
                        getSDKVersionName(),
                        userPassword,
                        userMsisdn
                )
                return when (requestResponse.code) {
                    200 -> {
                        val parent = Gson().fromJson(requestResponse.body, JsonObject::class.java)
                        val deviceId = parent.getAsJsonObject("device").get("deviceId").asString
                        val token = parent.getAsJsonObject("session").get("token").asString
                        val userId = parent.getAsJsonObject("profile").get("userId").asString
                        val userPhone = parent.getAsJsonObject("profile").get("userPhone").asString
                        val createdAt = parent.getAsJsonObject("profile").get("createdAt").asString

                        hSdkInitSaveToLocal(
                                deviceId,
                                userMsisdn,
                                userPassword,
                                token,
                                userId,
                                createdAt,
                                true
                        )

                        PushKFunAnswerRegister(
                                code = 200,
                                description = "Success",
                                result = PushSDKRegAnswerResult.OK,
                                deviceId = deviceId,
                                token = token,
                                userId = userId,
                                userPhone = userPhone,
                                createdAt = createdAt
                        )
                    }
                    401 -> {
                        PushKFunAnswerRegister(
                                code = 401,
                                description = "(Client error) authentication error, probably errors",
                                result = PushSDKRegAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                        )
                    }
                    400 -> {
                        PushKFunAnswerRegister(
                                code = 400,
                                description = "(Client error) request validation error",
                                result = PushSDKRegAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                        )
                    }
                    500 -> {
                        PushKFunAnswerRegister(
                                code = 500,
                                description = "(Server error)",
                                result = PushSDKRegAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                        )
                    }
                    else -> {
                        PushKFunAnswerRegister(
                                code = 710,
                                description = "Unknown error",
                                result = PushSDKRegAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                        )
                    }
                }
            }
        }
        catch (e: Exception) {
            return PushKFunAnswerRegister(
                    code = 700,
                    description = "Internal SDK error",
                    result = PushSDKRegAnswerResult.FAILED,
                    deviceId = "unknown",
                    token = "unknown",
                    userId = "unknown",
                    userPhone = "unknown",
                    createdAt = "unknown"
            )
        }
    }

    /**
     * Unregister the current device from database (if registered)
     */
    fun unregisterCurrentDevice(): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling unregisterCurrentDevice()")
        try {
            updateFCMToken()
            val xPushSessionId = pushSdkSavedDataProvider.firebase_registration_token
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.hDeviceRevoke(
                    "[\"${pushSdkSavedDataProvider.deviceId}\"]",
                    xPushSessionId,
                    pushSdkSavedDataProvider.push_k_registration_token
                )
                val deviceId = pushSdkSavedDataProvider.deviceId
                return when (requestResponse.code) {
                    200 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                "{\"device\":\"$deviceId\"}")
                    }
                    401 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body)
                    }
                    else -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                "{\"device\":\"$deviceId\"}")
                    }
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("unregisterCurrentDevice() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    /**
     * Unregister all devices registered with the current phone number from database
     *
     * @return PushKFunAnswerGeneral
     */
    fun unregisterAllDevices(): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling unregisterAllDevices() with params:")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.hGetDeviceAll(
                        pushSdkSavedDataProvider.firebase_registration_token, //_xPushSessionId
                        pushSdkSavedDataProvider.push_k_registration_token
                )
                val devices = Gson().fromJson(requestResponse.body, JsonObject::class.java)
                        .getAsJsonArray("devices")
                val deviceIds = JsonArray()
                for (device in devices) {
                    deviceIds.add(device.asJsonObject.getAsJsonPrimitive("id").asString)
                }
                PushSDKLogger.debug(context, "generated deviceIds: $deviceIds")
                val revokeRequestResponse: PushKDataApi = apiHandler.hDeviceRevoke(
                        deviceIds.toString(),
                        pushSdkSavedDataProvider.firebase_registration_token, //_xPushSessionId
                        pushSdkSavedDataProvider.push_k_registration_token
                )
                return when (revokeRequestResponse.code) {
                    200 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                revokeRequestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                "{\"devices\":\"$deviceIds\"}")
                    }
                    401 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                revokeRequestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                revokeRequestResponse.body)
                    }
                    else -> {
                        PushKFunAnswerGeneral(
                                revokeRequestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                "{\"devices\":\"$deviceIds\"}")
                    }
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }

        } catch (e: Exception) {
            PushSDKLogger.error("unregisterAllDevices() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    /**
     * Get message history
     * @param periodInSeconds amount of time to get message history for
     *
     * @return PushKFunAnswerGeneral
     */
    fun getMessageHistory(periodInSeconds: Int): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling getMessageHistory() with params:\n" +
                "periodInSeconds $periodInSeconds")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.hGetMessageHistory(
                    pushSdkSavedDataProvider.firebase_registration_token, //_xPushSessionId
                    pushSdkSavedDataProvider.push_k_registration_token,
                    periodInSeconds
                )
                return when (requestResponse.code) {
                    200 -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                requestResponse.body)
                    }
                    401 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body)
                    }
                    else -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                requestResponse.body)
                    }
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("getMessageHistory() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    /**
     * Get a list of all devices registered with the current phone number
     *
     * @return PushKFunAnswerGeneral
     */
    fun getAllRegisteredDevices(): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling getAllRegisteredDevices()")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.hGetDeviceAll(
                    pushSdkSavedDataProvider.firebase_registration_token, //_xPushSessionId
                    pushSdkSavedDataProvider.push_k_registration_token
                )
                return when (requestResponse.code) {
                    200 -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                requestResponse.body)
                    }
                    401 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body)
                    }
                    else -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                requestResponse.body)
                    }
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("getAllRegisteredDevices() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    /**
     * Update registration
     *
     * @return PushKFunAnswerGeneral
     */
    fun updateRegistration(): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling updateRegistration()")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.hDeviceUpdate(
                    pushSdkSavedDataProvider.push_k_registration_token, //pushSdkSavedDataProvider.push_k_registration_token
                    pushSdkSavedDataProvider.firebase_registration_token, // pushSdkSavedDataProvider.firebase_registration_token
                    Info.getDeviceName(),
                    pushDeviceType,
                    Info.getOSType(),
                    getSDKVersionName(),
                    pushSdkSavedDataProvider.firebase_registration_token
                )
                return when (requestResponse.code) {
                    200 -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                requestResponse.body)
                    }
                    401 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body)
                    }
                    else -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                requestResponse.body)
                    }
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("updateRegistration() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
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
        PushSDKLogger.debug(context, "calling sendMessageAndReceiveCallback() with params:\n" +
                "messageId $messageId\n" +
                "messageText $messageText")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.hMessageCallback(
                    messageId,
                    messageText,
                    pushSdkSavedDataProvider.firebase_registration_token, //_xPushSessionId
                    pushSdkSavedDataProvider.push_k_registration_token
                )
                return when (requestResponse.code) {
                    200 -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                requestResponse.body)
                    }
                    401 -> {
                        clearData()
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body)
                    }
                    else -> {
                        PushKFunAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                requestResponse.body)
                    }
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("sendMessageAndReceiveCallback() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    /**
     * Send a delivery report for a specific message
     * @param messageId message id to get the delivery report for
     *
     * @return PushKFunAnswerGeneral
     */
    fun getMessageDeliveryReport(messageId: String): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling getMessageDeliveryReport() with params:\n" +
                "messageId $messageId")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                if (pushSdkSavedDataProvider.push_k_registration_token != ""
                        && pushSdkSavedDataProvider.firebase_registration_token != "") {
                    val requestResponse = apiHandler.hMessageDr(
                        messageId,
                        pushSdkSavedDataProvider.firebase_registration_token, //_xPushSessionId
                        pushSdkSavedDataProvider.push_k_registration_token
                    )
                    return when (requestResponse.code) {
                        200 -> {
                            PushKFunAnswerGeneral(
                                    requestResponse.code,
                                    PushSDKAnswerResult.OK,
                                    "Success",
                                    requestResponse.body)
                        }
                        401 -> {
                            clearData()
                            PushKFunAnswerGeneral(
                                    requestResponse.code,
                                    PushSDKAnswerResult.FAILED,
                                    "Auth token is probably dead. Try to register your device again.",
                                    requestResponse.body)
                        }
                        else -> {
                            PushKFunAnswerGeneral(
                                    requestResponse.code,
                                    PushSDKAnswerResult.FAILED,
                                    "Error",
                                    requestResponse.body)
                        }
                    }
                } else {
                    return PushKFunAnswerGeneral(
                            700,
                            PushSDKAnswerResult.FAILED,
                            "Failed. firebase_registration_token or push_registration_token empty",
                            "{}")
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("getMessageDeliveryReport() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    private fun broadcastQueue(queueMessagesRaw: String) {
        PushSDKLogger.debug(context, "calling broadcastQueue() with params:\n" +
                "queueMessagesRaw $queueMessagesRaw")
        //Parse string here as json, then foreach -> send delivery
        val queueMessages = Gson().fromJson(queueMessagesRaw, QueueMessages::class.java)
        if (queueMessages.messages.isNotEmpty()) {
            Intent().apply {
                action = BROADCAST_QUEUE_INTENT_ACTION
                putExtra(BROADCAST_QUEUE_EXTRA_NAME, queueMessagesRaw)
                context.sendBroadcast(this)
            }
            //send delivery reports here
            queueMessages.messages.forEach { message ->
                apiHandler.hMessageDr(
                        message.messageId,
                        pushSdkSavedDataProvider.firebase_registration_token,
                        pushSdkSavedDataProvider.push_k_registration_token
                )
            }
        }
        else {
            PushSDKLogger.debug(context, "queueMessagesRaw had no queued messages")
        }
    }

    /**
     * Checks undelivered message queue and sends delivery report for the messages;
     * Will also broadcast an intent with all the queued message
     *
     * @return PushKFunAnswerGeneral
     */
    fun checkMessageQueue(): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling checkMessageQueue()")
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                if (pushSdkSavedDataProvider.firebase_registration_token != ""
                        && pushSdkSavedDataProvider.push_k_registration_token != "") {
                    val requestResponse = apiHandler.getDevicePushMsgQueue(
                        pushSdkSavedDataProvider.firebase_registration_token,
                        pushSdkSavedDataProvider.push_k_registration_token
                    )
                    return when (requestResponse.code) {
                        200 -> {
                            broadcastQueue(requestResponse.body)
                            PushKFunAnswerGeneral(
                                    requestResponse.code,
                                    PushSDKAnswerResult.OK,
                                    "Success",
                                    requestResponse.body)
                        }
                        401 -> {
                            clearData()
                            PushKFunAnswerGeneral(
                                    requestResponse.code,
                                    PushSDKAnswerResult.FAILED,
                                    "Auth token is probably dead. Try to register your device again.",
                                    requestResponse.body)
                        }
                        else -> {
                            PushKFunAnswerGeneral(
                                    requestResponse.code,
                                    PushSDKAnswerResult.FAILED,
                                    "Error",
                                    requestResponse.body)
                        }
                    }
                } else {
                    return PushKFunAnswerGeneral(
                            700,
                            PushSDKAnswerResult.FAILED,
                            "Failed. firebase_registration_token or push_k_registration_token empty",
                            "{}")
                }
            } else {
                return PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("checkMessageQueue() failed with exception: ${Log.getStackTraceString(e)}")
            return PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    /**
     * Change phone number (Locally)
     * @param newMsisdn new phone number
     *
     * @return PushKFunAnswerGeneral
     */
    fun rewriteMsisdn(newMsisdn: String): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "calling rewriteMsisdn() with params:\n" +
                "newMsisdn $newMsisdn")
        return try {
            if (pushSdkSavedDataProvider.registrationStatus) {
                pushSdkSavedDataProvider.push_k_user_msisdn = newMsisdn
                PushKFunAnswerGeneral(200, PushSDKAnswerResult.OK, "Success", "{}")
            } else {
                PushKFunAnswerGeneral(
                        704,
                        PushSDKAnswerResult.FAILED,
                        "Registration data not found",
                        "Not registered")
            }
        } catch (e: Exception) {
            PushSDKLogger.error("rewriteMsisdn() failed with exception: ${Log.getStackTraceString(e)}")
            PushKFunAnswerGeneral(
                    710,
                    PushSDKAnswerResult.FAILED,
                    "Unknown error",
                    "unknown")
        }
    }

    //TODO remove once confirmed useless
    /**
     * Change password (temporary)
     * @param newPassword new password
     *
     * @return PushKFunAnswerGeneral
     */
    fun rewritePassword(newPassword: String): PushKFunAnswerGeneral {
        PushSDKLogger.debug(context, "rewrite_password start: $newPassword")
        return if (pushSdkSavedDataProvider.registrationStatus) {
            pushSdkSavedDataProvider.push_k_user_Password = newPassword
            PushKFunAnswerGeneral(200, PushSDKAnswerResult.OK, "Success", "{}")
        } else {
            PushKFunAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered")
        }
    }
}
