@file:Suppress("unused", "SpellCheckingInspection")

package com.push.android.pushsdkandroid.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.push.android.pushsdkandroid.BuildConfig
import com.push.android.pushsdkandroid.add.GetInfo
import com.push.android.pushsdkandroid.add.PushKInternal
import java.net.HttpURLConnection
import java.net.URL

/**
 * Push SDK public params
 */
@Deprecated("Deprecated at 16.11.2020, will be removed")
internal open class PushKPublicParams {

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
}

/**
 * Push SDK "public" params.
 * fixme will need a better way of doing that
 */
object PushSdkParametersPublic {
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
internal object PushSdkParameters {
    private var infoLocalDeviceHardware: GetInfo = GetInfo()

    /**
     * SDK version
     */
    var sdkVersion: String = BuildConfig.VERSION_NAME

    /**
     * os type
     */
    var push_k_osType: String = "android"

    /**
     * device name
     */
    var push_k_deviceName: String = infoLocalDeviceHardware.getDeviceName().toString()
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
 * Data model that is used for storing "operative" values
 */
internal data class PushOperativeData(

    /**
     * is request to register new device completed or not:
     * (true - devise exists on server)
     * false - it is a new device and we need to complete push_register_new()
     */
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