package com.push.android.pushsdkandroid.add

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

    fun parseMessageId(input_json: String): String {
        PushKLoggerSdk.debug("Result: Function: parseMessageId, Class: PushParsing, input_json: $input_json")
        val regex = ""","messageId":"(.+)",|messageId=(.+),|"messageId":"(.+)"""".toRegex()
        val matchResults = regex.find(input_json)
        val (res) = matchResults!!.destructured
        PushKLoggerSdk.debug("Result: Function: parseMessageId, Class: PushParsing, output: $res")
        return res
    }


    fun parseImageUrl(input_json: String): String {
        PushKLoggerSdk.debug("Result: Function: parseImageUrl, Class: PushParsing, input_json: $input_json")
        var matchResults = input_json.substringAfter(""""image":{""").substringAfter(""""url":"""").substringBefore(""""""")
        if (matchResults.contains("\\/")) {
            matchResults = matchResults.replace("\\/", "/")
        }
        PushKLoggerSdk.debug("Result: Function: parseMessageId, Class: PushParsing, output: $matchResults")
        return matchResults
    }
}
