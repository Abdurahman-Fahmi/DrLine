package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Doctor(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("user_name")
    val userName: String? = null,

    @field:SerializedName("about")
    val about: String? = null,

    @field:SerializedName("certificate")
    val certificate: String? = null,

    @field:SerializedName("rating")
    val rating: String? = null,

    @field:SerializedName("per_hour")
    val perHour: String? = null,

    @field:SerializedName("from_time")
    val fromTime: String? = null,

    @field:SerializedName("doctor_id")
    val doctorId: String? = null,

    @field:SerializedName("category_id")
    val categoryId: String? = null,

    @field:SerializedName("contact_id")
    val contactId: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("doctor_categories")
    val doctorCategories: List<DoctorCategory?>? = null,

    @field:SerializedName("to_time")
    val toTime: String? = null,

    @field:SerializedName("customer_type")
    val customerType: String? = null,

    @field:SerializedName("profile_image")
    val profileImage: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("user_balance")
    val userBalance: Int? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("category_name")
    val categoryName: String? = null,

    @field:SerializedName("category_name_ar")
    val categoryNameAr: String? = null,

    @field:SerializedName("from_available_time")
    val fromAvailableTime: String? = null,

    @field:SerializedName("total_bookings")
    val totalBookings: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("to_available_time")
    val toAvailableTime: String? = null,

    @field:SerializedName("is_online")
    val isOnline: String? = null,

    @field:SerializedName("membership_expiry")
    val membershipExpiry: String? = null

) : Serializable