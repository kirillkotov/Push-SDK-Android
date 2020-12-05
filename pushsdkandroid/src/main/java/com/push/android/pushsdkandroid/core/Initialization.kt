package com.push.android.pushsdkandroid.core

import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import java.util.*

/**
 * Initialization of various parameters
 */
internal class Initialization(private val context: Context) {

    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)

    private fun hSdkUpdateFirebaseAuto() {
        PushSDKLogger.debug(context, "Initialization.hSdkUpdateFirebaseAuto start")
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            if (token != "") {
                pushSdkSavedDataProvider.firebase_registration_token = token
                PushSDKLogger.debug(context, "Initialization.hSdkUpdateFirebaseAuto.Firebase token: $token")
            } else {
                PushSDKLogger.debug(context, "Initialization.hSdkUpdateFirebaseAuto.Firebase token empty")
            }
        }
        PushSDKLogger.debug(context, "Initialization.hSdkUpdateFirebaseAuto finished")


    }

    fun hSdkUpdateFirebaseManual(x_token: String): String {
        pushSdkSavedDataProvider.firebase_registration_token = x_token
        PushSDKLogger.debug(context, "Initialization.hSdkUpdateFirebaseManual.Firebase token: $x_token")
        return x_token
    }


    fun hSdkGetParametersFromLocal() {
        PushSDKLogger.debug(context, "Initialization.hSdkGetParametersFromLocal started")

        val registrationStatus: Boolean = pushSdkSavedDataProvider.registrationStatus
        PushSDKLogger.debug(context, "Initialization.hSdkGetParametersFromLocal registrationStatus: $registrationStatus")

        hSdkUpdateFirebaseAuto()

        PushSDKLogger.debug(context, "Initialization.paramsLoader finished: push_k_uuid=${pushSdkSavedDataProvider.push_k_uuid}, " +
                "devId=${pushSdkSavedDataProvider.deviceId}, push_k_user_msisdn=${pushSdkSavedDataProvider.push_k_user_msisdn}, " +
                "push_k_user_Password=${pushSdkSavedDataProvider.push_k_user_Password}, " +
                "push_k_registration_token=${pushSdkSavedDataProvider.push_k_registration_token}, " +
                "push_k_user_id=${pushSdkSavedDataProvider.push_k_user_id}, " +
                "push_k_registration_createdAt=${pushSdkSavedDataProvider.push_k_registration_createdAt}")
    }

    fun hSdkInitSaveToLocal(
        deviceId: String,
        push_k_user_msisdn: String,
        push_k_user_Password: String,
        push_k_registration_token: String,
        push_k_user_id: String,
        push_k_registration_createdAt: String,
        registrationStatus: Boolean
    ) {
        PushSDKLogger.debug(context, "Initialization.hSdkInitSaveToLocal  started")
        val pushUuid = UUID.randomUUID().toString()
        PushSDKLogger.debug(context, "Initialization.hSdkInit  pushUuid=$pushUuid, " +
                "deviceId=$deviceId, push_k_user_msisdn=$push_k_user_msisdn, " +
                "push_k_user_Password=$push_k_user_Password, " +
                "push_k_registration_token=$push_k_registration_token, " +
                "push_k_user_id=$push_k_user_id, " +
                "push_k_registration_createdAt=$push_k_registration_createdAt")
        pushSdkSavedDataProvider.push_k_uuid = pushUuid
        pushSdkSavedDataProvider.deviceId = deviceId
        pushSdkSavedDataProvider.push_k_user_msisdn = push_k_user_msisdn
        pushSdkSavedDataProvider.push_k_user_Password = push_k_user_Password
        pushSdkSavedDataProvider.push_k_registration_createdAt = push_k_registration_createdAt
        pushSdkSavedDataProvider.push_k_user_id = push_k_user_id
        pushSdkSavedDataProvider.push_k_registration_token = push_k_registration_token
        pushSdkSavedDataProvider.registrationStatus = registrationStatus
    }


    fun clearData() {
        pushSdkSavedDataProvider.registrationStatus = false
        pushSdkSavedDataProvider.deviceId = ""
        pushSdkSavedDataProvider.push_k_user_id = ""
        PushSDKLogger.debug(context, "Initialization.clearData  processed")
    }
}
