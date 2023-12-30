package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class Notification(

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("time")
    val time: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("image")
    val image: String? = null,
)