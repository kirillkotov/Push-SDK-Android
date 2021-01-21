package com.push.android.pushsdkandroid.core

import android.content.Context
import com.push.android.pushsdkandroid.utils.Info
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import com.push.android.pushsdkandroid.models.PushKDataApi
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Communication with push rest server (REST API)
 * Will return only server API's response codes
 */
internal class APIHandler(private val context: Context) {

    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)

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

    enum class SupportedRestMethods {
        GET,
        POST
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
            PushSDKLogger.debug(context, "hashing successful, input: $sss, output: $resp")
            resp
        } catch (e: Exception) {
            val output = "failed"
            PushSDKLogger.debug(context, "hashing error, input: $sss, output: $output")
            output
        }
    }

    /**
     * Make an API request
     *
     * @throws IllegalArgumentException when supplied with wrong URL protocol or wrong REST method
     */
    private fun makeRequest(
        headers: Map<String, String>,
        method: SupportedRestMethods,
        url: URL,
        postData: String = ""
    ): PushKDataApi {
        PushSDKLogger.debug(context, "calling makeRequest() with params:\n" +
                "headers $headers\n" +
                "method $method\n" +
                "url $url\n" +
                "postData $postData")

        var requestResponseData = String()
        var requestResponseCode = 0
        val threadNetF3 = Thread(Runnable {
            try {
                //Build connection
                val connectorWebPlatform = when (url.protocol) {
                    "http" -> {
                        url.openConnection() as HttpURLConnection
                    }
                    "https" -> {
                        (url.openConnection() as HttpsURLConnection).also {
                            it.sslSocketFactory =
                                SSLSocketFactory.getDefault() as SSLSocketFactory
                        }
                    }
                    else -> {
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                    }
                }
                //set headers
                connectorWebPlatform.setRequestProperty("Content-Language", "en-US")
                connectorWebPlatform.setRequestProperty("Content-Type", "application/json")
                for (header in headers) {
                    connectorWebPlatform.setRequestProperty(header.key, header.value)
                }
                //request logic
                with(connectorWebPlatform) {
                    requestMethod = method.name  //default is GET
                    when (method) {
                        SupportedRestMethods.GET -> {
                            requestResponseCode = responseCode
                            inputStream.bufferedReader().use {
                                requestResponseData = it.readLine().toString()
                            }
                            PushSDKLogger.debugApiRequest(
                                context,
                                requestMethod,
                                url,
                                headers,
                                postData,
                                requestResponseCode,
                                requestResponseData
                            )
                        }
                        SupportedRestMethods.POST -> {
                            doOutput = true
                            val wr = DataOutputStream(outputStream)
                            val postData2: ByteArray =
                                postData.toByteArray(Charset.forName("UTF-8"))
                            wr.write(postData2)
                            wr.flush()
                            wr.close()
                            requestResponseCode = responseCode
                            inputStream.bufferedReader().use {
                                val response = StringBuffer()
                                var inputLine = it.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = it.readLine()
                                }
                                requestResponseData = response.toString()
                            }
                            PushSDKLogger.debugApiRequest(
                                context,
                                requestMethod,
                                url,
                                headers,
                                postData,
                                requestResponseCode,
                                requestResponseData
                            )
                        }
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        })
        threadNetF3.start()
        threadNetF3.join()
        val answer = PushKDataApi(requestResponseCode, requestResponseData, 0)
        PushSDKLogger.debug(context, "makeRequest() [($method) $url]\nReturning: $answer")
        return answer
    }

    //GET requests

    /**
     * GET request to get message history
     */
    fun hGetMessageHistory(
        sessionId: String,
        authToken: String,
        periodInSeconds: Int
    ): PushKDataApi {
        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.GET,
            URL("${getFullURLFor(ApiPaths.MESSAGE_HISTORY)}?startDate=${currentTimeSeconds - periodInSeconds}"),
            ""
        )
    }


    /**
     * GET request to get all registered devices
     */
    fun hGetDeviceAll(sessionId: String, authToken: String): PushKDataApi {
        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.GET,
            URL(getFullURLFor(ApiPaths.GET_DEVICE_ALL)),
            ""
        )
    }

    //POST requests

    /**
     * POST request to revoke registration
     */
    fun hDeviceRevoke(
        deviceList: String,
        sessionId: String,
        authToken: String
    ): PushKDataApi {
        val message = "{\"devices\":$deviceList}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.DEVICE_REVOKE)),
            message
        )
    }

    /**
     * POST request to update device registration
     */
    fun hDeviceUpdate(
        authToken: String,
        sessionId: String,
        deviceName: String,
        deviceType: String,
        osType: String,
        sdkVersion: String,
        fcmToken: String
    ): PushKDataApi {
        val message =
            "{\"fcmToken\": \"$fcmToken\",\"osType\": \"$osType\",\"osVersion\": \"$osVersion\",\"deviceType\": \"$deviceType\",\"deviceName\": \"$deviceName\",\"sdkVersion\": \"$sdkVersion\" }"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.DEVICE_UPDATE)),
            message
        )
    }

    /**
     * Message callback - POST request
     */
    fun hMessageCallback(
        messageId: String,
        pushAnswer: String,
        sessionId: String,
        authToken: String
    ): PushKDataApi {
        val message = "{\"messageId\": \"$messageId\", \"answer\": \"$pushAnswer\"}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.MESSAGE_CALLBACK)),
            message
        )
    }

    /**
     * POST request - report message delivery
     *
     * @throws IllegalArgumentException when one of the arguments is empty
     */
    fun hMessageDr(
        messageId: String,
        sessionId: String,
        authToken: String
    ): PushKDataApi {
        val message = "{\"messageId\": \"$messageId\"}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.MESSAGE_DELIVERY_REPORT)),
            message
        )
    }

    /**
     * Obtain the push message queue;
     * Will also broadcast an intent with all the queued message
     */
    internal fun getDevicePushMsgQueue(
        sessionId: String,
        authToken: String
    ): PushKDataApi {
        val message = "{}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.MESSAGE_QUEUE)),
            message
        )
    }

    /**
     * Registration POST request
     */
    fun hDeviceRegister(
        apiKey: String,
        sessionId: String,
        appFingerprint: String,
        deviceName: String,
        deviceType: String,
        osType: String,
        sdkVersion: String,
        userPassword: String,
        userPhone: String
    ): PushKDataApi {
        val message =
            "{\"userPhone\":\"$userPhone\",\"userPass\":\"$userPassword\",\"osType\":\"$osType\",\"osVersion\":\"$osVersion\",\"deviceType\":\"$deviceType\",\"deviceName\":\"$deviceName\",\"sdkVersion\":\"$sdkVersion\"}"

        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerClientApiKey] = apiKey
        headers[headerAppFingerprint] = appFingerprint
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.DEVICE_REGISTRATION)),
            message
        )
    }
}