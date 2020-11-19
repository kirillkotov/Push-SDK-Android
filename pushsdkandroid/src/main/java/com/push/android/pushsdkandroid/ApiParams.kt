package com.push.android.pushsdkandroid

class ApiParams {
    var baseURL = ""
    var apiVersion = "3.0"
    var headerClientApiKey = "X-Push-Client-API-Key"
    var headerAppFingerprint = "X-Push-App-Fingerprint"
    var headerSessionId = "X-Push-Session-Id"
    var headerTimestamp = "X-Push-Timestamp"
    var headerAuthToken = "X-Push-Auth-Token"

    //goes like "https://api.com/api" + "3.0" + "/device/update"
    var deviceUpdatePath = "/device/update"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var deviceRegistrationPath = "/device/registration"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var deviceRevokePath = "/device/revoke"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var getDeviceAllPath = "/device/all"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var messageCallbackPath = "/message/callback"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var messageDeliveryReportPath = "/message/dr"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var messageQueuePath = "/message/queue"
        get() {
            return "$baseURL/$apiVersion$field"
        }
    var messageHistoryPath = "/message/history"
        get() {
            return "$baseURL/$apiVersion$field"
        }

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