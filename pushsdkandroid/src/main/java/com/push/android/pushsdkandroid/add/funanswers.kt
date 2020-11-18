package com.push.android.pushsdkandroid.add

import android.content.Context
import com.push.android.pushsdkandroid.models.PushKFunAnswerGeneral
import com.push.android.pushsdkandroid.models.PushKFunAnswerRegister
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

internal class Answer {

    @Serializable
    data class ParentRegistration(
        @SerialName("session")
        val session: SessionClassNe,
        @SerialName("profile")
        val profile: ProfileClassNe,
        @SerialName("device")
        val device: DeviceClassNe
    )

    @Serializable
    data class SessionClassNe(
        @SerialName("token")
        val token: String
    )

    @Serializable
    data class ProfileClassNe(
        @SerialName("userId")
        val userId: String,
        @SerialName("userPhone")
        val userPhone: String,
        @SerialName("createdAt")
        val createdAt: String
    )

    @Serializable
    data class DeviceClassNe(
        @SerialName("deviceId")
        val deviceId: String
    )

    @Serializable
    data class RegAnswerSuccess(
        @SerialName("deviceId")
        val deviceId: String,
        @SerialName("token")
        val token: String,
        @SerialName("userId")
        val userId: String,
        @SerialName("userPhone")
        val userPhone: String,
        @SerialName("createdAt")
        val createdAt: String
    )

    fun pushKRegisterNewRegisterExists2(
        deviceId: String,
        push_k_registration_token: String,
        push_k_user_id: String,
        push_k_user_msisdn: String,
        push_k_registration_createdAt: String
    ): PushKFunAnswerRegister {

        return PushKFunAnswerRegister(
            code = 701,
            deviceId = deviceId,
            token = push_k_registration_token,
            userId = push_k_user_id,
            userPhone = push_k_user_msisdn,
            createdAt = push_k_registration_createdAt,
            result = "Exists",
            description = "Device already registered. Nothing to do"
        )
    }

    fun registerProcedureAnswer2(
        resp_code: String,
        resp_body: String,
        context: Context
    ): PushKFunAnswerRegister {

        val pushKRewrite = RewriteParams(context)
        val answerRegistrar: PushKFunAnswerRegister

        when (resp_code) {
            "200" -> {
                val parent = JSON.parse(ParentRegistration.serializer(), resp_body)
                val jsonBody = JSON.stringify(
                    RegAnswerSuccess.serializer(),
                    RegAnswerSuccess(
                        parent.device.deviceId,
                        parent.session.token,
                        parent.profile.userId,
                        parent.profile.userPhone,
                        parent.profile.createdAt
                    )
                )
                answerRegistrar = PushKFunAnswerRegister(
                    code = 200,
                    description = "Success",
                    result = "Ok",
                    deviceId = parent.device.deviceId,
                    token = parent.session.token,
                    userId = parent.profile.userId,
                    userPhone = parent.profile.userPhone,
                    createdAt = parent.profile.createdAt
                )

                pushKRewrite.rewritePushUserId(parent.profile.userId)
                pushKRewrite.rewritePushRegistrationToken(parent.session.token)
                pushKRewrite.rewritePushCreateAt(parent.profile.createdAt)
                pushKRewrite.rewritePushDeviceId(parent.device.deviceId)
                pushKRewrite.rewriteApiRegistrationStatus(true)

                return answerRegistrar
            }
            "401" -> {
                answerRegistrar = PushKFunAnswerRegister(
                    code = 401,
                    description = "(Client error) authentication error, probably errors",
                    result = "Failed",
                    deviceId = "unknown",
                    token = "unknown",
                    userId = "unknown",
                    userPhone = "unknown",
                    createdAt = "unknown"
                )
                return answerRegistrar
            }
            "400" -> {
                answerRegistrar = PushKFunAnswerRegister(
                    code = 400,
                    description = "(Client error) request validation error",
                    result = "Failed",
                    deviceId = "unknown",
                    token = "unknown",
                    userId = "unknown",
                    userPhone = "unknown",
                    createdAt = "unknown"
                )
                return answerRegistrar
            }
            "500" -> {
                answerRegistrar = PushKFunAnswerRegister(
                    code = 500,
                    description = "(Server error)",
                    result = "Failed",
                    deviceId = "unknown",
                    token = "unknown",
                    userId = "unknown",
                    userPhone = "unknown",
                    createdAt = "unknown"
                )
                return answerRegistrar
            }
            "700" -> {
                answerRegistrar = PushKFunAnswerRegister(
                    code = 700,
                    description = "Internal SDK error",
                    result = "Failed",
                    deviceId = "unknown",
                    token = "unknown",
                    userId = "unknown",
                    userPhone = "unknown",
                    createdAt = "unknown"
                )
                return answerRegistrar
            }
            else -> {
                answerRegistrar = PushKFunAnswerRegister(
                    code = 710,
                    description = "Unknown error",
                    result = "Failed",
                    deviceId = "unknown",
                    token = "unknown",
                    userId = "unknown",
                    userPhone = "unknown",
                    createdAt = "unknown"
                )
                return answerRegistrar
            }
        }
    }

    fun generalAnswer(
        resp_code: String,
        body_json: String,
        description: String
    ): PushKFunAnswerGeneral {

        return when (resp_code) {
            "200" -> {
                PushKFunAnswerGeneral(200, "OK", "Success", body_json)
            }
            "400" -> {
                PushKFunAnswerGeneral(400, "Failed", "Failed", "unknown")
            }
            else -> {
                PushKFunAnswerGeneral(resp_code.toInt(), "Failed", description, body_json)
            }
        }
    }

}