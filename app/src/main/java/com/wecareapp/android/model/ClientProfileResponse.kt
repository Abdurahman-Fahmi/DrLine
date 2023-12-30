package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class ClientProfileResponse(

    @field:SerializedName("profile")
    val profile: ClinicProfile? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)

data class ClinicProfile(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("clinic_2nd_image")
    val clinic2ndImage: String? = null,

    @field:SerializedName("default_category_id")
    val defaultCategoryId: String? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("doctors")
    val doctors: List<Doctor?>? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("rating")
    val rating: String? = null,

    @field:SerializedName("categories")
    val categories: List<Category?>? = null
)
