package com.push.android.pushsdkandroid.core

import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.push.android.pushsdkandroid.PushKDatabase
import com.push.android.pushsdkandroid.logger.PushSDKLogger
import com.push.android.pushsdkandroid.models.PushOperativeData
import java.util.*


/**
 * Initialization of various parameters
 */
internal class Initialization(val context: Context) {
    private val sharedPreferencesHandler: SharedPreferencesHandler = SharedPreferencesHandler(context)

    private fun hSdkUpdateFirebaseAuto() {
        PushSDKLogger.debug("Initialization.hSdkUpdateFirebaseAuto start")
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            if (token != "") {
                sharedPreferencesHandler.saveString("firebase_registration_token", token)
                PushSDKLogger.debug("Initialization.hSdkUpdateFirebaseAuto.Firebase token: $token")
            } else {
                PushSDKLogger.debug("Initialization.hSdkUpdateFirebaseAuto.Firebase token empty")
            }
        }
        PushSDKLogger.debug("Initialization.hSdkUpdateFirebaseAuto finished")


    }

    fun hSdkUpdateFirebaseManual(x_token: String): String {
        sharedPreferencesHandler.saveString("firebase_registration_token", x_token)
        PushSDKLogger.debug("Initialization.hSdkUpdateFirebaseManual.Firebase token: $x_token")
        return x_token
    }


    fun hSdkGetParametersFromLocal(): PushOperativeData {
        PushSDKLogger.debug("Initialization.hSdkGetParametersFromLocal started")

        PushKDatabase = PushOperativeData()

        val registrationStatus: Boolean = sharedPreferencesHandler.getValueBool("registrationstatus", false)
        PushKDatabase.registrationStatus = registrationStatus

        PushSDKLogger.debug("Initialization.hSdkGetParametersFromLocal registrationStatus: $registrationStatus")

        hSdkUpdateFirebaseAuto()

        //1
        val firebaseRegistrationToken: String =
            sharedPreferencesHandler.getValueString("firebase_registration_token")
        PushSDKLogger.debug("Initialization.hSdkGetParametersFromLocal firebaseRegistrationToken: $firebaseRegistrationToken")

        if (firebaseRegistrationToken != "") {
            PushKDatabase.firebase_registration_token = firebaseRegistrationToken
        }

        if (registrationStatus) {

            //2
            val pushUuid: String = sharedPreferencesHandler.getValueString("push_k_uuid")
            PushKDatabase.push_k_uuid = pushUuid

            //3
            val devId: String = sharedPreferencesHandler.getValueString("deviceId")
            PushKDatabase.deviceId = devId

            //4
            val pushUserMsisdn: String =
                sharedPreferencesHandler.getValueString("push_k_user_msisdn")
            PushKDatabase.push_k_user_msisdn = pushUserMsisdn

            //5
            val pushUserPassword: String =
                sharedPreferencesHandler.getValueString("push_k_user_Password")
            PushKDatabase.push_k_user_Password = pushUserPassword

            //6
            val pushRegistrationToken: String =
                sharedPreferencesHandler.getValueString("push_k_registration_token")
            PushKDatabase.push_k_registration_token = pushRegistrationToken

            //7
            val pushUserId: String =
                sharedPreferencesHandler.getValueString("push_k_user_id")
            PushKDatabase.push_k_user_id = pushUserId

            //8
            val pushRegistrationCreatedAt: String =
                sharedPreferencesHandler.getValueString("push_k_registration_createdAt")
            PushKDatabase.push_k_registration_createdAt = pushRegistrationCreatedAt
        }
        PushSDKLogger.debug("Initialization.paramsLoader finished: push_k_uuid=${PushKDatabase.push_k_uuid}, devId=${PushKDatabase.deviceId}, push_k_user_msisdn=${PushKDatabase.push_k_user_msisdn}, push_k_user_Password=${PushKDatabase.push_k_user_Password}, push_k_registration_token=${PushKDatabase.push_k_registration_token}, push_k_user_id=${PushKDatabase.push_k_user_id}, push_k_registration_createdAt=${PushKDatabase.push_k_registration_createdAt}")

        return PushKDatabase
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
        PushSDKLogger.debug("Initialization.hSdkInitSaveToLocal  started")
        val pushUuid = UUID.randomUUID().toString()
        PushSDKLogger.debug("Initialization.hSdkInit  pushUuid=$pushUuid, deviceId=$deviceId, push_k_user_msisdn=$push_k_user_msisdn, push_k_user_Password=$push_k_user_Password, push_k_registration_token=$push_k_registration_token, push_k_user_id=$push_k_user_id, push_k_registration_createdAt=$push_k_registration_createdAt")
        sharedPreferencesHandler.saveString("push_k_uuid", pushUuid)
        sharedPreferencesHandler.saveString("deviceId", deviceId)
        sharedPreferencesHandler.saveString("push_k_user_msisdn", push_k_user_msisdn)
        sharedPreferencesHandler.saveString("push_k_user_Password", push_k_user_Password)
        sharedPreferencesHandler.saveString("push_k_registration_token", push_k_registration_token)
        sharedPreferencesHandler.saveString("push_k_user_id", push_k_user_id)
        sharedPreferencesHandler.saveString("push_k_registration_createdAt", push_k_registration_createdAt)
        sharedPreferencesHandler.save("registrationstatus", registrationStatus)

        PushKDatabase.push_k_uuid = pushUuid
        PushKDatabase.deviceId = deviceId
        PushKDatabase.push_k_user_msisdn = push_k_user_msisdn
        PushKDatabase.push_k_user_Password = push_k_user_Password
        PushKDatabase.push_k_registration_createdAt = push_k_registration_createdAt
        PushKDatabase.push_k_user_id = push_k_user_id
        PushKDatabase.push_k_registration_token = push_k_registration_token
        PushKDatabase.registrationStatus = registrationStatus
    }


    fun clearData() {
        //sharedPreference.clearSharedPreference()
        sharedPreferencesHandler.save("registrationstatus", false)
        sharedPreferencesHandler.saveString("deviceId", "")
        sharedPreferencesHandler.saveString("push_k_user_id", "")
        PushKDatabase.registrationStatus = false
        PushSDKLogger.debug("Initialization.clearData  processed")
    }
}
