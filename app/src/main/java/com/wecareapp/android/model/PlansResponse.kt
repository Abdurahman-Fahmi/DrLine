package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class PlansResponse(

    @field:SerializedName("plans")
    val plans: List<PlansItem?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)

data class PlansItem(

    @field:SerializedName("plan_months")
    val planMonths: String? = null,

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("plan_for")
    val planFor: String? = null,

    @field:SerializedName("plan_price")
    val planPrice: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("plan_name")
    val planName: String? = null,

    var selected: Boolean = false
)
