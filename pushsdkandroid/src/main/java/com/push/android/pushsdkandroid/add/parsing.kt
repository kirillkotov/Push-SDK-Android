package com.push.android.pushsdkandroid.add

import com.push.android.pushsdkandroid.core.PushSdkParameters
import com.push.android.pushsdkandroid.core.UrlsPlatformList
import com.push.android.pushsdkandroid.logger.PushKLoggerSdk

internal class PushParsing {

    fun parseIdDevicesAll(input_json: String): String {
        PushKLoggerSdk.debug("Result: Function: parseIdDevicesAll, Class: PushParsing, input_json: $input_json")
        var restParsingStr = "["
        val regex =
            """"id":\s(\d+),\s|"id":(\d+),\s|"id":(\d+),|"id" :(\d+),|"id":(\d+) ,""".toRegex()
        val matchResults = regex.findAll(input_json)
        for (matchedText in matchResults) {
            val value: String =
                matchedText.value.replace("\"", "").replace(",", "").replace("id", "")
                    .replace(" ", "").replace(":", "")
            restParsingStr = "$restParsingStr\"$value\", "
        }
        restParsingStr = restParsingStr.dropLast(2) + "]"
        PushKLoggerSdk.debug("Result: Function: parseIdDevicesAll, Class: PushParsing, output: $restParsingStr")
        return restParsingStr
    }

    fun pathTransformation(baseUrl: String, pathUpl: UrlsPlatformList): UrlsPlatformList  {
        val lastSym: String = baseUrl.last().toString()
        var baseUrlUpdated = baseUrl
        if (baseUrlUpdated.contains("{version}")) {
            baseUrlUpdated = baseUrlUpdated.replace("{version}", PushSdkParameters.sdkVersion)
        }
        if (lastSym == "/") {
            return UrlsPlatformList(
                fun_pushsdk_url_device_update = baseUrlUpdated + pathUpl.fun_pushsdk_url_device_update,
                fun_pushsdk_url_registration = baseUrlUpdated + pathUpl.fun_pushsdk_url_registration,
                fun_pushsdk_url_revoke = baseUrlUpdated + pathUpl.fun_pushsdk_url_revoke,
                fun_pushsdk_url_get_device_all = baseUrlUpdated + pathUpl.fun_pushsdk_url_get_device_all,
                fun_pushsdk_url_message_callback = baseUrlUpdated + pathUpl.fun_pushsdk_url_message_callback,
                fun_pushsdk_url_message_dr = baseUrlUpdated + pathUpl.fun_pushsdk_url_message_dr,
                fun_pushsdk_url_mess_queue = baseUrlUpdated + pathUpl.fun_pushsdk_url_mess_queue,
                pushsdk_url_message_history = baseUrlUpdated + pathUpl.pushsdk_url_message_history
            )
        } else {
            return UrlsPlatformList(
                fun_pushsdk_url_device_update = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_device_update,
                fun_pushsdk_url_registration = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_registration,
                fun_pushsdk_url_revoke = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_revoke,
                fun_pushsdk_url_get_device_all = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_get_device_all,
                fun_pushsdk_url_message_callback = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_message_callback,
                fun_pushsdk_url_message_dr = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_message_dr,
                fun_pushsdk_url_mess_queue = baseUrlUpdated + "/" + pathUpl.fun_pushsdk_url_mess_queue,
                pushsdk_url_message_history = baseUrlUpdated + "/" + pathUpl.pushsdk_url_message_history
            )
        }
    }
}
