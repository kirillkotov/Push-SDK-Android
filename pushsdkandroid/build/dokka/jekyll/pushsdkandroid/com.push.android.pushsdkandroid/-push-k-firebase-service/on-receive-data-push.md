---
title: onReceiveDataPush -
---
//[pushsdkandroid](../../index.md)/[com.push.android.pushsdkandroid](../index.md)/[PushKFirebaseService](index.md)/[onReceiveDataPush](on-receive-data-push.md)



# onReceiveDataPush  
[androidJvm]  
Content  
open fun [onReceiveDataPush](on-receive-data-push.md)(appIsInForeground: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), remoteMessage: RemoteMessage)  
More info  


Called when the service receives a FCM push containing data



Override it without the "super" call, if you want to implement your own notifications or disable them



## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onReceiveDataPush/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>appIsInForeground| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onReceiveDataPush/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>whether the application is currently in foreground or background<br><br>
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onReceiveDataPush/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>remoteMessage| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onReceiveDataPush/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>received remote message object<br><br>
  
  



