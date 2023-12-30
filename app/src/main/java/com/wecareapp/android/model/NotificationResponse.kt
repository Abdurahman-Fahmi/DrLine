package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(

    @field:SerializedName("data")
    val data: List<Notification?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)
