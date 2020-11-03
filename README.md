# Push-SDK-Android

Installation


dependencies {
        implementation 'com.github.Incuube:Hyber-SVC-SDK-Android:v1.0.0.43'
}


Initialization:

        val hPlatformPushAdapterSdk =
            PushSDK(
                context = this,
                platform_branch = PushSdkParametersPublic.branchTestValue,
                log_level = "debug",
                push_style = 1
            )
            

