# Push-SDK-Android

Installation

        dependencies {
            implementation 'com.github.kirillkotov:Push-SDK-Android:v1.0.0.43'
        }


Initialization:

        val hPlatformPushAdapterSdk =
            PushSDK(
                context = this,
                platform_branch = PushSdkParametersPublic.branchTestValue,
                log_level = "debug",
                push_style = 1
            )
            
Using:

        val sdkAnswer: PushKFunAnswerGeneral = hPlatformPushAdapterSdk.push_get_message_history(7200)
        println(sdkAnswer)

            

