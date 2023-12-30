package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("result")
	val result: Profile? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
