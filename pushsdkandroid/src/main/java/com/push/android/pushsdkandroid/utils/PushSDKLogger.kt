package com.push.android.pushsdkandroid.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.core.PushSdkSavedDataProvider
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

internal object PushSDKLogger {

    fun error(message: String) {
        if (Build.VERSION.SDK_INT >= 26) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val formatted = current.format(formatter)
            Log.e(PushSDK.TAG_LOGGING, "$formatted $message")
        } else {
            val formatted =
                SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.e(PushSDK.TAG_LOGGING, "$formatted $message")
        }
    }

    fun debug(context: Context, message: String) {
        val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)
        if (pushSdkSavedDataProvider.logLevel == PushSDK.LogLevels.PUSHSDK_LOG_LEVEL_DEBUG.name) {
            if (Build.VERSION.SDK_INT >= 26) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val formatted = current.format(formatter)
                Log.d(PushSDK.TAG_LOGGING, "$formatted $message")
            } else {
                val formatted =
                    SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
                Log.d(PushSDK.TAG_LOGGING, "$formatted $message")
            }
        }
    }

    fun debugFirebaseRemoteMessage(context: Context, remoteMessage: RemoteMessage) {
        debug(context, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            debug(context, "Message from remote: $remoteMessage")
            debug(context, "Message from remote data: ${remoteMessage.data}")
            debug(context, "Message from remote messageId: ${remoteMessage.messageId}")
            debug(context, "Message from remote messageType: ${remoteMessage.messageType}")
            debug(context, "Message from remote priority: ${remoteMessage.priority}")
            debug(context, "Message from remote rawData: ${remoteMessage.rawData}")
            debug(context, "Message from remote ttl: ${remoteMessage.ttl}")
            debug(context, "Message from remote to: ${remoteMessage.to}")
            debug(context, "Message from remote sentTime: ${remoteMessage.sentTime}")
            debug(context, "Message from remote collapseKey: ${remoteMessage.collapseKey}")
            debug(
                context,
                "Message from remote originalPriority: ${remoteMessage.originalPriority}"
            )
            debug(context, "Message from remote senderId: ${remoteMessage.senderId}")
            debug(context, "Message from remote data to string: ${remoteMessage.data}")

            debug(
                context,
                "data payload(Map<String, String> toString): " + remoteMessage.data.toString()
            )
        }
    }

    fun debugApiRequest(
        context: Context,
        requestMethod: String,
        url: URL,
        headers: Map<String, String>,
        postData: String,
        requestResponseCode: Int,
        requestResponseData: String
    ) {
        var headersString = ""
        for (header in headers) {
            headersString += "${header.key} : ${header.value}\n"
        }
        debug(
            context, "Sent '$requestMethod' request to URL: $url\n" +
                    "Headers:\n" +
                    headersString +
                    "PostData:\n" +
                    postData + "\n" +
                    "Response Code: $requestResponseCode\n" +
                    "Response Body:\n" +
                    requestResponseData
        )
    }

}