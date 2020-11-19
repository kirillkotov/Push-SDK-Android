package com.push.android.pushsdkandroid

/**
 * Api handler parameters
 */
class ApiParams {

    //values themselves, no need for explanation
    var baseURL = ""
    var apiVersion = "3.0"
    var headerClientApiKey = "X-Push-Client-API-Key"
    var headerAppFingerprint = "X-Push-App-Fingerprint"
    var headerSessionId = "X-Push-Session-Id"
    var headerTimestamp = "X-Push-Timestamp"
    var headerAuthToken = "X-Push-Auth-Token"

    var deviceUpdatePath = "/device/update"
    var deviceRegistrationPath = "/device/registration"
    var deviceRevokePath = "/device/revoke"
    var getDeviceAllPath = "/device/all"
    var messageCallbackPath = "/message/callback"
    var messageDeliveryReportPath = "/message/dr"
    var messageQueuePath = "/message/queue"
    var messageHistoryPath = "/message/history"

    /**
     * Enum of possible paths
     */
    enum class ApiPaths {
        DEVICE_UPDATE,
        DEVICE_REGISTRATION,
        DEVICE_REVOKE,
        GET_DEVICE_ALL,
        MESSAGE_CALLBACK,
        MESSAGE_DELIVERY_REPORT,
        MESSAGE_QUEUE,
        MESSAGE_HISTORY
    }

    //goes like "https://api.com/api" + "3.0" + "/device/update"
    /**
     * Get full URL path, e.g. https://example.io/api/2.3/message/dr
     * @param path which path to get full URL for
     */
    fun getFullURLFor(path: ApiPaths): String {
        return "$baseURL/$apiVersion${when (path) {
            ApiPaths.DEVICE_UPDATE -> deviceUpdatePath
            ApiPaths.DEVICE_REGISTRATION -> deviceRegistrationPath
            ApiPaths.DEVICE_REVOKE -> deviceRevokePath
            ApiPaths.GET_DEVICE_ALL -> getDeviceAllPath
            ApiPaths.MESSAGE_CALLBACK -> messageCallbackPath
            ApiPaths.MESSAGE_DELIVERY_REPORT -> messageDeliveryReportPath
            ApiPaths.MESSAGE_QUEUE -> messageQueuePath
            ApiPaths.MESSAGE_HISTORY -> messageHistoryPath
        }}"
    }

    /**
     * Set values from another instance, for convenience
     * @param apiParams another instance to take params from
     */
    fun setFrom(apiParams: ApiParams) {
        this.baseURL = apiParams.baseURL
        this.apiVersion = apiParams.apiVersion
        this.headerClientApiKey = apiParams.headerClientApiKey
        this.headerAppFingerprint = apiParams.headerAppFingerprint
        this.headerSessionId = apiParams.headerSessionId
        this.headerTimestamp = apiParams.headerTimestamp
        this.headerAuthToken = apiParams.headerAuthToken
        this.deviceUpdatePath = apiParams.deviceUpdatePath
        this.deviceRegistrationPath = apiParams.deviceRegistrationPath
        this.deviceRevokePath = apiParams.deviceRevokePath
        this.getDeviceAllPath = apiParams.getDeviceAllPath
        this.messageCallbackPath = apiParams.messageCallbackPath
        this.messageDeliveryReportPath = apiParams.messageDeliveryReportPath
        this.messageQueuePath = apiParams.messageQueuePath
        this.messageHistoryPath = apiParams.messageHistoryPath
    }
}