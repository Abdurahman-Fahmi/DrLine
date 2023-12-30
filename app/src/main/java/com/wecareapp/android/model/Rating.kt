package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class Rating(

	@field:SerializedName("booking_id")
	val bookingId: String? = null,

	@field:SerializedName("doctor_id")
	val doctorId: String? = null,

	@field:SerializedName("profile_image")
	val profileImage: String? = null,

	@field:SerializedName("date_time")
	val dateTime: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("user_name")
	val userName: String? = null,

	@field:SerializedName("rating")
	val rating: String? = null,

	@field:SerializedName("comment")
	val comment: String? = null,

	@field:SerializedName("id")
	val id: String? = null
)