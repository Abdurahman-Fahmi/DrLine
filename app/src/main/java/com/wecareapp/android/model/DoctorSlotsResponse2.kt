package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class DoctorSlotsResponse2(

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("slots")
    val slots: List<TimeSlot>? = null,

    @field:SerializedName("user_name")
    val userName: String? = null,

    @field:SerializedName("reminder_status")
    val reminderStatus: String? = null,

    @field:SerializedName("per_hour")
    val perHour: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)
