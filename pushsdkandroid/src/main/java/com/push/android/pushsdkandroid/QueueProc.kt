package com.push.android.pushsdkandroid

import android.content.Context
import android.content.Intent
import com.push.android.pushsdkandroid.core.APIHandler
import com.push.android.pushsdkandroid.logger.PushSDKLogger
import com.push.android.pushsdkandroid.models.PushKDataApi
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Queue handling
 */
internal class QueueProc {

    /**
     * Creates special token for use in requests
     */
    private fun hash(sss: String): String {
        return try {
            val bytes = sss.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val resp: String = digest.fold("", { str, it -> str + "%02x".format(it) })
            PushSDKLogger.debug("Result: OK, Function: hash, Class: QueueProc, input: $sss, output: $resp")
            resp
        } catch (e: Exception) {
            PushSDKLogger.debug("Result: FAILED, Function: hash, Class: QueueProc, input: $sss, output: failed")
            "failed"
        }
    }

    /**
     * Process the push queue
     */
    private fun processPushQueue(
        queue: String,
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String
    ) {
        val apiPush = APIHandler()

        @Serializable
        data class Empty(
            @Optional
            val url: String? = null,
            @Optional
            val button: String? = null,
            @Optional
            val text: String? = null,
            @Optional
            val messageId: String? = null
        )

        @Serializable
        data class QueryList(
            @SerialName("phone")
            val phone: String,
            @SerialName("messageId")
            val messageId: String,
            @SerialName("title")
            val title: String,
            @SerialName("body")
            val body: String,
            @SerialName("image")
            val image: Empty?,
            @SerialName("button")
            val button: Empty?,
            @SerialName("partner")
            val partner: String,
            @SerialName("time")
            val time: String
        )

        @Serializable
        data class ParentQu(
            @Optional
            val messages: List<QueryList>
        )
        val parent = JSON.parse(ParentQu.serializer(), queue)
        if (parent.messages.isNotEmpty()) {
            val list = parent.messages
            Thread.sleep(2000)
            //initPush_params.push_init3()
            list.forEach {
                PushSDKLogger.debug("fb token: $X_Push_Session_Id")
                apiPush.hMessageDr(it.messageId, X_Push_Session_Id, X_Push_Auth_Token)
                PushSDKLogger.debug("Result: Start step2, Function: processPushQueue, Class: QueueProc, message: ${it.messageId}")
            }
        }
    }


    /**
     * Obtain the push message queue
     */
    internal fun pushDeviceMessQueue(
        X_Push_Session_Id: String,
        X_Push_Auth_Token: String,
        context: Context
    ): PushKDataApi {
        var functionNetAnswer2 = String()

        val threadNetF2 = Thread(Runnable {

            val pushUrlMessQueue = APIHandler.API_PARAMS.messageQueuePath

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

                            try {
                                if (response.toString() != """{"messages":[]}""") {
                                    PushKPushMess.message = response.toString()
                                    val intent = Intent()
                                    intent.action = "com.push.android.pushsdkandroid.Push"
                                    context.sendBroadcast(intent)
                                }
                            } catch (e: Exception) {
                                PushKPushMess.message = ""
                            }

                            processPushQueue(
                                response.toString(),
                                X_Push_Session_Id,
                                X_Push_Auth_Token
                            )
                            PushSDKLogger.debug("QueueProc.pushDeviceMessQueue Response : $response")
                        }
                    } catch (e: Exception) {
                        PushSDKLogger.debug("QueueProc.pushDeviceMessQueue response: unknown Fail")
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