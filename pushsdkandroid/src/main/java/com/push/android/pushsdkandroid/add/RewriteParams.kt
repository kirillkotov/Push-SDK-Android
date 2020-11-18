package com.push.android.pushsdkandroid.add

import android.content.Context
import com.push.android.pushsdkandroid.core.Initialization.Companion.PushKDatabase
import com.push.android.pushsdkandroid.core.SharedPreferencesHandler

//function for initialization different parameters
//
internal class RewriteParams(val context: Context) {
    private val sharedPreferencesHandler: SharedPreferencesHandler = SharedPreferencesHandler(context)

    fun rewritePushUserMsisdn(push_k_user_msisdn: String) {
        sharedPreferencesHandler.saveString("push_k_user_msisdn", push_k_user_msisdn)
        PushKDatabase.push_k_user_msisdn = push_k_user_msisdn
    }

    fun rewritePushUserPassword(push_k_user_password: String) {
        sharedPreferencesHandler.saveString("push_k_user_Password", push_k_user_password)
        PushKDatabase.push_k_user_Password = push_k_user_password
    }

    fun rewritePushRegistrationToken(push_k_registration_token: String) {
        sharedPreferencesHandler.saveString("push_k_registration_token", push_k_registration_token)
        PushKDatabase.push_k_registration_token = push_k_registration_token
    }

    fun rewritePushUserId(push_k_user_id: String) {
        sharedPreferencesHandler.saveString("push_k_user_id", push_k_user_id)
        PushKDatabase.push_k_user_id = push_k_user_id
    }

    fun rewritePushDeviceId(deviceId: String) {
        sharedPreferencesHandler.saveString("deviceId", deviceId)
        PushKDatabase.deviceId = deviceId
    }

    fun rewritePushCreateAt(push_k_registration_createdAt: String) {
        sharedPreferencesHandler.saveString("push_k_registration_createdAt", push_k_registration_createdAt)
        PushKDatabase.push_k_registration_createdAt = push_k_registration_createdAt
    }

    fun rewriteApiRegistrationStatus(registrationstatus: Boolean) {
        sharedPreferencesHandler.save("registrationstatus", registrationstatus)
        PushKDatabase.registrationStatus = registrationstatus
    }

    fun rewriteFirebaseToken(fb_token_new: String) {
        sharedPreferencesHandler.saveString("firebase_registration_token", fb_token_new)
        PushKDatabase.firebase_registration_token = fb_token_new
    }

}