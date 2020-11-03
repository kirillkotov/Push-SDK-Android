/*
Service for Firebase Push notification messaging
 */

package com.push.android.pushsdkandroid

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.push.android.pushsdkandroid.add.GetInfo
import com.push.android.pushsdkandroid.add.PushParsing
import com.push.android.pushsdkandroid.add.RewriteParams
import com.push.android.pushsdkandroid.core.PushKApi
import com.push.android.pushsdkandroid.core.PushSdkParameters
import com.push.android.pushsdkandroid.core.PushKPublicParams
import com.push.android.pushsdkandroid.logger.PushKLoggerSdk

@SuppressLint("Registered")
internal class PushKFirebaseService : FirebaseMessagingService() {

    private var api: PushKApi = PushKApi()
    private var parsing: PushParsing = PushParsing()
    private var getDevInform: GetInfo = GetInfo()

    override fun onCreate() {
        super.onCreate()
        PushKLoggerSdk.debug("PushFirebaseService.onCreate : MyService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        PushKLoggerSdk.debug("PushFirebaseService.onDestroy : MyService onDestroy")
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        PushKLoggerSdk.debug("PushFirebaseService.onNewToken : Result: Start step1, Function: onNewToken, Class: PushFirebaseService, new_token: $s")

        try {
            if (s != "") {
                val pushUpdateParams = RewriteParams(applicationContext)
                pushUpdateParams.rewriteFirebaseToken(s)
                PushKLoggerSdk.debug("PushFirebaseService.onNewToken : local update: success")
            }
        } catch (e: Exception) {
            PushKLoggerSdk.debug("PushFirebaseService.onNewToken : local update: unknown error")
        }

        try {
            if (PushKDatabase.push_k_registration_token != "" && PushKDatabase.firebase_registration_token != "") {

                val localPhoneInfoNewToken = getDevInform.getPhoneType(applicationContext)
                PushKLoggerSdk.debug("PushFirebaseService.onNewToken : localPhoneInfoNewToken: $localPhoneInfoNewToken")
                val answerPlatform = api.hDeviceUpdate(
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.firebase_registration_token,
                    PushSdkParameters.push_k_deviceName,
                    localPhoneInfoNewToken,
                    PushSdkParameters.push_k_osType,
                    PushSdkParameters.sdkVersion,
                    s
                )
                PushKLoggerSdk.debug("PushFirebaseService.onNewToken : update success $answerPlatform")
            } else {
                PushKLoggerSdk.debug("PushFirebaseService.onNewToken : update: failed")
            }

        } catch (e: Exception) {
            PushKLoggerSdk.debug("PushFirebaseService.onNewToken : update: unknown error")
        }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        PushKLoggerSdk.debug("PushFirebaseService.onMessageReceived : started")

        super.onMessageReceived(remoteMessage)

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        PushKLoggerSdk.debug("From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            try {
                PushKLoggerSdk.debug("Message from remote: $remoteMessage")
                PushKLoggerSdk.debug("Message from remote data: ${remoteMessage.data}")
                PushKLoggerSdk.debug("Message from remote messageId: ${remoteMessage.messageId}")
                PushKLoggerSdk.debug("Message from remote messageType: ${remoteMessage.messageType}")
                PushKLoggerSdk.debug("Message from remote priority: ${remoteMessage.priority}")
                PushKLoggerSdk.debug("Message from remote rawData: ${remoteMessage.rawData}")
                PushKLoggerSdk.debug("Message from remote ttl: ${remoteMessage.ttl}")
                PushKLoggerSdk.debug("Message from remote to: ${remoteMessage.to}")
                PushKLoggerSdk.debug("Message from remote sentTime: ${remoteMessage.sentTime}")
                PushKLoggerSdk.debug("Message from remote collapseKey: ${remoteMessage.collapseKey}")
                PushKLoggerSdk.debug("Message from remote originalPriority: ${remoteMessage.originalPriority}")
                PushKLoggerSdk.debug("Message from remote senderId: ${remoteMessage.senderId}")
                PushKLoggerSdk.debug("Message from remote data to string: ${remoteMessage.data}")
                if (PushKDatabase.firebase_registration_token != "" && PushKDatabase.push_k_registration_token != "") {
                    val pushAnswer = api.hMessageDr(
                        parsing.parseMessageId(remoteMessage.data.toString()),
                        PushKDatabase.firebase_registration_token,
                        PushKDatabase.push_k_registration_token
                    )
                    PushKLoggerSdk.debug("From Message Delivery Report: $pushAnswer")
                    PushKLoggerSdk.debug("delivery report success: messid ${remoteMessage.messageId.toString()}, token: ${PushKDatabase.firebase_registration_token}, push_k_registration_token: ${PushKDatabase.push_k_registration_token}")
                } else {
                    PushKLoggerSdk.debug("delivery report failed: messid ${remoteMessage.messageId.toString()}, token: ${PushKDatabase.firebase_registration_token}, push_k_registration_token: ${PushKDatabase.push_k_registration_token}")
                }
            } catch (e: Exception) {
                PushKLoggerSdk.debug("onMessageReceived: failed")
            }

            try {
                var dataMessFromFirebase = remoteMessage.data.toString()
                if (dataMessFromFirebase.contains("\\/")) {
                    dataMessFromFirebase = dataMessFromFirebase.replace("\\/", "/")
                }
                PushKPushMess.message = dataMessFromFirebase
                val intent = Intent()
                intent.action = "com.push.android.pushsdkandroid.Push"
                sendBroadcast(intent)
            } catch (e: Exception) {
                PushKPushMess.message = ""
            }
            PushKLoggerSdk.debug("Message data payload: " + remoteMessage.data.toString())
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            PushKLoggerSdk.debug("Message Notification Body: " + remoteMessage.notification!!.body!!)

            try {
                when (PushKPushMess.push_message_style) {
                    0 -> sendNotification(remoteMessage)
                    1 -> {
                        sendNotificationImageType1(remoteMessage, parsing.parseImageUrl(remoteMessage.data.toString()))
                    }
                    else -> {
                        sendNotification(remoteMessage)
                    }
                }
            } catch (ee: Exception) {
                PushKLoggerSdk.debug("Notification payload sendNotification: Unknown Fail")
            }
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val notificationObject = PushKPublicParams()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            137923, // ID of notification
            notificationObject.notificationBuilder(
                applicationContext,
                remoteMessage.notification!!.body.toString()
            ).build()
        )
    }

    private fun sendNotificationImageType1(remoteMessage: RemoteMessage, imageUrl: String) {
        val notificationObject = PushKPublicParams()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            137923, // ID of notification
            notificationObject.notificationBuilder(
                applicationContext,
                remoteMessage.notification!!.body.toString(),
                imageUrl
            ).build()
        )
    }
}

