package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ClinicListResponse(

	@field:SerializedName("data")
	val data: List<Clinic?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Clinic(

	@field:SerializedName("image")
	val image: Any? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("user_name")
	val userName: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("about")
	val about: String? = null,

	@field:SerializedName("certificate")
	val certificate: String? = null,

	@field:SerializedName("rating")
	val rating: String? = null,

	@field:SerializedName("clinic_id")
	val clinicId: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("is_online")
	val isOnline: String? = null
) : Serializable
