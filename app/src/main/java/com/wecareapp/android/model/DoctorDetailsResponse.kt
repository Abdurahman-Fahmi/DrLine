package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class DoctorDetailsResponse(

    @field:SerializedName("data")
    val data: List<Doctor?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)
