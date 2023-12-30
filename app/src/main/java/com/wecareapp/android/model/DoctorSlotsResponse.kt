package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class DoctorSlotsResponse(

    @field:SerializedName("data")
    val data: List<DoctorSlotDataItem?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class DoctorSlotDataItem(

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

    @field:SerializedName("slot_price")
    val slotPrice: String? = null,

    @field:SerializedName("user_name")
    val userName: String? = null,

    @field:SerializedName("book_date")
    val bookDate: String? = null
)
