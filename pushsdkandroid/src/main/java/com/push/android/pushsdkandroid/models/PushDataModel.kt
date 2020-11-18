package com.push.android.pushsdkandroid.models

/**
 * Push data model, describes push data contents, only for internal use
 */
internal data class PushDataModel(
    var message: PushDataMessageModel?
)

/**
 * Push data message model, describes push data contents, only for internal use
 */
internal data class PushDataMessageModel(
    var messageId: String,
    var title: String,
    var body: String,
    var image: PushDataMessageImageModel
)

/**
 * Push data message image model, describes push data contents, only for internal use
 */
internal data class PushDataMessageImageModel(
    var url: String
)