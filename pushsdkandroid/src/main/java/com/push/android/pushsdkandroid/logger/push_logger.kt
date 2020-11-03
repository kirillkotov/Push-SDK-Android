package com.push.android.pushsdkandroid.logger

import android.os.Build
import android.util.Log
import com.push.android.pushsdkandroid.PushKPushMess
import com.push.android.pushsdkandroid.core.PushSdkParametersPublic
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

internal object PushKLoggerSdk {


    fun error(message: String) {

        if (Build.VERSION.SDK_INT >= 26) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val formatted = current.format(formatter)
            Log.e(PushSdkParametersPublic.TAG_LOGGING, "$formatted $message")
        } else {
            val formatted =
                SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.e(PushSdkParametersPublic.TAG_LOGGING, "$formatted $message")
        }
    }

    fun debug(message: String) {
        if (PushKPushMess.log_level_active == "debug") {
            if (Build.VERSION.SDK_INT >= 26) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val formatted = current.format(formatter)
                Log.d(PushSdkParametersPublic.TAG_LOGGING, "$formatted $message")
            } else {
                val formatted =
                    SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
                Log.d(PushSdkParametersPublic.TAG_LOGGING, "$formatted $message")
            }
        }
    }

}