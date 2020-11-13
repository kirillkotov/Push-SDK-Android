---
title: onDisplayNotification -
---
//[pushsdkandroid](../../index.md)/[com.push.android.pushsdkandroid](../index.md)/[PushKFirebaseService](index.md)/[onDisplayNotification](on-display-notification.md)



# onDisplayNotification  
[androidJvm]  
Content  
open fun [onDisplayNotification](on-display-notification.md)(appIsInForeground: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), remoteMessage: RemoteMessage)  
More info  


Called when notification will be displayed this method displays a notification



Displaying a notification is not guaranteed if the application's notification limit has been reached.



The method will not be called if onReceiveDataPush wasn't called before



## See also  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onDisplayNotification/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>sendNotification| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onDisplayNotification/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>the default method for showing notifications<br><br>
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onDisplayNotification/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>appIsInForeground| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onDisplayNotification/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>whether the application is currently in foreground or background<br><br>
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onDisplayNotification/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>remoteMessage| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onDisplayNotification/#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>received remote message object<br><br>
  
  



