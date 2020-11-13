package com.push.android.pushsdkandroid.add

import android.content.Context
import com.push.android.pushsdkandroid.PushKDatabase
import com.push.android.pushsdkandroid.core.SharedPreference

//function for initialization different parameters
//
internal class RewriteParams(val context: Context) {
    private val sharedPreference: SharedPreference = SharedPreference(context)

    fun rewritePushUserMsisdn(push_k_user_msisdn: String) {
        sharedPreference.saveString("push_k_user_msisdn", push_k_user_msisdn)
        PushKDatabase.push_k_user_msisdn = push_k_user_msisdn
    }

    fun rewritePushUserPassword(push_k_user_password: String) {
        sharedPreference.saveString("push_k_user_Password", push_k_user_password)
        PushKDatabase.push_k_user_Password = push_k_user_password
    }

    fun rewritePushRegistrationToken(push_k_registration_token: String) {
        sharedPreference.saveString("push_k_registration_token", push_k_registration_token)
        PushKDatabase.push_k_registration_token = push_k_registration_token
    }

    fun rewritePushUserId(push_k_user_id: String) {
        sharedPreference.saveString("push_k_user_id", push_k_user_id)
        PushKDatabase.push_k_user_id = push_k_user_id
    }

    fun rewritePushDeviceId(deviceId: String) {
        sharedPreference.saveString("deviceId", deviceId)
        PushKDatabase.deviceId = deviceId
    }

    fun rewritePushCreateAt(push_k_registration_createdAt: String) {
        sharedPreference.saveString("push_k_registration_createdAt", push_k_registration_createdAt)
        PushKDatabase.push_k_registration_createdAt = push_k_registration_createdAt
    }

    fun rewriteApiRegistrationStatus(registrationstatus: Boolean) {
        sharedPreference.save("registrationstatus", registrationstatus)
        PushKDatabase.registrationStatus = registrationstatus
    }

    fun rewriteFirebaseToken(fb_token_new: String) {
        sharedPreference.saveString("firebase_registration_token", fb_token_new)
        PushKDatabase.firebase_registration_token = fb_token_new
    }

}