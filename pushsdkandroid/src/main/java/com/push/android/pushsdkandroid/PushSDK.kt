package com.push.android.pushsdkandroid

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.push.android.pushsdkandroid.add.Answer
import com.push.android.pushsdkandroid.add.GetInfo
import com.push.android.pushsdkandroid.add.PushParsing
import com.push.android.pushsdkandroid.add.RewriteParams
import com.push.android.pushsdkandroid.core.*
import com.push.android.pushsdkandroid.logger.PushKLoggerSdk
import kotlin.properties.Delegates

@Suppress("SpellCheckingInspection")
object PushKPushMess {
    var message: String? = null   //global variable for push messages
    var log_level_active: String = "error" //global variable sdk log level
    //push_message_style types
    //0 - only text in push notification
    //1 - text and large image in notification
}

internal lateinit var PushKDatabase: PushOperativeData

@Suppress("SpellCheckingInspection", "unused", "FunctionName")
class PushSDKQueue {

    fun push_check_queue(context: Context): PushKFunAnswerGeneral {
        val answerNotKnown = PushKFunAnswerGeneral(710, "Failed", "Unknown error", "unknown")
        try {
            val answ = Answer()
            val answerNotRegistered = PushKFunAnswerGeneral(
                704,
                "Failed",
                "Registration data not found",
                "Not registered"
            )
            val initPushParams2 = Initialization(context)
            initPushParams2.hSdkGetParametersFromLocal()

            return if (PushKDatabase.registrationStatus) {
                val queue = QueueProc()
                val anss = queue.pushDeviceMessQueue(
                    PushKDatabase.firebase_registration_token,
                    PushKDatabase.push_k_registration_token, context
                )
                PushKLoggerSdk.debug("PushSDKQueue.push_check_queue response: $anss")
                answ.generalAnswer("200", "{}", "Success")
            } else {
                answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }
}

@Suppress("SpellCheckingInspection", "unused", "FunctionName", "MemberVisibilityCanBePrivate")
class PushSDK(
    context: Context,
    platform_branch: UrlsPlatformList = PushSdkParametersPublic.branchMasterValue,
    log_level: String = "error",
    basePushURL: String
) {
    //any classes initialization
    private var context: Context by Delegates.notNull()
    private var initHObject: Initialization = Initialization(context)
    private var localDeviceInfo: GetInfo = GetInfo()
    private var apiPushData: PushKApi = PushKApi()
    private var answerAny: Answer = Answer()
    private var rewriteParams: RewriteParams = RewriteParams(context)
    private var parsing: PushParsing = PushParsing()
    private var pushInternalParamsObject: PushSdkParameters = PushSdkParameters
    private var pushDeviceType: String = ""
    private var parsingPushClass: PushParsing = PushParsing()

    //main class initialization
    init {
        this.context = context
        PushKPushMess.log_level_active = log_level
        pushDeviceType = localDeviceInfo.getPhoneType(context)
        if (basePushURL != "") {
            PushSdkParameters.branchCurrentActivePath = parsingPushClass.pathTransformation(
                baseUrl = basePushURL,
                pathUpl = platform_branch
            )
        } else {
            throw IllegalArgumentException("incorrect basePushURL parameter")
        }
        try {
            val localDataLoaded = initHObject.hSdkGetParametersFromLocal()
            if (localDataLoaded.registrationStatus) {
                this.push_update_registration()
            }
        } catch (e: Exception) {
            PushKLoggerSdk.error("PushSDK.init registration update problem $e")
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


    //1
    fun push_register_new(
        X_Push_Client_API_Key: String,
        X_Push_App_Fingerprint: String,
        user_msisdn: String,
        user_password: String
    ): PushKFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            val xPushSessionId = PushKDatabase.firebase_registration_token
            PushKLoggerSdk.debug("Start push_register_new X_Push_Client_API_Key: ${X_Push_Client_API_Key}, X_Push_App_Fingerprint: ${X_Push_App_Fingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Push_Session_Id: $xPushSessionId")

            if (PushKDatabase.registrationStatus) {
                return answerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )
            } else {

                if (xPushSessionId != "" && xPushSessionId != " ") {
                    val respPush: PushKDataApi2 = apiPushData.hDeviceRegister(
                        X_Push_Client_API_Key,
                        xPushSessionId,
                        X_Push_App_Fingerprint,
                        PushSdkParameters.push_k_deviceName,
                        pushDeviceType,
                        PushSdkParameters.push_k_osType,
                        PushSdkParameters.sdkVersion,
                        user_password,
                        user_msisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushKLoggerSdk.debug("push_register_new response: $respPush")
                    PushKLoggerSdk.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPush.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPush.body.deviceId,
                        user_msisdn,
                        user_password,
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
                    return answerAny.registerProcedureAnswer2(
                        "901",
                        "X_Push_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return answerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }


    //1-1
    //registration procedure with direct FCM token input
    //push_register_new2(
    fun push_register_new(
        X_Push_Client_API_Key: String,    // APP API key on push platform
        X_Push_App_Fingerprint: String,   // App Fingerprint key
        user_msisdn: String,               // User MSISDN
        user_password: String,             // User Password
        X_FCM_token: String                // FCM firebase token
    ): PushKFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            PushKLoggerSdk.debug("Start push_register_new: X_Push_Client_API_Key: ${X_Push_Client_API_Key}, X_Push_App_Fingerprint: ${X_Push_App_Fingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Push_Session_Id: $X_FCM_token")

            if (PushKDatabase.registrationStatus) {
                return answerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )

            } else {
                initHObject.hSdkUpdateFirebaseManual(X_FCM_token)
                if (X_FCM_token != "" && X_FCM_token != " ") {
                    val respPush: PushKDataApi2 = apiPushData.hDeviceRegister(
                        X_Push_Client_API_Key,
                        X_FCM_token,
                        X_Push_App_Fingerprint,
                        PushSdkParameters.push_k_deviceName,
                        pushDeviceType,
                        PushSdkParameters.push_k_osType,
                        PushSdkParameters.sdkVersion,
                        user_password,
                        user_msisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushKLoggerSdk.debug("push_register_new response: $respPush")
                    PushKLoggerSdk.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPush.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPush.body.deviceId,
                        user_msisdn,
                        user_password,
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
                    return answerAny.registerProcedureAnswer2(
                        "901",
                        "X_Push_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return answerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }

    //2
    fun push_clear_current_device(): PushKFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("push_clear_current_device start")
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            val xPushSessionId = PushKDatabase.firebase_registration_token
            if (PushKDatabase.registrationStatus) {
                PushKLoggerSdk.debug("Start push_clear_current_device: firebase_registration_token: ${xPushSessionId}, push_registration_token: ${PushKDatabase.push_k_registration_token}, registrationstatus: ${PushKDatabase.registrationStatus}, deviceId: ${PushKDatabase.deviceId}")

                val pushAnswer: PushKDataApi = apiPushData.hDeviceRevoke(
                    "[\"${PushKDatabase.deviceId}\"]",
                    xPushSessionId,
                    PushKDatabase.push_k_registration_token
                )
                PushKLoggerSdk.debug("push_answer : $pushAnswer")

                if (pushAnswer.code == 200) {
                    PushKLoggerSdk.debug("start clear data")
                    val deviceId = PushKDatabase.deviceId
                    initHObject.clearData()
                    return answerAny.generalAnswer(
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
                    return answerAny.generalAnswer(
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

    //return all message history till time
    //3
    fun push_get_message_history(period_in_seconds: Int): PushKFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("push_get_message_history period_in_seconds: $period_in_seconds")
            updateToken()
            PushKLoggerSdk.debug("Start push_get_message_history request: firebase_registration_token: ${PushKDatabase.firebase_registration_token}, push_registration_token: ${PushKDatabase.push_k_registration_token}, period_in_seconds: $period_in_seconds")
            if (PushKDatabase.registrationStatus) {
                val messHistPush: PushKFunAnswerGeneral = apiPushData.hGetMessageHistory(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token,
                    period_in_seconds
                )
                PushKLoggerSdk.debug("push_get_message_history mess_hist_push: $messHistPush")

                if (messHistPush.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(
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

    //4
    fun push_get_device_all_from_server(): PushKFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("Start push_get_device_all_from_server request: firebase_registration_token: ${PushKDatabase.firebase_registration_token}, push_registration_token: ${PushKDatabase.push_k_registration_token}")

            updateToken()
            if (PushKDatabase.registrationStatus) {
                val deviceAllPush: PushKDataApi = apiPushData.hGetDeviceAll(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                PushKLoggerSdk.debug("deviceAllPush : $deviceAllPush")

                if (deviceAllPush.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(
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

    //5
    fun push_update_registration(): PushKFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("push_update_registration started")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val resss: PushKDataApi = apiPushData.hDeviceUpdate(
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    pushInternalParamsObject.push_k_deviceName,
                    pushDeviceType,
                    pushInternalParamsObject.push_k_osType,
                    pushInternalParamsObject.sdkVersion,
                    PushKDatabase.firebase_registration_token
                )
                if (resss.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(resss.code.toString(), resss.body, "Success")
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //6
    fun push_send_message_callback(
        message_id: String,
        message_text: String
    ): PushKFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("push_send_message_callback message_id: $message_id, message_text: $message_text")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val respp: PushKDataApi = apiPushData.hMessageCallback(
                    message_id,
                    message_text,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                if (respp.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(respp.code.toString(), respp.body, "Success")
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //7
    fun push_message_delivery_report(message_id: String): PushKFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("push_message_delivery_report message_id: $message_id")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                if (PushKDatabase.push_k_registration_token != "" && PushKDatabase.firebase_registration_token != "") {
                    val respp1: PushKDataApi = apiPushData.hMessageDr(
                        message_id,
                        PushKDatabase.firebase_registration_token, //_xPushSessionId
                        PushKDatabase.push_k_registration_token
                    )
                    if (respp1.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answerAny.generalAnswer(respp1.code.toString(), respp1.body, "Success")
                } else {
                    return answerAny.generalAnswer(
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


    //8 delete all devices
    fun push_clear_all_device(): PushKFunAnswerGeneral {
        try {
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val deviceAllPush: PushKDataApi = apiPushData.hGetDeviceAll(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                PushKLoggerSdk.debug("push_clear_all_device deviceAllPush: $deviceAllPush")

                val deviceList: String = parsing.parseIdDevicesAll(deviceAllPush.body)

                val pushAnswer: PushKDataApi = apiPushData.hDeviceRevoke(
                    deviceList,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )

                PushKLoggerSdk.debug("push_answer : $pushAnswer")

                if (pushAnswer.code == 200) {
                    PushKLoggerSdk.debug("start clear data")
                    initHObject.clearData()
                    return answerAny.generalAnswer("200", "{\"devices\":$deviceList}", "Success")
                } else {
                    if (pushAnswer.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answerAny.generalAnswer(
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

    //9temp
    fun rewrite_msisdn(newmsisdn: String): PushKFunAnswerGeneral {
        PushKLoggerSdk.debug("rewrite_msisdn start: $newmsisdn")
        return try {
            if (PushKDatabase.registrationStatus) {
                rewriteParams.rewritePushUserMsisdn(newmsisdn)
                answerAny.generalAnswer("200", "{}", "Success")
            } else {
                answerNotRegistered
            }
        } catch (e: Exception) {
            answerNotKnown
        }
    }

    //10temp
    fun rewrite_password(newPassword: String): PushKFunAnswerGeneral {

        PushKLoggerSdk.debug("rewrite_password start: $newPassword")

        return if (PushKDatabase.registrationStatus) {
            rewriteParams.rewritePushUserPassword(newPassword)
            answerAny.generalAnswer("200", "{}", "Success")
        } else {
            answerNotRegistered
        }
    }


    //11push
    fun push_check_queue(): PushKFunAnswerGeneral {
        try {
            updateToken()
            if (PushKDatabase.registrationStatus) {
                if (PushKDatabase.firebase_registration_token != "" && PushKDatabase.push_k_registration_token != "") {
                    val queue = QueueProc()
                    val answerData = queue.pushDeviceMessQueue(
                        PushKDatabase.firebase_registration_token,
                        PushKDatabase.push_k_registration_token, context
                    )

                    PushKLoggerSdk.debug("push_k_check_queue answerData: $answerData")

                    return answerAny.generalAnswer("200", "{}", "Success")
                } else {
                    return answerAny.generalAnswer(
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

    private fun updateToken() {
        PushKLoggerSdk.debug("PushSDK.updateToken started2")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    PushKLoggerSdk.debug("PushSDK.updateToken experimental: failed")
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result!!.token
                if (token != "") {
                    if (token != PushKDatabase.firebase_registration_token) {
                        PushKDatabase.firebase_registration_token = token
                        PushKLoggerSdk.debug("PushSDK.updateToken token2: $token")
                        rewriteParams.rewriteFirebaseToken(token)
                    }
                }
            })
        PushKLoggerSdk.debug("PushSDK.updateToken finished2")
    }
}
