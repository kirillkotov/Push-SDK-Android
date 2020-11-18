package com.push.android.pushsdkandroid.add

import android.os.Build
import android.text.TextUtils
import android.content.Context
import android.content.res.Configuration
import com.push.android.pushsdkandroid.logger.PushKLoggerSdk

/**
 * Utils for getting info
 */
internal class GetInfo {

    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return try {
            if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else capitalize(manufacturer) + " " + model
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Get android version
     */
    fun getAndroidVersion(): String {
        return try {
            Build.VERSION.RELEASE
        } catch (e: java.lang.Exception) {
            "unknown"
        }
    }

    /**
     * get device type (phone or tablet)
     */
    fun getPhoneType(context: Context): String {
        return try {
            val flagIsTab: Boolean =
                context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
            if (flagIsTab) {
                PushKLoggerSdk.debug("Result: Function: get_phone_type, Class: GetInfo, flagisTab: $flagIsTab, answer: tablet")
                "tablet"
            } else {
                PushKLoggerSdk.debug("Result: Function: get_phone_type, Class: GetInfo, flagisTab: $flagIsTab, answer: phone")
                "phone"
            }
        } catch (e: java.lang.Exception) {
            "unknown"
        }
    }

    /**
     * Capitalize string (why?!?!)
     */
    private fun capitalize(str: String): String? {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }


}