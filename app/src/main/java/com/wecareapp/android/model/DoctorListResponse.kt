package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName


open class DoctorListResponse : BaseResponse() {
    @field:SerializedName("data")
    val data: List<Doctor?>? = null

    @field:SerializedName("doctors")
    val doctors: List<Doctor?>? = null
}
