package com.push.android.pushsdkandroid.core

import android.content.Context

internal class PushSdkSavedDataProvider(context: Context) {
    private val sharedPreferencesHandler: SharedPreferencesHandler = SharedPreferencesHandler(context.applicationContext)

    ////////////////////////////////////////
    //----Old params
    var registrationStatus: Boolean
        get() = sharedPreferencesHandler.getValueBool("registrationstatus", false)
        set(value) = sharedPreferencesHandler.save("registrationstatus", value)

    var push_k_user_Password: String
        get() = sharedPreferencesHandler.getValueString("push_k_user_Password")
        set(value) = sharedPreferencesHandler.saveString("push_k_user_Password", value)
    var push_k_user_msisdn: String
        get() = sharedPreferencesHandler.getValueString("push_k_user_msisdn")
        set(value) = sharedPreferencesHandler.saveString("push_k_user_msisdn", value)
    var push_k_registration_token: String
        get() = sharedPreferencesHandler.getValueString("push_k_registration_token")
        set(value) = sharedPreferencesHandler.saveString("push_k_registration_token", value)
    var push_k_user_id: String
        get() = sharedPreferencesHandler.getValueString("push_k_user_id")
        set(value) = sharedPreferencesHandler.saveString("push_k_user_id", value)
    var push_k_registration_createdAt: String
        get() = sharedPreferencesHandler.getValueString("push_k_registration_createdAt")
        set(value) = sharedPreferencesHandler.saveString("push_k_registration_createdAt", value)
    var firebase_registration_token: String
        get() = sharedPreferencesHandler.getValueString("firebase_registration_token")
        set(value) = sharedPreferencesHandler.saveString("firebase_registration_token", value)

    //uuid generates only one time
    var push_k_uuid: String
        get() = sharedPreferencesHandler.getValueString("push_k_uuid")
        set(value) = sharedPreferencesHandler.saveString("push_k_uuid", value)

    //its deviceId which we receive from server with answer for push_register_new()
    var deviceId: String
        get() = sharedPreferencesHandler.getValueString("deviceId")
        set(value) = sharedPreferencesHandler.saveString("deviceId", value)

    ////////////////////////////////////////
    //----Settings
    var logLevel: String
        get() = sharedPreferencesHandler.getValueString("logLevel")
        set(value) = sharedPreferencesHandler.saveString("logLevel", value)

    ////////////////////////////////////////
    //----API params
    /**
     * baseURL, must be structured like "https://api.com/api"
     */
    var baseApiUrl: String
        get() = sharedPreferencesHandler.getValueString("baseApiUrl")
        set(value) = sharedPreferencesHandler.saveString("baseApiUrl", value)

}