package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class DoctorAppointmentsResponse(

	@field:SerializedName("appointments")
	val appointments: List<Appointment?>? = null,

	@field:SerializedName("data")
	val data: List<Appointment?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

