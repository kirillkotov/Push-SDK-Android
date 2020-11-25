package com.push.android.pushsdkandroid.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.add.Info
import com.push.android.pushsdkandroid.logger.PushSDKLogger
import com.push.android.pushsdkandroid.models.*
import com.push.android.pushsdkandroid.models.PushKDataApi
import com.push.android.pushsdkandroid.models.PushKDataApi2
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Communication with push rest server (REST API)
 */
internal class APIHandler {

    //class init for creation answers
    private var requestAnswerHandlerForm: RequestAnswerHandler =
        RequestAnswerHandler()

    //parameters for procedures
    private val osVersion = Info.getAndroidVersion()

    companion object {
        /**
         * Api parameters
         */
        val API_PARAMS = ApiParams()
    }

    /**
     * Creates special token for use in requests
     */
    private fun hash(sss: String): String {
        return try {
            val bytes = sss.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val resp: String = digest.fold("", { str, it -> str + "%02x".format(it) })
            PushSDKLogger.debug("Result: OK, Function: hash, Class: PushKApi, input: $sss, output: $resp")
            resp
        } catch (e: Exception) {
            PushSDKLogger.debug("Result: FAILED, Function: hash, Class: PushKApi, input: $sss, output: failed")
            "failed"
        }
    }

    /**
     * Registration POST request
     */
    fun hDeviceRegister(
        xPlatformClientAPIKey: String,
        X_Push_Session_Id: String,
        X_Push_App_Fingerprint: String,
        device_Name: String,
        device_Type: String,
        os_Type: String,
        sdk_Version: String,
        user_Pass: String,
        user_Phone: String,
        context: Context
    ): PushKDataApi2 {
        var functionNetAnswer = PushKFunAnswerRegister()
        //var functionCodeAnswer = 0

        val threadNetF1 = Thread(Runnable {
            try {
                PushSDKLogger.debug("Result: Start step1, Function: push_device_register, Class: PushKApi, xPlatformClientAPIKey: $xPlatformClientAPIKey, X_Push_Session_Id: $X_Push_Session_Id, X_Push_App_Fingerprint: $X_Push_App_Fingerprint, device_Name: $device_Name, device_Type: $device_Type, os_Type: $os_Type, sdk_Version: $sdk_Version, user_Pass: $user_Pass, user_Phone: $user_Phone")

                val message =
                    "{\"userPhone\":\"$user_Phone\",\"userPass\":\"$user_Pass\",\"osType\":\"$os_Type\",\"osVersion\":\"$osVersion\",\"deviceType\":\"$device_Type\",\"deviceName\":\"$device_Name\",\"sdkVersion\":\"$sdk_Version\"}"
                //val message = "{\"userPhone\":\"$user_Phone\",\"userPass\":\"$user_Pass\",\"osType\":\"$os_Type\",\"osVersion\":\"$os_version\",\"deviceType\":\"$device_Type\",\"deviceName\":\"$device_Name\",\"sdkVersion\":\"$sdk_Version\"}"

                PushSDKLogger.debug("Result: Start step2, Function: push_device_register, Class: PushKApi, message: $message")

                //val currentTimestamp = System.currentTimeMillis()
                val postData: ByteArray = message.toByteArray(Charset.forName("UTF-8"))
                val mURL = URL(API_PARAMS.getFullURLFor(ApiParams.ApiPaths.DEVICE_REGISTRATION))
                PushSDKLogger.debug("Requesting $mURL")
                val connectorWebPlatform = mURL.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerClientApiKey,
                    xPlatformClientAPIKey
                )
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerAppFingerprint,
                    X_Push_App_Fingerprint
                )
                connectorWebPlatform.sslSocketFactory =
                    SSLSocketFactory.getDefault() as SSLSocketFactory

                with(connectorWebPlatform) {
                    requestMethod = "POST"
                    doOutput = true
                    val wr = DataOutputStream(outputStream)

                    wr.write(postData)

                    wr.flush()

                    PushSDKLogger.debug("Result: Finished step3, Function: push_device_register, Class: PushKApi, Response Code : $responseCode")

                    //functionCodeAnswer = responseCode
                    if (responseCode == 200) {

                        BufferedReader(InputStreamReader(inputStream)).use {
                            val response = StringBuffer()

                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            }
                            it.close()

                            PushSDKLogger.debug("Result: Finished step4, Function: push_device_register, Class: PushKApi, Response : $response")

                            functionNetAnswer = requestAnswerHandlerForm.registerProcedureAnswer2(
                                responseCode.toString(),
                                response.toString(),
                                context
                            )
                        }
                    } else {
                        functionNetAnswer = requestAnswerHandlerForm.registerProcedureAnswer2(
                            responseCode.toString(),
                            "unknown",
                            context
                        )

                    }
                }
            } catch (e: Exception) {

                PushSDKLogger.debug(
                    "Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${Log.getStackTraceString(
                        e
                    )}"
                )

                functionNetAnswer = requestAnswerHandlerForm.registerProcedureAnswer2(
                    "705",
                    "unknown",
                    context
                )
            }
        })

        threadNetF1.start()
        threadNetF1.join()

        return PushKDataApi2(functionNetAnswer.code, functionNetAnswer, 0)
    }

    /**
     * POST request to revoke registration
     */
    fun hDeviceRevoke(
        dev_list: String,
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String
    ): PushKDataApi {

        var functionNetAnswer2 = String()

        val threadNetF2 = Thread(Runnable {

            try {

                PushSDKLogger.debug("Result: Start step1, Function: push_device_revoke, Class: PushKApi, dev_list: $dev_list, X_Push_Session_Id: $X_Push_Session_Id, X_Push_Auth_Token: $X_Push_Auth_Token")

                val message2 = "{\"devices\":$dev_list}"

                PushSDKLogger.debug("Result: Start step2, Function: push_device_revoke, Class: PushKApi, message2: $message2")

                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds
                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")
                val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))
                val mURL2 = URL(API_PARAMS.getFullURLFor(ApiParams.ApiPaths.DEVICE_REVOKE))

                val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerTimestamp,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.setRequestProperty(API_PARAMS.headerAuthToken, authToken)

                connectorWebPlatform.sslSocketFactory =
                    SSLSocketFactory.getDefault() as SSLSocketFactory

                with(connectorWebPlatform) {
                    requestMethod = "POST"
                    doOutput = true
                    val wr = DataOutputStream(outputStream)
                    wr.write(postData2)
                    wr.flush()

                    try {
                        BufferedReader(InputStreamReader(inputStream)).use {
                            val response = StringBuffer()

                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            }
                            it.close()
                            PushSDKLogger.debug("Response : $response")
                        }
                    } catch (e: Exception) {
                        PushSDKLogger.debug("Failed")
                    }

                    functionNetAnswer2 = responseCode.toString()
                }
            } catch (e: Exception) {
                functionNetAnswer2 = "500"
            }
        })

        threadNetF2.start()
        threadNetF2.join()

        return PushKDataApi(functionNetAnswer2.toInt(), "{}", 0)
    }

    /**
     * GET request to get message history
     */
    fun hGetMessageHistory(
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String,
        period_in_seconds: Int
    ): PushKFunAnswerGeneral {

        var functionNetAnswer3 = String()
        var functionCodeAnswer3 = 0

        val threadNetF3 = Thread(Runnable {
            try {

                //this timestamp for URL.
                val currentTimestamp1 =
                    (System.currentTimeMillis() / 1000L) - period_in_seconds // We want timestamp in seconds

                PushSDKLogger.debug("Result: val currentTimestamp1, Function: hGetMessageHistory, Class: PushKApi, currentTimestamp1: $currentTimestamp1")

                //this timestamp for token
                val currentTimestamp2 = System.currentTimeMillis() / 1000L

                PushSDKLogger.debug("Result: val currentTimestamp2, Function: hGetMessageHistory, Class: PushKApi, currentTimestamp2: $currentTimestamp2")

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                PushSDKLogger.debug("Result: val authToken, Function: hGetMessageHistory, Class: PushKApi, authToken: $authToken")


                PushSDKLogger.debug("\nSent 'GET' request to push_get_device_all with : X_Push_Session_Id : $X_Push_Session_Id; X_Push_Auth_Token : $X_Push_Auth_Token; period_in_seconds : $period_in_seconds")

                val mURL2 = URL("${API_PARAMS.getFullURLFor(ApiParams.ApiPaths.MESSAGE_HISTORY)}?startDate=${currentTimestamp1}")

                with(mURL2.openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    //doOutput = true
                    setRequestProperty("Content-Language", "en-US")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty(API_PARAMS.headerSessionId, X_Push_Session_Id)
                    setRequestProperty(API_PARAMS.headerTimestamp, currentTimestamp2.toString())
                    setRequestProperty(API_PARAMS.headerAuthToken, authToken)

                    sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

                    requestMethod = "GET"

                    PushSDKLogger.debug("Sent 'GET' request to URL : $url; Response Code : $responseCode")
                    functionCodeAnswer3 = responseCode

                    inputStream.bufferedReader().use {
                        functionNetAnswer3 = it.readLine().toString()
                        PushSDKLogger.debug("Result: val functionNetAnswer3, Function: hGetMessageHistory, Class: PushKApi, functionNetAnswer3: $functionNetAnswer3")
                    }
                }
            } catch (e: Exception) {
                PushSDKLogger.debug(
                    "Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${Log.getStackTraceString(
                        e
                    )}"
                )
                functionCodeAnswer3 = 700
                functionNetAnswer3 = "Failed"
            }
        })

        threadNetF3.start()
        threadNetF3.join()
        return PushKFunAnswerGeneral(functionCodeAnswer3, "OK", "Processed", functionNetAnswer3)

    }


    /**
     * GET request to get all registered devices
     */
    fun hGetDeviceAll(X_Push_Session_Id: String, X_Push_Auth_Token: String): PushKDataApi {

        try {

            var functionNetAnswer4 = String()
            var functionCodeAnswer4 = 0

            val threadNetF4 = Thread(Runnable {


                try {

                    val currentTimestamp2 =
                        System.currentTimeMillis() // We want timestamp in seconds

                    val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")


                    PushSDKLogger.debug("Result: Start step1, Function: push_get_device_all, Class: PushKApi, X_Push_Session_Id: $X_Push_Session_Id, X_Push_Auth_Token: $X_Push_Auth_Token, currentTimestamp2: $currentTimestamp2, auth_token: $authToken")

                    val mURL2 = URL(API_PARAMS.getFullURLFor(ApiParams.ApiPaths.GET_DEVICE_ALL))

                    with(mURL2.openConnection() as HttpsURLConnection) {
                        requestMethod = "GET"  // optional default is GET
                        //doOutput = true
                        setRequestProperty("Content-Language", "en-US")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty(API_PARAMS.headerSessionId, X_Push_Session_Id)
                        setRequestProperty(API_PARAMS.headerTimestamp, currentTimestamp2.toString())
                        setRequestProperty(API_PARAMS.headerAuthToken, authToken)

                        sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

                        PushSDKLogger.debug("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                        functionCodeAnswer4 = responseCode

                        //if (responseCode==401) { init_push.clearData() }

                        inputStream.bufferedReader().use {

                            functionNetAnswer4 = it.readLine().toString()

                            PushSDKLogger.debug("Result: Finish step2, Function: push_get_device_all, Class: PushKApi, function_net_answer4: $functionNetAnswer4")
                        }
                    }
                } catch (e: Exception) {
                    PushSDKLogger.debug(
                        "Result: Failed step3, Function: push_get_device_all, Class: PushKApi, exception: ${Log.getStackTraceString(
                            e
                        )}"
                    )


                    functionNetAnswer4 = "Failed"
                }
            })

            threadNetF4.start()
            threadNetF4.join()
            return PushKDataApi(functionCodeAnswer4, functionNetAnswer4, 0)

        } catch (e: Exception) {
            return PushKDataApi(700, "Failed", 0)
        }

    }

    /**
     * POST request to update device registration
     */
    fun hDeviceUpdate(
        X_Push_Auth_Token: String,
        X_Push_Session_Id: String,
        device_Name: String,
        device_Type: String,
        os_Type: String,
        sdk_Version: String,
        fcm_Token: String
    ): PushKDataApi {

        var functionNetAnswer5 = String()
        var functionCodeAnswer5 = 0

        val threadNetF5 = Thread(Runnable {

            try {
                val message =
                    "{\"fcmToken\": \"$fcm_Token\",\"osType\": \"$os_Type\",\"osVersion\": \"$osVersion\",\"deviceType\": \"$device_Type\",\"deviceName\": \"$device_Name\",\"sdkVersion\": \"$sdk_Version\" }"

                PushSDKLogger.debug(message)

                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                val postData: ByteArray = message.toByteArray(Charset.forName("UTF-8"))

                val mURL = URL(API_PARAMS.getFullURLFor(ApiParams.ApiPaths.DEVICE_UPDATE))

                val connectorWebPlatform = mURL.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(API_PARAMS.headerAuthToken, authToken)
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerTimestamp,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.sslSocketFactory =
                    SSLSocketFactory.getDefault() as SSLSocketFactory

                with(connectorWebPlatform) {
                    requestMethod = "POST"
                    doOutput = true

                    val wr = DataOutputStream(outputStream)

                    wr.write(postData)

                    wr.flush()

                    functionCodeAnswer5 = responseCode

                    BufferedReader(InputStreamReader(inputStream)).use {
                        val response = StringBuffer()

                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        it.close()
                        functionNetAnswer5 = response.toString()
                    }
                }

            } catch (e: Exception) {
                PushSDKLogger.debug(
                    "Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${Log.getStackTraceString(
                        e
                    )}"
                )

                functionNetAnswer5 = "Failed"
            }


        })

        threadNetF5.start()
        threadNetF5.join()

        return PushKDataApi(functionCodeAnswer5, functionNetAnswer5, 0)


    }

    /**
     * Message callback - POST request
     */
    fun hMessageCallback(
        message_id: String,
        push_answer: String,
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String
    ): PushKDataApi {

        var functionNetAnswer6 = String()
        var functionCodeAnswer6 = 0

        val threadNetF6 = Thread(Runnable {

            try {
                val message2 = "{\"messageId\": \"$message_id\", \"answer\": \"$push_answer\"}"
                PushSDKLogger.debug("Body message to push server : $message2")
                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")


                val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))

                val mURL2 = URL(API_PARAMS.getFullURLFor(ApiParams.ApiPaths.MESSAGE_CALLBACK))

                val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(
                    API_PARAMS.headerTimestamp,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.setRequestProperty(API_PARAMS.headerAuthToken, authToken)

                connectorWebPlatform.sslSocketFactory =
                    SSLSocketFactory.getDefault() as SSLSocketFactory


                with(connectorWebPlatform) {
                    requestMethod = "POST"
                    doOutput = true

                    val wr = DataOutputStream(outputStream)

                    wr.write(postData2)
                    wr.flush()
                    PushSDKLogger.debug("URL : $url")
                    PushSDKLogger.debug("Response Code : $responseCode")
                    functionCodeAnswer6 = responseCode
                    BufferedReader(InputStreamReader(inputStream)).use {
                        val response = StringBuffer()

                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        it.close()
                        PushSDKLogger.debug("Response : $response")

                        functionNetAnswer6 = response.toString()
                    }
                }

            } catch (e: Exception) {
                PushSDKLogger.debug(
                    "Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${Log.getStackTraceString(
                        e
                    )}"
                )
            }
        })

        threadNetF6.start()
        threadNetF6.join()
        return PushKDataApi(functionCodeAnswer6, functionNetAnswer6, 0)


    }

    /**
     * POST request - report message delivery
     */
    fun hMessageDr(
        message_id: String,
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String
    ): PushKDataApi {

        if (X_Push_Session_Id != "" && X_Push_Auth_Token != "" && message_id != "") {

            var functionNetAnswer7 = String()

            val threadNetF7 = Thread(Runnable {

                try {
                    val message2 = "{\"messageId\": \"$message_id\"}"

                    PushSDKLogger.debug("Body message to push server : $message2")
                    val currentTimestamp2 =
                        System.currentTimeMillis() // We want timestamp in seconds

                    PushSDKLogger.debug("Timestamp : $currentTimestamp2")

                    val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                    val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))

                    val mURL2 =
                        URL(API_PARAMS.getFullURLFor(ApiParams.ApiPaths.MESSAGE_DELIVERY_REPORT))

                    val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                    connectorWebPlatform.doOutput = true
                    connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                    connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                    connectorWebPlatform.setRequestProperty(
                        API_PARAMS.headerSessionId,
                        X_Push_Session_Id
                    )
                    connectorWebPlatform.setRequestProperty(
                        API_PARAMS.headerTimestamp,
                        currentTimestamp2.toString()
                    )
                    connectorWebPlatform.setRequestProperty(API_PARAMS.headerAuthToken, authToken)

                    connectorWebPlatform.sslSocketFactory =
                        SSLSocketFactory.getDefault() as SSLSocketFactory


                    with(connectorWebPlatform) {
                        requestMethod = "POST"
                        doOutput = true
                        val wr = DataOutputStream(outputStream)
                        wr.write(postData2)
                        wr.flush()
                        PushSDKLogger.debug("URL : $url")
                        PushSDKLogger.debug("Response Code : $responseCode")

                        BufferedReader(InputStreamReader(inputStream)).use {
                            val response = StringBuffer()

                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            }
                            it.close()
                            PushSDKLogger.debug("Response : $response")
                        }
                        functionNetAnswer7 = responseCode.toString()
                    }
                } catch (e: Exception) {
                    functionNetAnswer7 = "500"
                }
            })
            threadNetF7.start()
            threadNetF7.join()
            return PushKDataApi(functionNetAnswer7.toInt(), "{}", 0)
        } else {
            return PushKDataApi(700, "{}", 0)
        }
    }

    /**
     * Obtain the push message queue;
     * Will also broadcast an intent with all the queued message
     */
    internal fun getDevicePushMsgQueue(
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String,
        context: Context
    ): PushKDataApi {
        var functionNetAnswer2 = String()

        val threadNetF2 = Thread(Runnable {

            val pushUrlMessQueue = APIHandler.API_PARAMS.getFullURLFor(ApiParams.ApiPaths.MESSAGE_QUEUE)

            try {
                PushSDKLogger.debug("Result: Start step1, Function: push_device_mess_queue, Class: QueueProc, X_Push_Session_Id: $X_Push_Session_Id, X_Push_Auth_Token: $X_Push_Auth_Token")

                val message2 = "{}"

                PushSDKLogger.debug("Result: Start step2, Function: push_device_mess_queue, Class: QueueProc, message2: $message2")

                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds
                //val date = Date(currentTimestamp * 1000) // Timestamp must be in ms to be converted to Date

                PushSDKLogger.debug("QueueProc.pushDeviceMessQueue \"currentTimestamp2 : $currentTimestamp2\"")

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))

                val mURL2 = URL(pushUrlMessQueue)

                val urlConnectorPlatform = mURL2.openConnection() as HttpsURLConnection
                urlConnectorPlatform.doOutput = true
                urlConnectorPlatform.setRequestProperty("Content-Language", "en-US")
                urlConnectorPlatform.setRequestProperty("Content-Type", "application/json")
                urlConnectorPlatform.setRequestProperty(APIHandler.API_PARAMS.headerSessionId, X_Push_Session_Id)
                urlConnectorPlatform.setRequestProperty(APIHandler.API_PARAMS.headerTimestamp, currentTimestamp2.toString())
                urlConnectorPlatform.setRequestProperty(APIHandler.API_PARAMS.headerAuthToken, authToken)

                urlConnectorPlatform.sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

                with(urlConnectorPlatform) {
                    requestMethod = "POST"
                    doOutput = true
                    val wr = DataOutputStream(outputStream)
                    wr.write(postData2)
                    wr.flush()

                    PushSDKLogger.debug("QueueProc.pushDeviceMessQueue \"URL : $url\"")
                    PushSDKLogger.debug("QueueProc.pushDeviceMessQueue \"Response Code : $responseCode\"")
                    try {
                        BufferedReader(InputStreamReader(inputStream)).use {
                            val response = StringBuffer()
                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            }
                            it.close()

                            //Parse string here as json, then foreach -> send delivery
                            val queueMessages = Gson().fromJson(response.toString(), QueueMessages::class.java)

                            //send broadcast with queued messages
                            if (queueMessages.messages.isNotEmpty()) {
                                Intent().apply {
                                    action = PushSDK.BROADCAST_QUEUE_INTENT_ACTION
                                    putExtra(PushSDK.BROADCAST_QUEUE_EXTRA_NAME, response.toString())
                                    context.sendBroadcast(this)
                                }

                                //send delivery reports here
                                queueMessages.messages.forEach { message ->
                                    PushSDKLogger.debug("fb token: $X_Push_Session_Id")
                                    hMessageDr(
                                        message.messageId,
                                        X_Push_Session_Id,
                                        X_Push_Auth_Token
                                    )
                                    PushSDKLogger.debug("Result: Start step2, Function: processPushQueue, Class: QueueProc, message: ${message.messageId}")
                                }
                                PushSDKLogger.debug("QueueProc.pushDeviceMessQueue Response : $response")
                            }
                            else {
                                PushSDKLogger.debug("QueueProc.pushDeviceMessQueue had no queued messages")
                            }
                        }
                    } catch (e: Exception) {
                        PushSDKLogger.debug("QueueProc.pushDeviceMessQueue response: unknown Fail \n ${Log.getStackTraceString(e)}")
                    }

                    functionNetAnswer2 = responseCode.toString()
                }
            } catch (e: Exception) {
                functionNetAnswer2 = "500"
            }
        })
        threadNetF2.start()
        threadNetF2.join()
        return PushKDataApi(functionNetAnswer2.toInt(), "{}", 0)
    }
}