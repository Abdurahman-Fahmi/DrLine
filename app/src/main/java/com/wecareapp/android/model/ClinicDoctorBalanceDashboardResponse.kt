package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class ClinicDoctorBalanceDashboardResponse(

    @field:SerializedName("wallet")
    val wallet: String = "0",

    @field:SerializedName("payments")
    val payments: List<PaymentChart?>? = null,

    @field:SerializedName("stauts")
    val stauts: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("membership_expiry")
    val membershipExpiry: String? = null,

    @field:SerializedName("recent_appointments")
    val recentAppointments: List<Appointment?>? = null
)
