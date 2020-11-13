# Push SDK Android

## Using the SDK

Get started by visiting:

* [Setting up your project to work with the SDK](https://github.com/kirillkotov/Push-SDK-Android/wiki/Setting-up-your-project-to-work-with-the-SDK)

* [SDK method reference page](https://github.com/kirillkotov/Push-SDK-Android/wiki/SDK-methods)

* [Receiving push messages and showing notifications](https://github.com/kirillkotov/Push-SDK-Android/wiki/Receiving-push-messages-and-showing-notifications)

### JavaDoc:
https://kirillkotov.github.io/Push-SDK-Android/com/push/android/pushsdkandroid/PushSDK.html

## Short dependency info

### Push SDK

Make sure you have declared maven repository in your project (top-level) `build.gradle`
```
allprojects {
    repositories {
        ...
        maven {
            url 'https://jitpack.io'
        }
    }
}
```
Add dependency to your module (app-level) `build.gradle`
```
dependencies {
    ...
    implementation 'com.github.kirillkotov:Push-SDK-Android:v1.0.0.47'
}
```

### Firebase cloud messaging

Recommended dependency versions to use within your app (or use whatever would be compatible with what is used within the SDK):
- Project (top-level) `build.gradle`:
```
buildscript {
    dependencies {
        ...
        classpath 'com.google.gms:google-services:4.3.4'
    }
}
```
- Module (app-level) `build.gradle`:
```
plugins {
    ...
    id 'com.google.gms.google-services'
}

dependencies {
    ...
    implementation 'com.google.firebase:firebase-messaging:21.0.0'
}
```
- Make sure you add proper `google-services.json` file to the root of your app module
