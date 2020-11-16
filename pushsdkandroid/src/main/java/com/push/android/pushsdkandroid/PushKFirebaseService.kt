/*
Service for Firebase Push notification messaging
 */

package com.push.android.pushsdkandroid

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.push.android.pushsdkandroid.add.GetInfo
import com.push.android.pushsdkandroid.add.PushParsing
import com.push.android.pushsdkandroid.add.RewriteParams
import com.push.android.pushsdkandroid.core.PushKApi
import com.push.android.pushsdkandroid.core.PushSdkParameters
import com.push.android.pushsdkandroid.logger.PushKLoggerSdk
import com.push.android.pushsdkandroid.models.PushDataModel
import java.net.URL
import java.util.*
import kotlin.random.Random

/**
 * A "FirebaseMessagingService based" service for handling push messages
 *
 * @constructor A "FirebaseMessagingService based" service for handling push messages
 *
 * @param PARAM_NOTIFICATIONS_SUMMARY_TITLE_AND_TEXT Summary notification title and text <title, text>,
 * used for displaying a "summary notification", which serves as a root notification for other notifications
 * notifications will not be bundled(grouped) if null; Ignored if api level is below android 7
 *
 * @param PARAM_NOTIFICATIONS_ICON_RESOURCE_ID Notification small icon
 *
 * @param PARAM_NOTIFICATIONS_STYLE Notification style
 *
 * @see NotificationStyle, (https://developer.android.com/training/notify-user/group), (https://stackoverflow.com/a/41114135)
 *
 */
open class PushKFirebaseService(
    private val PARAM_NOTIFICATIONS_SUMMARY_TITLE_AND_TEXT: Pair<String, String>?,
    private val PARAM_NOTIFICATIONS_ICON_RESOURCE_ID: Int = android.R.drawable.ic_notification_overlay,
    private val PARAM_NOTIFICATIONS_STYLE: NotificationStyle = NotificationStyle.LARGE_ICON
) : FirebaseMessagingService() {

    private var api: PushKApi = PushKApi()
    private var parsing: PushParsing = PushParsing()
    private var getDevInform: GetInfo = GetInfo()

    /**
     * Constants used within the PushKFirebaseService
     */
    companion object {
        /**
         * max notifications that can be shown by the system at a time
         */
        const val MAX_NOTIFICATIONS = 25

        /**
         * Channel id of notifications
         */
        const val DEFAULT_NOTIFICATION_CHANNEL_ID = "pushsdk.notification.channel"

        /**
         * group id of notifications
         */
        const val DEFAULT_NOTIFICATION_GROUP_ID = "pushsdk.notification.group"

        /**
         * Intent action when user clicks a notification
         */
        const val DEFAULT_NOTIFICATION_ACTION = "pushsdk.intent.action.notification"

        /**
         * Action for intent that is broadcasted when a push is received
         */
        const val DEFAULT_BROADCAST_ACTION = "com.push.android.pushsdkandroid.Push"

        /**
         * The "user-visible" name of the channel
         */
        const val NOTIFICATION_CHANNEL_NAME = "PushSDK channel"

        /**
         * tag for regular notification
         */
        const val NOTIFICATION_TAG = "pushsdk_b_n"

        /**
         * constant summary notification id
         */
        const val DEFAULT_SUMMARY_NOTIFICATION_ID = 0

        /**
         * tag for summary notification
         */
        const val SUMMARY_NOTIFICATION_TAG = "pushsdk_s_b_n"
    }

    /**
     * Notification styles enumeration, used for displaying notifications
     * @see sendNotification
     */
    enum class NotificationStyle {
        /**
         * Default style
         * Plain title/text notification
         */
        DEFAULT_PLAIN_TEXT,

        /**
         * shows image as large icon
         * or uses default style if image can not be displayed
         */
        LARGE_ICON,

        /**
         * shows image as big picture
         * or uses default style if image can not be displayed
         */
        BIG_PICTURE
    }

    /**
     * Get bitmap from a URL
     * @param strURL URL containing an image
     */
    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        try {
            URL(strURL).openConnection().run {
                doInput = true
                connect()
                return BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            PushKLoggerSdk.debug(Log.getStackTraceString(e))
            return null
        }
    }

    /**
     * Check if the app is currently in foreground or background
     * @return true if the app is in foreground, false otherwise
     */
    private fun isAppInForeground(): Boolean {
        val isInForeground =
            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        PushKLoggerSdk.debug("App is in foreground: $isInForeground")
        return isInForeground
    }

    /**
     * Build and show the notification
     *
     * @param data data object received from push
     */
    private fun sendNotification(
        data: Map<String, String>
    ) {
        //parse the data object
        val message = Gson().fromJson(data.toString(), PushDataModel::class.java).message
        if (message == null) {
            //message is empty, thus it must be an error
            PushKLoggerSdk.error("message is empty")
            //TODO do something if needed
            //then stop executing
            return
        }

        //Create notification channel if it doesn't exist (mandatory for Android O and above)
        NotificationManagerCompat.from(applicationContext).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    NotificationChannel(
                        DEFAULT_NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }

            //construct the notification object
            val notification =
                NotificationCompat.Builder(applicationContext, DEFAULT_NOTIFICATION_CHANNEL_ID)
                    .apply {
                        setGroup(DEFAULT_NOTIFICATION_GROUP_ID)
                        priority = NotificationCompat.PRIORITY_MAX
                        setAutoCancel(true)
                        setContentTitle(message.title)
                        setContentText(message.body)
                        setSmallIcon(PARAM_NOTIFICATIONS_ICON_RESOURCE_ID)
                        setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        //actually should never be null, but just in case
                        packageManager.getLaunchIntentForPackage(applicationInfo.packageName)?.let {
                            //build an intent for notification (click to open the app)
                            val pendingIntent = PendingIntent.getActivity(
                                this@PushKFirebaseService,
                                0,
                                it.apply {
                                    action = DEFAULT_NOTIFICATION_ACTION
                                    putExtra("data", data.toString())
                                },
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            setContentIntent(pendingIntent)
                        }
                        when (PARAM_NOTIFICATIONS_STYLE) {
                            NotificationStyle.DEFAULT_PLAIN_TEXT -> {
                                //do nothing
                            }
                            NotificationStyle.LARGE_ICON -> {
                                //Must not be above 1mb in size
                                getBitmapFromURL(message.image.url)?.let {
                                    setLargeIcon(it)
                                }
                            }
                            NotificationStyle.BIG_PICTURE -> {
                                //Must not be above 1mb in size
                                getBitmapFromURL(message.image.url)?.let {
                                    setStyle(NotificationCompat.BigPictureStyle().bigPicture(it))
                                }
                            }
                        }
                    }.build()

            //show regular notification
            val notificationId = Random.nextInt(DEFAULT_SUMMARY_NOTIFICATION_ID+1,Int.MAX_VALUE - 10)
            notify(NOTIFICATION_TAG, notificationId, notification)

            //Not much point in making bundled notifications on API < 24
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //construct the summary notification object
                PARAM_NOTIFICATIONS_SUMMARY_TITLE_AND_TEXT?.let {
                    val summaryNotification =
                        NotificationCompat.Builder(
                            applicationContext,
                            DEFAULT_NOTIFICATION_CHANNEL_ID
                        )
                            .apply {
                                setGroup(DEFAULT_NOTIFICATION_GROUP_ID)
                                setGroupSummary(true)
                                priority = NotificationCompat.PRIORITY_MAX
                                setAutoCancel(true)
                                setContentTitle(PARAM_NOTIFICATIONS_SUMMARY_TITLE_AND_TEXT.first)
                                setContentText(PARAM_NOTIFICATIONS_SUMMARY_TITLE_AND_TEXT.second)
                                setSmallIcon(PARAM_NOTIFICATIONS_ICON_RESOURCE_ID)
                            }.build()

                    //show summary notification
                    notify(
                        SUMMARY_NOTIFICATION_TAG,
                        DEFAULT_SUMMARY_NOTIFICATION_ID,
                        summaryNotification
                    )
                }
            }
        }
    }

    /**
     * Whether system has space to show at least 1 more notification;
     * Assume there is no space by default; Will always return true for api levels < 23
     *
     * @param cancelOldest - cancel oldest notification to free up space
     */
    private fun hasSpaceForNotification(cancelOldest: Boolean) : Boolean {
        var hasSpaceForNotification = false

        //check notification limit, and cancel first active notification if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager =
                this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val activeNotifications = notificationManager.activeNotifications.toMutableList()
            val compareNotificationByPostTime = Comparator<StatusBarNotification> { o1, o2 ->
                return@Comparator (o1.postTime).compareTo(o2.postTime)
            }
            when (activeNotifications.size) {
                in 0 until MAX_NOTIFICATIONS -> {
                    hasSpaceForNotification = true
                }
                else -> {
                    if (cancelOldest) {
                        Collections.sort(activeNotifications, compareNotificationByPostTime)
                        for (activeNotification in activeNotifications) {
                            if (activeNotification.tag == NOTIFICATION_TAG) {
                                notificationManager.cancel(
                                    activeNotification.tag, activeNotification.id
                                )
                                hasSpaceForNotification = true
                                break
                            }
                        }
                    }
                }
            }
        }
        else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //TODO not yet supported, might use NotificationListenerService
                //assume there is space
                hasSpaceForNotification = true
            }
            else {
                //assume there is space, nothing can be done in this case
                hasSpaceForNotification = true
            }

        return hasSpaceForNotification
    }

    /**
     * Called when the service receives a FCM push containing data; Override it
     * without the "super" call, if you want to implement your own notifications or disable them
     *
     * @param appIsInForeground whether the application is currently in foreground or background
     * @param remoteMessage received remote message object
     */
    open fun onReceiveDataPush(appIsInForeground: Boolean, remoteMessage: RemoteMessage) {
        if (remoteMessage.notification == null && !appIsInForeground) {
            //send notification
            val areNotificationsEnabled = NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            if (areNotificationsEnabled && hasSpaceForNotification(true)) {
                sendNotification(remoteMessage.data)
                onDisplayNotification(appIsInForeground, remoteMessage)
            } else {
                //notify the user there is no space for notifications,
                // and the push must be handled manually
                onUnableToDisplayNotification(areNotificationsEnabled, appIsInForeground, remoteMessage)
            }
        }
    }

    /**
     * Called when notification will be displayed;
     * Displaying a notification is not guaranteed if the application's notification limit has been reached;
     * The method will not be called if onReceiveDataPush wasn't called before
     *
     * @param appIsInForeground whether the application is currently in foreground or background
     * @param remoteMessage received remote message object
     * @see sendNotification the default method for showing notifications
     */
    open fun onDisplayNotification(appIsInForeground: Boolean, remoteMessage: RemoteMessage) {
        //does nothing
    }

    /**
     * Called when notification will not be displayed, so you can try displaying it manually;
     * The method will not be called if onReceiveDataPush wasn't called before
     *
     * @param areNotificationsEnabled whether notifications are enabled for the app
     * @see NotificationManagerCompat.areNotificationsEnabled
     * @param appIsInForeground whether the application is currently in foreground or background
     * @param remoteMessage received remote message object
     */
    open fun onUnableToDisplayNotification(areNotificationsEnabled: Boolean, appIsInForeground: Boolean, remoteMessage: RemoteMessage) {
        //does nothing
    }

    /**
     * Sends dataPush broadcast, so it could be caught somewhere else
     */
    private fun sendDataPushBroadcast(remoteMessage: RemoteMessage) {
        try {
            PushKPushMess.message = remoteMessage.data.toString().replace("\\/", "/")
            Intent().apply {
                action = DEFAULT_BROADCAST_ACTION
                sendBroadcast(this)
            }
            PushKLoggerSdk.debug("datapush broadcast success")
        } catch (e: Exception) {
            PushKPushMess.message = ""
            PushKLoggerSdk.debug("datapush broadcast error: ${Log.getStackTraceString(e)}")
        }
    }

    /**
     * Called when the service is created
     */
    override fun onCreate() {
        super.onCreate()
        PushKLoggerSdk.debug("PushFirebaseService.onCreate : MyService onCreate")
    }

    /**
     * Called when the service is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        PushKLoggerSdk.debug("PushFirebaseService.onDestroy : MyService onDestroy")
    }

    /**
     * Called when a new firebase token is obtained
     * @param newToken the new firebase token
     */
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        PushKLoggerSdk.debug("PushFirebaseService.onNewToken : Result: Start step1, Function: onNewToken, Class: PushFirebaseService, new_token: $newToken")

        try {
            if (newToken != "") {
                val pushUpdateParams = RewriteParams(applicationContext)
                pushUpdateParams.rewriteFirebaseToken(newToken)
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
                    newToken
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


    /**
     * Called when a message is received from firebase
     * @param remoteMessage the received message
     *
     * There are two types of messages data messages and notification messages. Data messages are handled
     * here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
     * traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
     * is in the foreground. When the app is in the background an automatically generated notification is displayed.
     * When the user taps on the notification they are returned to the app. Messages containing both notification
     * and data payloads are treated as notification messages. The Firebase console always sends notification
     * messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        PushKLoggerSdk.debug("PushFirebaseService.onMessageReceived : started")
        super.onMessageReceived(remoteMessage)

        PushKLoggerSdk.debugFirebaseRemoteMessage(remoteMessage)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            try {
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
                PushKLoggerSdk.debug("onMessageReceived: failed: ${Log.getStackTraceString(e)}")
            }

            sendDataPushBroadcast(remoteMessage)

            //data push received, make a callback
            onReceiveDataPush(isAppInForeground(), remoteMessage)
        }
    }

    /**
     * Called when firebase deletes some messages (they will never be delivered);
     * When the app instance receives this callback, it should perform a full sync with your app server
     *
     * In some situations, FCM may not deliver a message.
     * This occurs when there are too many messages (>100) pending for your app on a particular
     * device at the time it connects or if the device hasn't connected to FCM in more than one month.
     * In these cases, you may receive a callback to FirebaseMessagingService.onDeletedMessages()
     * When the app instance receives this callback, it should perform a full sync with your app server.
     * If you haven't sent a message to the app on that device within the last 4 weeks, FCM won't call onDeletedMessages()
     */

    @Suppress("RedundantOverride")
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        //TODO sync something in here
    }
}
