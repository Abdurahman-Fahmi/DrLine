package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class PatientProfileResponse(

    @field:SerializedName("profile")
    val profile: Profile? = null,

    @field:SerializedName("data")
    val data: List<Profile?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)