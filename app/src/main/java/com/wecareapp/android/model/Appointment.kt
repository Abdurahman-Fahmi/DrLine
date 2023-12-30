package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class Appointment(

    @field:SerializedName("booking_id")
    val bookingId: String? = null,

    @field:SerializedName("transaction_id")
    val transactionId: String? = null,

    @field:SerializedName("profile_image")
    val profileImage: String? = null,

    @field:SerializedName("book_time")
    val bookTime: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("doctor_id")
    val doctorId: String? = null,

    @field:SerializedName("slot_price")
    val slotPrice: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("user_name")
    val userName: String? = null,

    @field:SerializedName("patient_name")
    val patientName: String? = null,

    @field:SerializedName("book_date")
    val bookDate: String? = null,

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("user_review_status")
    val userReviewStatus: String? = null,

    @field:SerializedName("doctor_review_status")
    val doctorReviewStatus: String? = null,

    @field:SerializedName("reminder_status")
    val reminderStatus: String? = null,

    @field:SerializedName("text")
    val text: String? = null
)