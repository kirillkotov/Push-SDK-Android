# Push-SDK-Android

Installation

        dependencies {
            implementation 'com.github.kirillkotov:Push-SDK-Android:v1.0.0.43'
        }

############################################

Initialization:

        val hPlatformPushAdapterSdk =
            PushSDK(
                context = this,
                log_level = "debug",
                push_style = 1,
                basePushURL = "https://example.com/push/{version}/"
            )
Call example:

        val sdkAnswer: PushKFunAnswerGeneral = hPlatformPushAdapterSdk.push_get_message_history(7200)
        println(sdkAnswer)

############################################

if you need specific path for procedure you can override it by platform_branch parameter

example:

            val branchSomeValue: UrlsPlatformList = UrlsPlatformList(
                fun_pushsdk_url_device_update = "device/update/test",
                fun_pushsdk_url_registration = "device/test/registration",
                fun_pushsdk_url_revoke = "device/revoke",
                fun_pushsdk_url_get_device_all = "device/all",
                fun_pushsdk_url_message_callback = "message/callback",
                fun_pushsdk_url_message_dr = "message/dr",
                fun_pushsdk_url_mess_queue = "message/queue",
                pushsdk_url_message_history = "message/history?startDate="
            )

            val hPlatformPushAdapterSdk =
                PushSDK(
                    context = this,
                    platform_branch = branchSomeValue,
                    log_level = "debug",
                    push_style = 1,
                    basePushURL = "https://example.com/push/{version}/"
                )





