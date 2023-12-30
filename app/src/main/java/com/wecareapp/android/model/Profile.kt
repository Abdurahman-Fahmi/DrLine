package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Profile(

    @field:SerializedName("profile_image")
    val profileImage: String? = null,

    @field:SerializedName("clinic_2nd_image")
    val clinicBackgroundImage: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("user_name")
    val userName: String? = null,

    @field:SerializedName("user_location")
    val userLocation: String? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("about")
    val about: String? = null,

    @field:SerializedName("certificate")
    val certificate: String? = null,

    @field:SerializedName("rating")
    val rating: String? = null,

    @field:SerializedName("contact_id")
    val contactId: String? = null,

    @field:SerializedName("customer_type")
    val customerType: Int? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("available_status")
    val availableStatus: String? = null,

    @field:SerializedName("total_spent")
    val totalSpent: String? = null,

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("appointments_completed")
    val appointmentsCompleted: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("total_appointments")
    val totalAppointments: String? = null,

    @field:SerializedName("category_name")
    val categoryName: String? = null,

    @field:SerializedName("category_name_ar")
    val categoryNameAr: String? = null
) : Serializable