package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class PaymentChart(

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("month")
    val month: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null
)