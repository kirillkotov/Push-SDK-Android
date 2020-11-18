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
@Deprecated("Deprecated, will be removed")
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

