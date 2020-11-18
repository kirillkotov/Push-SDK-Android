package com.push.android.pushsdkandroid.logger

import android.os.Build
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.push.android.pushsdkandroid.PushKPushMess
import com.push.android.pushsdkandroid.PushSDK
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

    fun debug(message: String) {
        if (PushKPushMess.log_level_active == "debug") {
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

    fun debugFirebaseRemoteMessage(remoteMessage: RemoteMessage) {
        debug("From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            debug("Message from remote: $remoteMessage")
            debug("Message from remote data: ${remoteMessage.data}")
            debug("Message from remote messageId: ${remoteMessage.messageId}")
            debug("Message from remote messageType: ${remoteMessage.messageType}")
            debug("Message from remote priority: ${remoteMessage.priority}")
            debug("Message from remote rawData: ${remoteMessage.rawData}")
            debug("Message from remote ttl: ${remoteMessage.ttl}")
            debug("Message from remote to: ${remoteMessage.to}")
            debug("Message from remote sentTime: ${remoteMessage.sentTime}")
            debug("Message from remote collapseKey: ${remoteMessage.collapseKey}")
            debug("Message from remote originalPriority: ${remoteMessage.originalPriority}")
            debug("Message from remote senderId: ${remoteMessage.senderId}")
            debug("Message from remote data to string: ${remoteMessage.data}")

            debug("Message data payload: " + remoteMessage.data.toString())
        }
    }

}