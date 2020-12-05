package com.push.android.pushsdkandroid.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.utils.Info
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import com.push.android.pushsdkandroid.models.*
import com.push.android.pushsdkandroid.models.PushKDataApi
import com.push.android.pushsdkandroid.models.PushKDataApi2
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Communication with push rest server (REST API)
 */
internal class APIHandler(private val context: Context) {

    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)

    //class init for creation answers
    private var requestAnswerHandlerForm: RequestAnswerHandler =
        RequestAnswerHandler()

    //parameters for procedures
    private val osVersion = Info.getAndroidVersion()

    var headerClientApiKey = "X-Msghub-Client-API-Key"
    var headerAppFingerprint = "X-Msghub-App-Fingerprint"
    var headerSessionId = "X-Msghub-Session-Id"
    var headerTimestamp = "X-Msghub-Timestamp"
    var headerAuthToken = "X-Msghub-Auth-Token"

    //should start with slash
    var deviceUpdatePath = "/device/update"
    var deviceRegistrationPath = "/device/registration"
    var deviceRevokePath = "/device/revoke"
    var getDeviceAllPath = "/device/all"
    var messageCallbackPath = "/message/callback"
    var messageDeliveryReportPath = "/message/dr"
    var messageQueuePath = "/message/queue"
    var messageHistoryPath = "/message/history"

    /**
     * Enum of possible paths
     */
    enum class ApiPaths {
        DEVICE_UPDATE,
        DEVICE_REGISTRATION,
        DEVICE_REVOKE,
        GET_DEVICE_ALL,
        MESSAGE_CALLBACK,
        MESSAGE_DELIVERY_REPORT,
        MESSAGE_QUEUE,
        MESSAGE_HISTORY
    }

    //goes like "https://api.com/api" + "3.0" + "/device/update"
    /**
     * Get full URL path, e.g. https://example.io/api/2.3/message/dr
     * @param path which path to get full URL for
     */
    fun getFullURLFor(path: ApiPaths): String {
        return URI(
            "${pushSdkSavedDataProvider.baseApiUrl}${
                when (path) {
                    ApiPaths.DEVICE_UPDATE -> deviceUpdatePath
                    ApiPaths.DEVICE_REGISTRATION -> deviceRegistrationPath
                    ApiPaths.DEVICE_REVOKE -> deviceRevokePath
                    ApiPaths.GET_DEVICE_ALL -> getDeviceAllPath
                    ApiPaths.MESSAGE_CALLBACK -> messageCallbackPath
                    ApiPaths.MESSAGE_DELIVERY_REPORT -> messageDeliveryReportPath
                    ApiPaths.MESSAGE_QUEUE -> messageQueuePath
                    ApiPaths.MESSAGE_HISTORY -> messageHistoryPath
                }
            }"
        ).normalize().toString()
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
            PushSDKLogger.debug(context, "Result: OK, Function: hash, Class: PushKApi, input: $sss, output: $resp")
            resp
        } catch (e: Exception) {
            PushSDKLogger.debug(context, "Result: FAILED, Function: hash, Class: PushKApi, input: $sss, output: failed")
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
                PushSDKLogger.debug(context, "Result: Start step1, Function: push_device_register, Class: PushKApi, xPlatformClientAPIKey: $xPlatformClientAPIKey, X_Push_Session_Id: $X_Push_Session_Id, X_Push_App_Fingerprint: $X_Push_App_Fingerprint, device_Name: $device_Name, device_Type: $device_Type, os_Type: $os_Type, sdk_Version: $sdk_Version, user_Pass: $user_Pass, user_Phone: $user_Phone")

                val message =
                    "{\"userPhone\":\"$user_Phone\",\"userPass\":\"$user_Pass\",\"osType\":\"$os_Type\",\"osVersion\":\"$osVersion\",\"deviceType\":\"$device_Type\",\"deviceName\":\"$device_Name\",\"sdkVersion\":\"$sdk_Version\"}"
                //val message = "{\"userPhone\":\"$user_Phone\",\"userPass\":\"$user_Pass\",\"osType\":\"$os_Type\",\"osVersion\":\"$os_version\",\"deviceType\":\"$device_Type\",\"deviceName\":\"$device_Name\",\"sdkVersion\":\"$sdk_Version\"}"

                PushSDKLogger.debug(context, "Result: Start step2, Function: push_device_register, Class: PushKApi, message: $message")

                //val currentTimestamp = System.currentTimeMillis()
                val postData: ByteArray = message.toByteArray(Charset.forName("UTF-8"))
                val mURL = URL(getFullURLFor(ApiPaths.DEVICE_REGISTRATION))
                PushSDKLogger.debug(context, "Requesting $mURL")
                //val connectorWebPlatform = mURL.openConnection() as HttpsURLConnection
                val connectorWebPlatform = when (mURL.protocol) {
                    "http" -> {
                        mURL.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (mURL.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        //fixme throw error
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                        //mURL.openConnection() as HttpsURLConnection
                    }
                }

                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty(
                    headerClientApiKey,
                    xPlatformClientAPIKey
                )
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(
                    headerAppFingerprint,
                    X_Push_App_Fingerprint
                )

                with(connectorWebPlatform) {
                    requestMethod = "POST"
                    doOutput = true
                    val wr = DataOutputStream(outputStream)

                    wr.write(postData)

                    wr.flush()

                    PushSDKLogger.debug(context, "Result: Finished step3, Function: push_device_register, Class: PushKApi, Response Code : $responseCode")

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

                            PushSDKLogger.debug(context, "Result: Finished step4, Function: push_device_register, Class: PushKApi, Response : $response")

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

                PushSDKLogger.debug(context,
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

                PushSDKLogger.debug(context, "Result: Start step1, Function: push_device_revoke, Class: PushKApi, dev_list: $dev_list, X_Push_Session_Id: $X_Push_Session_Id, X_Push_Auth_Token: $X_Push_Auth_Token")

                val message2 = "{\"devices\":$dev_list}"

                PushSDKLogger.debug(context, "Result: Start step2, Function: push_device_revoke, Class: PushKApi, message2: $message2")

                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds
                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")
                val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))
                val mURL2 = URL(getFullURLFor(ApiPaths.DEVICE_REVOKE))
                PushSDKLogger.debug(context, "Requesting $mURL2")
                //val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                val connectorWebPlatform = when (mURL2.protocol) {
                    "http" -> {
                        mURL2.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (mURL2.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                    SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        //fixme throw error
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                        //mURL.openConnection() as HttpsURLConnection
                    }
                }
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(
                    headerTimestamp,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.setRequestProperty(headerAuthToken, authToken)

//                connectorWebPlatform.sslSocketFactory =
//                    SSLSocketFactory.getDefault() as SSLSocketFactory

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
                            PushSDKLogger.debug(context, "Response : $response")
                        }
                    } catch (e: Exception) {
                        PushSDKLogger.debug(context, "Failed")
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

                PushSDKLogger.debug(context, "Result: val currentTimestamp1, Function: hGetMessageHistory, Class: PushKApi, currentTimestamp1: $currentTimestamp1")

                //this timestamp for token
                val currentTimestamp2 = System.currentTimeMillis() / 1000L

                PushSDKLogger.debug(context, "Result: val currentTimestamp2, Function: hGetMessageHistory, Class: PushKApi, currentTimestamp2: $currentTimestamp2")

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                PushSDKLogger.debug(context, "Result: val authToken, Function: hGetMessageHistory, Class: PushKApi, authToken: $authToken")


                PushSDKLogger.debug(context, "\nSent 'GET' request to push_get_device_all with : X_Push_Session_Id : $X_Push_Session_Id; X_Push_Auth_Token : $X_Push_Auth_Token; period_in_seconds : $period_in_seconds")

                val mURL2 = URL("${getFullURLFor(ApiPaths.MESSAGE_HISTORY)}?startDate=${currentTimestamp1}")
                PushSDKLogger.debug(context, "Requesting $mURL2")
                val connectorWebPlatform = when (mURL2.protocol) {
                    "http" -> {
                        mURL2.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (mURL2.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                    SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        //fixme throw error
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                        //mURL.openConnection() as HttpsURLConnection
                    }
                }

                with(connectorWebPlatform) {
                    requestMethod = "GET"  // optional default is GET

                    //doOutput = true
                    setRequestProperty("Content-Language", "en-US")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty(headerSessionId, X_Push_Session_Id)
                    setRequestProperty(headerTimestamp, currentTimestamp2.toString())
                    setRequestProperty(headerAuthToken, authToken)

                    //sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

                    requestMethod = "GET"

                    PushSDKLogger.debug(context, "Sent 'GET' request to URL : $url; Response Code : $responseCode")
                    functionCodeAnswer3 = responseCode

                    inputStream.bufferedReader().use {
                        functionNetAnswer3 = it.readLine().toString()
                        PushSDKLogger.debug(context, "Result: val functionNetAnswer3, Function: hGetMessageHistory, Class: PushKApi, functionNetAnswer3: $functionNetAnswer3")
                    }
                }
            } catch (e: Exception) {
                PushSDKLogger.debug(context,
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


                    PushSDKLogger.debug(context, "Result: Start step1, Function: push_get_device_all, Class: PushKApi, X_Push_Session_Id: $X_Push_Session_Id, X_Push_Auth_Token: $X_Push_Auth_Token, currentTimestamp2: $currentTimestamp2, auth_token: $authToken")

                    val mURL2 = URL(getFullURLFor(ApiPaths.GET_DEVICE_ALL))
                    PushSDKLogger.debug(context, "Requesting $mURL2")

                    val connectorWebPlatform = when (mURL2.protocol) {
                        "http" -> {
                            mURL2.openConnection() as HttpURLConnection
                        }
                        "https" -> {
                            (mURL2.openConnection() as HttpsURLConnection).also {
                                it.sslSocketFactory =
                                        SSLSocketFactory.getDefault() as SSLSocketFactory
                            }
                        }
                        else -> {
                            //fixme throw error
                            PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                            throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                            //mURL.openConnection() as HttpsURLConnection
                        }
                    }

                    with(connectorWebPlatform) {
                        requestMethod = "GET"  // optional default is GET
                        //doOutput = true
                        setRequestProperty("Content-Language", "en-US")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty(headerSessionId, X_Push_Session_Id)
                        setRequestProperty(headerTimestamp, currentTimestamp2.toString())
                        setRequestProperty(headerAuthToken, authToken)

                        //sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

                        PushSDKLogger.debug(context, "\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                        functionCodeAnswer4 = responseCode

                        //if (responseCode==401) { init_push.clearData() }

                        inputStream.bufferedReader().use {

                            functionNetAnswer4 = it.readLine().toString()

                            PushSDKLogger.debug(context, "Result: Finish step2, Function: push_get_device_all, Class: PushKApi, function_net_answer4: $functionNetAnswer4")
                        }
                    }
                } catch (e: Exception) {
                    PushSDKLogger.debug(context,
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

                PushSDKLogger.debug(context, message)

                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                val postData: ByteArray = message.toByteArray(Charset.forName("UTF-8"))

                val mURL = URL(getFullURLFor(ApiPaths.DEVICE_UPDATE))
                PushSDKLogger.debug(context, "Requesting $mURL")

                //val connectorWebPlatform = mURL.openConnection() as HttpsURLConnection
                val connectorWebPlatform = when (mURL.protocol) {
                    "http" -> {
                        mURL.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (mURL.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                    SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        //fixme throw error
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                        //mURL.openConnection() as HttpsURLConnection
                    }
                }
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(headerAuthToken, authToken)
                connectorWebPlatform.setRequestProperty(
                    headerTimestamp,
                    currentTimestamp2.toString()
                )
//                connectorWebPlatform.sslSocketFactory =
//                    SSLSocketFactory.getDefault() as SSLSocketFactory

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
                PushSDKLogger.debug(context,
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
                PushSDKLogger.debug(context, "Body message to push server : $message2")
                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")


                val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))

                val mURL2 = URL(getFullURLFor(ApiPaths.MESSAGE_CALLBACK))
                PushSDKLogger.debug(context, "Requesting $mURL2")

                //val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                val connectorWebPlatform = when (mURL2.protocol) {
                    "http" -> {
                        mURL2.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (mURL2.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                    SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        //fixme throw error
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                        //mURL.openConnection() as HttpsURLConnection
                    }
                }
                connectorWebPlatform.doOutput = true
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                connectorWebPlatform.setRequestProperty(
                    headerSessionId,
                    X_Push_Session_Id
                )
                connectorWebPlatform.setRequestProperty(
                    headerTimestamp,
                    currentTimestamp2.toString()
                )
                connectorWebPlatform.setRequestProperty(headerAuthToken, authToken)

//                connectorWebPlatform.sslSocketFactory =
//                    SSLSocketFactory.getDefault() as SSLSocketFactory


                with(connectorWebPlatform) {
                    requestMethod = "POST"
                    doOutput = true

                    val wr = DataOutputStream(outputStream)

                    wr.write(postData2)
                    wr.flush()
                    PushSDKLogger.debug(context, "URL : $url")
                    PushSDKLogger.debug(context, "Response Code : $responseCode")
                    functionCodeAnswer6 = responseCode
                    BufferedReader(InputStreamReader(inputStream)).use {
                        val response = StringBuffer()

                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        it.close()
                        PushSDKLogger.debug(context, "Response : $response")

                        functionNetAnswer6 = response.toString()
                    }
                }

            } catch (e: Exception) {
                PushSDKLogger.debug(context,
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

                    PushSDKLogger.debug(context, "Body message to push server : $message2")
                    val currentTimestamp2 =
                        System.currentTimeMillis() // We want timestamp in seconds

                    PushSDKLogger.debug(context, "Timestamp : $currentTimestamp2")

                    val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                    val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))

                    val mURL2 =
                        URL(getFullURLFor(ApiPaths.MESSAGE_DELIVERY_REPORT))
                    PushSDKLogger.debug(context, "Requesting $mURL2")

                    //val connectorWebPlatform = mURL2.openConnection() as HttpsURLConnection
                    val connectorWebPlatform = when (mURL2.protocol) {
                        "http" -> {
                            mURL2.openConnection() as HttpURLConnection
                        }
                        "https" -> {
                            (mURL2.openConnection() as HttpsURLConnection).also {
                                it.sslSocketFactory =
                                        SSLSocketFactory.getDefault() as SSLSocketFactory
                            }
                        }
                        else -> {
                            //fixme throw error
                            PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                            throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                            //mURL.openConnection() as HttpsURLConnection
                        }
                    }
                    connectorWebPlatform.doOutput = true
                    connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                    connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                    connectorWebPlatform.setRequestProperty(
                        headerSessionId,
                        X_Push_Session_Id
                    )
                    connectorWebPlatform.setRequestProperty(
                        headerTimestamp,
                        currentTimestamp2.toString()
                    )
                    connectorWebPlatform.setRequestProperty(headerAuthToken, authToken)

//                    connectorWebPlatform.sslSocketFactory =
//                        SSLSocketFactory.getDefault() as SSLSocketFactory


                    with(connectorWebPlatform) {
                        requestMethod = "POST"
                        doOutput = true
                        val wr = DataOutputStream(outputStream)
                        wr.write(postData2)
                        wr.flush()
                        PushSDKLogger.debug(context, "URL : $url")
                        PushSDKLogger.debug(context, "Response Code : $responseCode")

                        BufferedReader(InputStreamReader(inputStream)).use {
                            val response = StringBuffer()

                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            }
                            it.close()
                            PushSDKLogger.debug(context, "Response : $response")
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

            val pushUrlMessQueue = getFullURLFor(ApiPaths.MESSAGE_QUEUE)
            PushSDKLogger.debug(context, "Requesting $pushUrlMessQueue")

            try {
                PushSDKLogger.debug(context, "Result: Start step1, Function: push_device_mess_queue, Class: QueueProc, X_Push_Session_Id: $X_Push_Session_Id, X_Push_Auth_Token: $X_Push_Auth_Token")

                val message2 = "{}"

                PushSDKLogger.debug(context, "Result: Start step2, Function: push_device_mess_queue, Class: QueueProc, message2: $message2")

                val currentTimestamp2 = System.currentTimeMillis() // We want timestamp in seconds
                //val date = Date(currentTimestamp * 1000) // Timestamp must be in ms to be converted to Date

                PushSDKLogger.debug(context, "QueueProc.pushDeviceMessQueue \"currentTimestamp2 : $currentTimestamp2\"")

                val authToken = hash("$X_Push_Auth_Token:$currentTimestamp2")

                val postData2: ByteArray = message2.toByteArray(Charset.forName("UTF-8"))

                val mURL2 = URL(pushUrlMessQueue)

                //val urlConnectorPlatform = mURL2.openConnection() as HttpsURLConnection
                val urlConnectorPlatform = when (mURL2.protocol) {
                    "http" -> {
                        mURL2.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (mURL2.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                    SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        //fixme throw error
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                        //mURL.openConnection() as HttpsURLConnection
                    }
                }
                urlConnectorPlatform.doOutput = true
                urlConnectorPlatform.setRequestProperty("Content-Language", "en-US")
                urlConnectorPlatform.setRequestProperty("Content-Type", "application/json")
                urlConnectorPlatform.setRequestProperty(headerSessionId, X_Push_Session_Id)
                urlConnectorPlatform.setRequestProperty(headerTimestamp, currentTimestamp2.toString())
                urlConnectorPlatform.setRequestProperty(headerAuthToken, authToken)

                //urlConnectorPlatform.sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

                with(urlConnectorPlatform) {
                    requestMethod = "POST"
                    doOutput = true
                    val wr = DataOutputStream(outputStream)
                    wr.write(postData2)
                    wr.flush()

                    PushSDKLogger.debug(context, "QueueProc.pushDeviceMessQueue \"URL : $url\"")
                    PushSDKLogger.debug(context, "QueueProc.pushDeviceMessQueue \"Response Code : $responseCode\"")
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
                                    PushSDKLogger.debug(context, "fb token: $X_Push_Session_Id")
                                    hMessageDr(
                                        message.messageId,
                                        X_Push_Session_Id,
                                        X_Push_Auth_Token
                                    )
                                    PushSDKLogger.debug(context, "Result: Start step2, Function: processPushQueue, Class: QueueProc, message: ${message.messageId}")
                                }
                                PushSDKLogger.debug(context, "QueueProc.pushDeviceMessQueue Response : $response")
                            }
                            else {
                                PushSDKLogger.debug(context, "QueueProc.pushDeviceMessQueue had no queued messages")
                            }
                        }
                    } catch (e: Exception) {
                        PushSDKLogger.debug(context, "QueueProc.pushDeviceMessQueue response: unknown Fail \n ${Log.getStackTraceString(e)}")
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