package com.push.android.pushsdkandroid.models

/**
 * request response model
 */
internal data class PushKDataApi(
    val code: Int,
    val body: String,
    val time: Int
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

internal data class QueueMessages(
    val messages: PushDataMessageModel?
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