//[pushsdkandroid](../../index.md)/[com.push.android.pushsdkandroid](../index.md)/[PushKFirebaseService](index.md)/[onUnableToDisplayNotification](on-unable-to-display-notification.md)



# onUnableToDisplayNotification  
[androidJvm]  
Content  
open fun [onUnableToDisplayNotification](on-unable-to-display-notification.md)(areNotificationsEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), appIsInForeground: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), remoteMessage: RemoteMessage)  
More info  


Called when notification will not be displayed, so you can try displaying it manually. The method will not be called if onReceiveDataPush wasn't called before



## See also  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>[NotificationManagerCompat.areNotificationsEnabled](https://developer.android.com/reference/kotlin/androidx/core/app/NotificationManagerCompat.html#arenotificationsenabled)| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>appIsInForeground| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>whether the application is currently in foreground or background<br><br>
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>areNotificationsEnabled| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>whether notifications are enabled for the app<br><br>
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>remoteMessage| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onUnableToDisplayNotification/#kotlin.Boolean#kotlin.Boolean#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>received remote message object<br><br>
  
  



