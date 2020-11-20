package com.push.android.pushsdkandroid.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushKFirebaseService
import com.push.android.pushsdkandroid.logger.PushSDKLogger
import com.push.android.pushsdkandroid.models.PushDataModel
import com.push.android.pushsdkandroid.models.PushKDataApi
import com.push.android.pushsdkandroid.models.QueueMessages
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
 * fixme remove the class, implement same functionality inside APIHandler
 */
@Deprecated("will be removed once redone")
internal class QueueProc2 {

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
     * Obtain the push message queue
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
                            val messages = Gson().fromJson(response.toString(), QueueMessages::class.java).messages
                            messages?.let {
                                Intent().apply {
                                    action = "com.push.android.pushsdkandroid.pushqueue"
                                    putExtra("data", messages.toString())
                                    context.sendBroadcast(this)
                                }
                            }

                            PushSDKLogger.debug("QueueProc.pushDeviceMessQueue Response : $messages")

//                            try {
//                                if (response.toString() != """{"messages":[]}""") {
//                                    Intent().apply {
//                                        action = "com.push.android.pushsdkandroid.pushqueue"
//                                        putExtra("data", response.toString())
//                                        context.sendBroadcast(this)
//                                    }
//                                }
//                            } catch (e: Exception) {
//
//                            }

//                            PushSDKLogger.debug("QueueProc.pushDeviceMessQueue Response : $response")
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