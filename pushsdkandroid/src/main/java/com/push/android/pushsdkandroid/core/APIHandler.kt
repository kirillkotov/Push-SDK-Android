package com.push.android.pushsdkandroid.core

import android.content.Context
import com.push.android.pushsdkandroid.add.RequestAnswerHandler
import com.push.android.pushsdkandroid.add.GetInfo
import com.push.android.pushsdkandroid.logger.PushSDKLogger
import com.push.android.pushsdkandroid.models.PushKDataApi
import com.push.android.pushsdkandroid.models.PushKDataApi2
import com.push.android.pushsdkandroid.models.PushKFunAnswerGeneral
import com.push.android.pushsdkandroid.models.PushKFunAnswerRegister
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
    private var requestAnswerHandlerForm: RequestAnswerHandler = RequestAnswerHandler()
    private var osVersionClass: GetInfo = GetInfo()

    //parameters for procedures
    private val osVersion: String = osVersionClass.getAndroidVersion()

    /**
     * Headers and API URLs
     */
    companion object {
        const val HEADER_CLIENT_API_KEY = "X-Hyber-Client-API-Key"
        const val HEADER_APP_FINGERPRINT = "X-Hyber-App-Fingerprint"
        const val HEADER_SESSION_ID = "X-Hyber-Session-Id"
        const val HEADER_TIMESTAMP = "X-Hyber-Timestamp"
        const val HEADER_AUTH_TOKEN = "X-Hyber-Auth-Token"

        const val BASE_URL = "https://test-push.hyber.im/api/2.3/"

        const val API_URL_DEVICE_UPDATE = "device/update"
        const val API_URL_DEVICE_REGISTRATION = "device/registration"
        const val API_URL_DEVICE_REVOKE = "device/revoke"
        const val API_URL_GET_DEVICE_ALL = "device/all"
        const val API_URL_MESSAGE_CALLBACK = "message/callback"
        const val API_URL_MESSAGE_DELIVERY_REPORT = "message/dr"
        const val API_URL_MESSAGE_QUEUE = "message/queue"
        const val API_URL_MESSAGE_HISTORY = "message/history?startDate="
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
        var functionCodeAnswer = 0

        val threadNetF1 = Thread(Runnable {
            try {
                PushSDKLogger.debug("Result: Start step1, Function: push_device_register, Class: PushKApi, xPlatformClientAPIKey: $xPlatformClientAPIKey, X_Push_Session_Id: $X_Push_Session_Id, X_Push_App_Fingerprint: $X_Push_App_Fingerprint, device_Name: $device_Name, device_Type: $device_Type, os_Type: $os_Type, sdk_Version: $sdk_Version, user_Pass: $user_Pass, user_Phone: $user_Phone")

                val message =
                    "{\"userPhone\":\"$user_Phone\",\"userPass\":\"$user_Pass\",\"osType\":\"$os_Type\",\"osVersion\":\"$osVersion\",\"deviceType\":\"$device_Type\",\"deviceName\":\"$device_Name\",\"sdkVersion\":\"$sdk_Version\"}"
                //val message = "{\"userPhone\":\"$user_Phone\",\"userPass\":\"$user_Pass\",\"osType\":\"$os_Type\",\"osVersion\":\"$os_version\",\"deviceType\":\"$device_Type\",\"deviceName\":\"$device_Name\",\"sdkVersion\":\"$sdk_Version\"}"

                PushSDKLogger.debug("Result: Start step2, Function: push_device_register, Class: PushKApi, message: $message")

                val currentTimestamp = System.currentTimeMillis()
                val postData: ByteArray = message.toByteArray(Charset.forName("UTF-8"))
                val mURL = URL("$BASE_URL$API_URL_DEVICE_REGISTRATION")
                val connectorWebPlatform = mURL.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty(
                    HEADER_CLIENT_API_KEY,
                    xPlatformClientAPIKey
                )
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(HEADER_SESSION_ID, X_Push_Session_Id)
                connectorWebPlatform.setRequestProperty(
                    HEADER_APP_FINGERPRINT,
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

                    functionCodeAnswer = responseCode
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

                PushSDKLogger.debug("Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${e.stackTrace}")

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
                val mURL2 = URL("$BASE_URL$API_URL_DEVICE_REVOKE")

                val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(HEADER_SESSION_ID, X_Push_Session_Id)
                connectorWebPlatform.setRequestProperty(
                    HEADER_TIMESTAMP,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.setRequestProperty(HEADER_AUTH_TOKEN, authToken)

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

                val mURL2 = URL("$BASE_URL$API_URL_MESSAGE_HISTORY${currentTimestamp1}")

                with(mURL2.openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    //doOutput = true
                    setRequestProperty("Content-Language", "en-US")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty(HEADER_SESSION_ID, X_Push_Session_Id)
                    setRequestProperty(HEADER_TIMESTAMP, currentTimestamp2.toString())
                    setRequestProperty(HEADER_AUTH_TOKEN, authToken)

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
                PushSDKLogger.debug("Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${e.stackTrace}")
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

                    val mURL2 = URL("$BASE_URL$API_URL_GET_DEVICE_ALL")

                    with(mURL2.openConnection() as HttpsURLConnection) {
                        requestMethod = "GET"  // optional default is GET
                        //doOutput = true
                        setRequestProperty("Content-Language", "en-US")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty(HEADER_SESSION_ID, X_Push_Session_Id)
                        setRequestProperty(HEADER_TIMESTAMP, currentTimestamp2.toString())
                        setRequestProperty(HEADER_AUTH_TOKEN, authToken)

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
                    PushSDKLogger.debug("Result: Failed step3, Function: push_get_device_all, Class: PushKApi, exception: $e")


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

                val mURL = URL("$BASE_URL$API_URL_DEVICE_UPDATE")

                val connectorWebPlatform = mURL.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(HEADER_SESSION_ID, X_Push_Session_Id)
                connectorWebPlatform.setRequestProperty(HEADER_AUTH_TOKEN, authToken)
                connectorWebPlatform.setRequestProperty(
                    HEADER_TIMESTAMP,
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
                PushSDKLogger.debug("Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${e.stackTrace}")

                functionNetAnswer5 = "Failed"
            }


        })

        threadNetF5.start()
        threadNetF5.join()

        return PushKDataApi(functionCodeAnswer5, functionNetAnswer5, 0)


    }

    /**
     * Message callpack - POST request
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

                val mURL2 = URL("$BASE_URL$API_URL_MESSAGE_CALLBACK")

                val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(HEADER_SESSION_ID, X_Push_Session_Id)
                connectorWebPlatform.setRequestProperty(
                    HEADER_TIMESTAMP,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.setRequestProperty(HEADER_AUTH_TOKEN, authToken)

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
                PushSDKLogger.debug("Result: Failed step5, Function: push_device_register, Class: PushKApi, exception: ${e.stackTrace}")
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

                    val mURL2 = URL("$BASE_URL$API_URL_MESSAGE_DELIVERY_REPORT")

                    val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                    connectorWebPlatform.doOutput = true
                    connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                    connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                    connectorWebPlatform.setRequestProperty(
                        HEADER_SESSION_ID,
                        X_Push_Session_Id
                    )
                    connectorWebPlatform.setRequestProperty(
                        HEADER_TIMESTAMP,
                        currentTimestamp2.toString()
                    )
                    connectorWebPlatform.setRequestProperty(HEADER_AUTH_TOKEN, authToken)

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
}