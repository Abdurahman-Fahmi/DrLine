package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class TimeSlot(

    @field:SerializedName("slot")
    val timeSlot: String? = null,

    var selected: Boolean = false,

    @field:SerializedName("booked_status")
    val bookedStatus: String? = null
)