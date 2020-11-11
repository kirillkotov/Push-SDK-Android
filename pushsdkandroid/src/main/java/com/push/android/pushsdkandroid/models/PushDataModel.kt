package com.push.android.pushsdkandroid.models

/**
 * Push data model, describes push data contents, only for internal use
 */
internal data class PushDataModel(
    var message: PushDataMessageModel?
)

internal data class PushDataMessageModel(
    var title: String,
    var body: String,
    var image: PushDataMessageImageModel
)

internal data class PushDataMessageImageModel(
    var url: String
)