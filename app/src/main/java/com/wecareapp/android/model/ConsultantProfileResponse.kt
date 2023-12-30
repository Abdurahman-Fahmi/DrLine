package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class ConsultantProfileResponse(

    @field:SerializedName("profile")
    val profile: List<Doctor?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)
