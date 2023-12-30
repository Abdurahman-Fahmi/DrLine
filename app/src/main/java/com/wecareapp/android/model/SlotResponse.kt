package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class SlotResponse(

	@field:SerializedName("booking_id")
	val bookingId: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
