package com.push.android.pushsdkandroid.add

import android.app.Notification
import android.os.Build

internal object PushKInternal {

    fun notificationPriorityOld(priorityInfo: Int): Int {

        return if (Build.VERSION.SDK_INT >= 21) {
            when (priorityInfo) {
                0 -> Notification.DEFAULT_VIBRATE
                1 -> Notification.VISIBILITY_PUBLIC
                2 -> Notification.VISIBILITY_PUBLIC
                else -> Notification.DEFAULT_VIBRATE
            }
        } else {
            when (priorityInfo) {
                0 -> Notification.PRIORITY_DEFAULT
                1 -> Notification.PRIORITY_HIGH
                2 -> Notification.PRIORITY_MAX
                else -> Notification.PRIORITY_DEFAULT
            }
        }

    }

}