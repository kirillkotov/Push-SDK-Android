---
title: onDeletedMessages -
---
//[pushsdkandroid](../../index.md)/[com.push.android.pushsdkandroid](../index.md)/[PushKFirebaseService](index.md)/[onDeletedMessages](on-deleted-messages.md)



# onDeletedMessages  
[androidJvm]  
Content  
open override fun [onDeletedMessages](on-deleted-messages.md)()  
More info  


In some situations, FCM may not deliver a message. This occurs when there are too many messages (>100) pending for your app on a particular device at the time it connects or if the device hasn't connected to FCM in more than one month. In these cases, you may receive a callback to FirebaseMessagingService.onDeletedMessages() When the app instance receives this callback, it should perform a full sync with your app server. If you haven't sent a message to the app on that device within the last 4 weeks, FCM won't call onDeletedMessages()

  



