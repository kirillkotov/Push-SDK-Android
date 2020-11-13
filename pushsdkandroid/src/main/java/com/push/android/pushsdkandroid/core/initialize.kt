package com.push.android.pushsdkandroid.core

import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.push.android.pushsdkandroid.PushKDatabase
import com.push.android.pushsdkandroid.logger.PushKLoggerSdk
import java.util.*


//function for initialization different parameters
internal class Initialization(val context: Context) {
    private val sharedPreference: SharedPreference = SharedPreference(context)

    private fun hSdkUpdateFirebaseAuto() {
        PushKLoggerSdk.debug("Initialization.hSdkUpdateFirebaseAuto start")
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            if (token != "") {
                sharedPreference.saveString("firebase_registration_token", token)
                PushKLoggerSdk.debug("Initialization.hSdkUpdateFirebaseAuto.Firebase token: $token")
            } else {
                PushKLoggerSdk.debug("Initialization.hSdkUpdateFirebaseAuto.Firebase token empty")
            }
        }
        PushKLoggerSdk.debug("Initialization.hSdkUpdateFirebaseAuto finished")


    }

    fun hSdkUpdateFirebaseManual(x_token: String): String {
        sharedPreference.saveString("firebase_registration_token", x_token)
        PushKLoggerSdk.debug("Initialization.hSdkUpdateFirebaseManual.Firebase token: $x_token")
        return x_token
    }


    fun hSdkGetParametersFromLocal(): PushOperativeData {
        PushKLoggerSdk.debug("Initialization.hSdkGetParametersFromLocal started")

        PushKDatabase = PushOperativeData()

        val registrationStatus: Boolean = sharedPreference.getValueBool("registrationstatus", false)
        PushKDatabase.registrationStatus = registrationStatus

        PushKLoggerSdk.debug("Initialization.hSdkGetParametersFromLocal registrationStatus: $registrationStatus")

        hSdkUpdateFirebaseAuto()

        //1
        val firebaseRegistrationToken: String =
            sharedPreference.getValueString("firebase_registration_token")!!.toString()
        PushKLoggerSdk.debug("Initialization.hSdkGetParametersFromLocal firebaseRegistrationToken: $firebaseRegistrationToken")

        if (firebaseRegistrationToken != "") {
            PushKDatabase.firebase_registration_token = firebaseRegistrationToken
        }

        if (registrationStatus) {

            //2
            val pushUuid: String = sharedPreference.getValueString("push_k_uuid")!!.toString()
            PushKDatabase.push_k_uuid = pushUuid

            //3
            val devId: String = sharedPreference.getValueString("deviceId")!!.toString()
            PushKDatabase.deviceId = devId

            //4
            val pushUserMsisdn: String =
                sharedPreference.getValueString("push_k_user_msisdn")!!.toString()
            PushKDatabase.push_k_user_msisdn = pushUserMsisdn

            //5
            val pushUserPassword: String =
                sharedPreference.getValueString("push_k_user_Password")!!.toString()
            PushKDatabase.push_k_user_Password = pushUserPassword

            //6
            val pushRegistrationToken: String =
                sharedPreference.getValueString("push_k_registration_token")!!.toString()
            PushKDatabase.push_k_registration_token = pushRegistrationToken

            //7
            val pushUserId: String =
                sharedPreference.getValueString("push_k_user_id")!!.toString()
            PushKDatabase.push_k_user_id = pushUserId

            //8
            val pushRegistrationCreatedAt: String =
                sharedPreference.getValueString("push_k_registration_createdAt")!!.toString()
            PushKDatabase.push_k_registration_createdAt = pushRegistrationCreatedAt
        }
        PushKLoggerSdk.debug("Initialization.paramsLoader finished: push_k_uuid=${PushKDatabase.push_k_uuid}, devId=${PushKDatabase.deviceId}, push_k_user_msisdn=${PushKDatabase.push_k_user_msisdn}, push_k_user_Password=${PushKDatabase.push_k_user_Password}, push_k_registration_token=${PushKDatabase.push_k_registration_token}, push_k_user_id=${PushKDatabase.push_k_user_id}, push_k_registration_createdAt=${PushKDatabase.push_k_registration_createdAt}")

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
        PushKLoggerSdk.debug("Initialization.hSdkInitSaveToLocal  started")
        val pushUuid = UUID.randomUUID().toString()
        PushKLoggerSdk.debug("Initialization.hSdkInit  pushUuid=$pushUuid, deviceId=$deviceId, push_k_user_msisdn=$push_k_user_msisdn, push_k_user_Password=$push_k_user_Password, push_k_registration_token=$push_k_registration_token, push_k_user_id=$push_k_user_id, push_k_registration_createdAt=$push_k_registration_createdAt")
        sharedPreference.saveString("push_k_uuid", pushUuid)
        sharedPreference.saveString("deviceId", deviceId)
        sharedPreference.saveString("push_k_user_msisdn", push_k_user_msisdn)
        sharedPreference.saveString("push_k_user_Password", push_k_user_Password)
        sharedPreference.saveString("push_k_registration_token", push_k_registration_token)
        sharedPreference.saveString("push_k_user_id", push_k_user_id)
        sharedPreference.saveString("push_k_registration_createdAt", push_k_registration_createdAt)
        sharedPreference.save("registrationstatus", registrationStatus)

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
        sharedPreference.save("registrationstatus", false)
        sharedPreference.saveString("deviceId", "")
        sharedPreference.saveString("push_k_user_id", "")
        PushKDatabase.registrationStatus = false
        PushKLoggerSdk.debug("Initialization.clearData  processed")
    }
}
