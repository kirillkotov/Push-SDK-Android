package com.push.android.pushsdkandroid.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.models.PushDataMessageModel
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import java.net.URL
import java.util.*
import kotlin.Comparator
import kotlin.random.Random

/**
 * The SDK's notification manager; Used to display notifications and check availability
 *
 * @constructor The SDK's notification manager
 *
 * @param context Context to use
 * @param summaryNotificationTitleAndText Summary notification title and text <title, text>,
 * used for displaying a "summary notification", which serves as a root notification for other notifications
 * notifications will not be bundled(grouped) if null; Ignored if api level is below android 7
 * @param notificationIconResourceId Notification small icon
 */
class PushSdkNotificationManager(private val context: Context,
                                 private val summaryNotificationTitleAndText: Pair<String, String>?,
                                 private val notificationIconResourceId: Int = android.R.drawable.ic_notification_overlay) {
    /**
     * Notification constants
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
         * Shows notification without a style;
         * Text will be displayed as single line;
         * Will display the picture as large icon if push message has one
         */
        NO_STYLE,

        /**
         * Default style (Recommended);
         * Sets "Big text" style to allow multiple lines of text;
         * Will display the picture as large icon if push message has one
         */
        BIG_TEXT,

        /**
         * Shows image as big picture;
         * Or uses default style (no style) if image can not be displayed
         */
        BIG_PICTURE
    }

    /**
     * Get bitmap from a URL
     * @param strURL URL containing an image
     */
    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        strURL?.let {
            try {
                URL(strURL).openConnection().run {
                    doInput = true
                    connect()
                    return BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                //not an error
                PushSDKLogger.debug(context, Log.getStackTraceString(e))
                return null
            }
        } ?: return null
    }

    /**
     * Constructs the base NotificationCompat Builder object for chaining
     * @param data - FCM push RemoteMessage's data
     * @param notificationStyle - which built-in style to use (BIG_TEXT as default)
     * @return NotificationCompat Builder object or null if message could not be read
     */
    fun constructNotification(data: Map<String, String>, notificationStyle: NotificationStyle): NotificationCompat.Builder? {
        //parse the data object
        val message = Gson().fromJson(data["message"], PushDataMessageModel::class.java)
        if (message == null) {
            //message is empty, thus it must be an error
            PushSDKLogger.error("constructNotificationBase() - message is empty")
            //TODO do something if needed
            //then stop executing
            return null
        }

        return NotificationCompat.Builder(context.applicationContext, DEFAULT_NOTIFICATION_CHANNEL_ID)
            .apply {
                setGroup(DEFAULT_NOTIFICATION_GROUP_ID)
                priority = NotificationCompat.PRIORITY_MAX
                setAutoCancel(true)
                setContentTitle(message.title)
                setContentText(message.body)
                setSmallIcon(notificationIconResourceId)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //actually should never be null, but just in case
                context.packageManager.getLaunchIntentForPackage(context.applicationInfo.packageName)?.let {
                    //build an intent for notification (click to open the app)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        it.apply {
                            action = PushSDK.NOTIFICATION_CLICK_INTENT_ACTION
                            putExtra(PushSDK.NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME, data["message"])
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    setContentIntent(pendingIntent)
                }
                when (notificationStyle) {
                    NotificationStyle.NO_STYLE -> {
                        //image size is recommended to be <1mb for notifications
                        getBitmapFromURL(message.image.url)?.let {
                            setLargeIcon(it)
                        }
                    }
                    NotificationStyle.BIG_TEXT -> {
                        setStyle(NotificationCompat.BigTextStyle())
                        //image size is recommended to be <1mb for notifications
                        getBitmapFromURL(message.image.url)?.let {
                            setLargeIcon(it)
                        }
                    }
                    NotificationStyle.BIG_PICTURE -> {
                        //image size is recommended to be <1mb for notifications
                        getBitmapFromURL(message.image.url)?.let {
                            setStyle(NotificationCompat.BigPictureStyle().bigPicture(it))
                        }
                    }
                }
            }
    }

    /**
     * Build and show the notification
     *
     * @param notificationConstruct NotificationCompat.Builder object to send
     */
    fun sendNotification(
            notificationConstruct: NotificationCompat.Builder
    ): Boolean {
        //Create notification channel if it doesn't exist (mandatory for Android O and above)
        NotificationManagerCompat.from(context.applicationContext).apply {
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
            val notification = notificationConstruct.build()

            //show regular notification
            val notificationId = Random.nextInt(DEFAULT_SUMMARY_NOTIFICATION_ID+1,Int.MAX_VALUE - 10)
            notify(NOTIFICATION_TAG, notificationId, notification)

            //Not much point in making bundled notifications on API < 24
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //construct the summary notification object
                summaryNotificationTitleAndText?.let {
                    val summaryNotification =
                        NotificationCompat.Builder(
                            context.applicationContext,
                            DEFAULT_NOTIFICATION_CHANNEL_ID
                        )
                            .apply {
                                setGroup(DEFAULT_NOTIFICATION_GROUP_ID)
                                setGroupSummary(true)
                                priority = NotificationCompat.PRIORITY_MAX
                                setAutoCancel(true)
                                setContentTitle(summaryNotificationTitleAndText.first)
                                setContentText(summaryNotificationTitleAndText.second)
                                setSmallIcon(notificationIconResourceId)
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
        return true
    }

    /**
     * Whether system has space to show at least 1 more notification;
     * Assume there is no space by default; Will always return true for api levels < 23
     *
     * @param cancelOldest - cancel oldest notification to free up space
     */
    fun hasSpaceForNotification(cancelOldest: Boolean) : Boolean {
        var hasSpaceForNotification = false

        //check notification limit, and cancel first active notification if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager =
                context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
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
     * Whether notifications are enabled for the app
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
    }

    /**
     * Whether user has "Do not disturb mode on"; Will return false if unable to obtain information
     */
    fun isDoNotDisturbModeActive(): Boolean {
        return try {
            val notificationManager =
                context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PushSDKLogger.debug(context.applicationContext, "currentInterruptionFilter: ${notificationManager.currentInterruptionFilter}")
                notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Whether notification channel is muted on the user's device
     * @param channelId channel id to check
     */
    @Suppress("LiftReturnOrAssignment")
    fun isNotificationChannelMuted(channelId: String): Boolean {
        val notificationManager =
                context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            if (channel != null) {
                val importance = channel.importance
                PushSDKLogger.debug(context.applicationContext, "getNotificationChannel(channelId).importance: $importance")
                return importance == NotificationManager.IMPORTANCE_NONE
            }
            else {
                PushSDKLogger.debug(context.applicationContext, "$channelId - CHANNEL ID DOES NOT EXIST")
                return false
            }
        }
        else {
            return false
        }
    }
}