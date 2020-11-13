//[pushsdkandroid](../../index.md)/[com.push.android.pushsdkandroid](../index.md)/[PushKFirebaseService](index.md)/[onMessageReceived](on-message-received.md)



# onMessageReceived  
[androidJvm]  
Content  
open override fun [onMessageReceived](on-message-received.md)(remoteMessage: RemoteMessage)  
More info  


Called when a message is received from firebase



## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onMessageReceived/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a>remoteMessage| <a name="com.push.android.pushsdkandroid/PushKFirebaseService/onMessageReceived/#com.google.firebase.messaging.RemoteMessage/PointingToDeclaration/"></a><br><br>the received message<br><br><br><br>There are two types of messages data messages and notification messages. Data messages are handled here in onMessageReceived whether the app is in the foreground or background. Data messages are the type traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app is in the foreground. When the app is in the background an automatically generated notification is displayed. When the user taps on the notification they are returned to the app. Messages containing both notification and data payloads are treated as notification messages. The Firebase console always sends notification messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options<br><br>
  
  



