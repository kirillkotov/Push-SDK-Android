package com.push.android.pushsdkandroid.models

/**
 * Push data message model, describes push data contents, only for internal use
 */
internal data class PushDataMessageModel(
    val messageId: String,
    val title: String,
    val body: String,
    val image: PushDataMessageImageModel
)

/**
 * Push data message image model, describes push data contents, only for internal use
 */
internal data class PushDataMessageImageModel(
    val url: String
)