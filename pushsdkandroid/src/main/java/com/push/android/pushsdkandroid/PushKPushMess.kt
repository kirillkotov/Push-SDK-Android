package com.push.android.pushsdkandroid

/**
 * An object used for storing and exchanging data, will surely need to be redone, as it is no good;
 * Will be deprecated in later versions.
 * TODO fixme remove this, find a better way
 */
object PushKPushMess {
    /**
     * Latest message received
     */
    var message: String? = null   //global variable for push messages

    /**
     * SDK logging level; Can be "error" or "debug"
     */
    var log_level_active: String = "error" //global variable sdk log level
}