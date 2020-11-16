@file:Suppress("unused", "SpellCheckingInspection")

package com.push.android.pushsdkandroid.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.push.android.pushsdkandroid.add.GetInfo
import com.push.android.pushsdkandroid.add.PushKInternal
import java.net.HttpURLConnection
import java.net.URL

/**
 * Push SDK public params
 */
@Deprecated("Deprecated at 16.11.2020, will be removed")
open class PushKPublicParams {

    /**
     * Get Bitmap image from a URL
     */
    @Deprecated("will be removed")
    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        var ansBitmap: Bitmap? = null
        val threadNetBitmap = Thread(Runnable {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                ansBitmap = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                ansBitmap = null
            }
        })
        threadNetBitmap.start()
        threadNetBitmap.join()
        return ansBitmap
    }

    /**
     * Notification builder of unknown origin
     */
    @Deprecated("will be removed")
    open fun notificationBuilder(
        context: Context,
        notificationTextMess: String,
        image: String
    ): NotificationCompat.Builder {
        val imageBitmap = getBitmapFromUrl(image)
        val intent = Intent(context, context::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (imageBitmap != null) {
            return NotificationCompat.Builder(context, "push.push.k.sdk")
                .setContentText(notificationTextMess)
                .setAutoCancel(true)
                //.setSmallIcon(R.drawable.googleg_standard_color_18)
                .setPriority(PushKInternal.notificationPriorityOld(PushSdkParameters.push_notification_display_priority))
                .setSound(defaultSoundUri)
                //.setVibrate(longArrayOf(1000))
                .setContentIntent(pendingIntent)
                .setLargeIcon(imageBitmap)
        } else {
            return NotificationCompat.Builder(context, "push.push.k.sdk")
                .setContentText(notificationTextMess)
                .setAutoCancel(true)
                //.setSmallIcon(R.drawable.googleg_standard_color_18)
                .setPriority(PushKInternal.notificationPriorityOld(PushSdkParameters.push_notification_display_priority))
                .setSound(defaultSoundUri)
                //.setVibrate(longArrayOf(1000))
                .setContentIntent(pendingIntent)
        }
    }

    /**
     * Notification builder of unknown origin
     */
    @Deprecated("will be removed")
    open fun notificationBuilder(
        context: Context,
        notificationTextMess: String
    ): NotificationCompat.Builder {
        val intent = Intent(context, context::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return NotificationCompat.Builder(context, "push.push.k.sdk")
            .setContentText(notificationTextMess)
            .setAutoCancel(true)
            //.setSmallIcon(R.drawable.googleg_standard_color_18)
            .setPriority(PushKInternal.notificationPriorityOld(PushSdkParameters.push_notification_display_priority))
            .setSound(defaultSoundUri)
            //.setVibrate(longArrayOf(1000))
            .setContentIntent(pendingIntent)
    }
}

/**
 * URLs DATA for Push platform for different branches
 */
object PushSdkParametersPublic {
    /**
     * Common branches
     */
    val branchMasterValue: UrlsPlatformList = UrlsPlatformList(
        fun_pushsdk_url_device_update = "device/update",
        fun_pushsdk_url_registration = "device/registration",
        fun_pushsdk_url_revoke = "device/revoke",
        fun_pushsdk_url_get_device_all = "device/all",
        fun_pushsdk_url_message_callback = "message/callback",
        fun_pushsdk_url_message_dr = "message/dr",
        fun_pushsdk_url_mess_queue = "message/queue",
        pushsdk_url_message_history = "message/history?startDate="
    )

    /**
     * Logging tag
     */
    const val TAG_LOGGING = "PushPushSDK"

    /**
     * log level "error"
     */
    const val pushsdk_log_level_error = "error"

    /**
     * log level "debug"
     */
    const val pushsdk_log_level_debug = "debug"

}

/**
 * Push SDK params
 */
object PushSdkParameters {
    private var infoLocalDeviceHardware: GetInfo = GetInfo()

    /**
     * SDK version
     */
    var sdkVersion: String = "1.0.0.47"

    /**
     * os type
     */
    var push_k_osType: String = "android"

    /**
     * device name
     */
    var push_k_deviceName: String = infoLocalDeviceHardware.getDeviceName().toString()

    /**
     * push notification priority?!?!?!
     */
    var push_notification_display_priority: Int = 2
        set(value) {
            if (value > 0) field = value
        }

    /**
     * platform url branches. It can be overridden by Push SDK initiation
     */
    var platformBranchLoaded: UrlsPlatformList = PushSdkParametersPublic.branchMasterValue

    /**
     * Cureent branch "list"
     */
    var branchCurrentActivePath: UrlsPlatformList? = null
}

/**
 * Seems to be useless
 */
@Deprecated("Deprecated at 16.11.2020, will be removed")
interface PushKAp

/**
 * Seems to be useless
 */
@Deprecated("Deprecated at 16.11.2020, will be removed")
enum class PushKApC : PushKAp {
    /**
     * No reason to exist
     */
    BODY
}

/**
 * request response model
 */
internal data class PushKDataApi(
    val code: Int,
    val body: String,
    val time: Int
)

/**
 * General request response answer structure
 * @param code response code
 * @param result response result
 * @param description description
 * @param body body
 */
data class PushKFunAnswerGeneral(
    val code: Int,
    val result: String,
    val description: String,
    val body: String
)

/**
 * Answer structure received from registration request response
 * @param code
 * @param result
 * @param description
 * @param deviceId
 * @param token
 * @param userId
 * @param userPhone
 * @param createdAt
 */
data class PushKFunAnswerRegister(
    val code: Int = 0,
    val result: String = "",
    val description: String = "",
    val deviceId: String = "",
    val token: String = "",
    val userId: String = "",
    val userPhone: String = "",
    val createdAt: String = ""
)

/**
 * request response model
 */
internal data class PushKDataApi2(
    val code: Int,
    val body: PushKFunAnswerRegister,
    val time: Int
)

/**
 * "List" of platform URLs
 */
data class UrlsPlatformList(
    val fun_pushsdk_url_device_update: String,
    val fun_pushsdk_url_registration: String,
    val fun_pushsdk_url_revoke: String,
    val fun_pushsdk_url_get_device_all: String,
    val fun_pushsdk_url_message_callback: String,
    val fun_pushsdk_url_message_dr: String,
    val fun_pushsdk_url_mess_queue: String,
    val pushsdk_url_message_history: String
)

data class PushOperativeData(
    //is procedure for register new device completed or not
    // (true - devise exist on server. )
    // false - it s new device and we need to complete push_register_new()
    var registrationStatus: Boolean = false,

    var push_k_user_Password: String = "",
    var push_k_user_msisdn: String = "",
    var push_k_registration_token: String = "",
    var push_k_user_id: String = "",
    var push_k_registration_createdAt: String = "",
    var firebase_registration_token: String = "",
    var push_k_registration_time: String = "",

    //uuid generates only one time
    var push_k_uuid: String = "",

    //its deviceId which we receive from server with answer for push_register_new()
    var deviceId: String = ""
)