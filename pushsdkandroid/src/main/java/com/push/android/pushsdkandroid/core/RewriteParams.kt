package com.push.android.pushsdkandroid.core

import android.content.Context

//function for initialization different parameters
//
internal class RewriteParams(val context: Context) {
    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)

    fun rewritePushUserMsisdn(push_k_user_msisdn: String) {
        pushSdkSavedDataProvider.push_k_user_msisdn = push_k_user_msisdn
    }

    fun rewritePushUserPassword(push_k_user_password: String) {
        pushSdkSavedDataProvider.push_k_user_Password = push_k_user_password
    }

    fun rewritePushRegistrationToken(push_k_registration_token: String) {
        pushSdkSavedDataProvider.push_k_registration_token = push_k_registration_token
    }

    fun rewritePushUserId(push_k_user_id: String) {
        pushSdkSavedDataProvider.push_k_user_id = push_k_user_id
    }

    fun rewritePushDeviceId(deviceId: String) {
        pushSdkSavedDataProvider.deviceId = deviceId
    }

    fun rewritePushCreateAt(push_k_registration_createdAt: String) {
        pushSdkSavedDataProvider.push_k_registration_createdAt = push_k_registration_createdAt
    }

    fun rewriteApiRegistrationStatus(registrationstatus: Boolean) {
        pushSdkSavedDataProvider.registrationStatus = registrationstatus
    }

    fun rewriteFirebaseToken(fb_token_new: String) {
        pushSdkSavedDataProvider.firebase_registration_token = fb_token_new
    }

}