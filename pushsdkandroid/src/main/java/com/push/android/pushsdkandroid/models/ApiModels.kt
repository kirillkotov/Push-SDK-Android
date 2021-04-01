package com.push.android.pushsdkandroid.models

////////////////////////
/// REMOTE API

/**
 * request response model
 */
internal data class PushKDataApi(
    val code: Int,
    val body: String,
    val time: Int
)

internal data class QueueMessages(
    val messages: List<PushDataMessageModel>
)

///////////////////////
/// SDK API

enum class PushSDKAnswerResult {
    OK,
    FAILED
}

enum class PushSDKRegAnswerResult {
    OK,
    FAILED,
    EXISTS
}

/**
 * General request response answer structure
 * @param code response code
 * @param result response result
 * @param description description
 * @param body body
 */
data class PushKFunAnswerGeneral(
    val code: Int,
    val result: PushSDKAnswerResult,
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
    val result: PushSDKRegAnswerResult,
    val description: String = "",
    val deviceId: String = "",
    val token: String = "",
    val userId: String = "",
    val userPhone: String = "",
    val createdAt: String = ""
)