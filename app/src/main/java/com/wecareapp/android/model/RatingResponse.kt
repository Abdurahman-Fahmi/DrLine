package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class RatingResponse(

	@field:SerializedName("data")
	val data: List<Rating?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
