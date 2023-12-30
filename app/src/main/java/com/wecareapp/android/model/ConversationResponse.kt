package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName

data class ConversationResponse(

    @field:SerializedName("my_name")
    val myName: String? = null,

    @field:SerializedName("my_profile_pic")
    val myProfilePic: String? = null,

    @field:SerializedName("you_online_status")
    val youOnlineStatus: String? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("you_name")
    val youName: String? = null,

    @field:SerializedName("conversation")
    val conversation: List<Conversation?>? = null,

    @field:SerializedName("you_profile_pic")
    val youProfilePic: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("chat_id")
    val chatId: String? = null,

    @field:SerializedName("chat_access")
    val chatAccess: String? = null,

    @field:SerializedName("channel_name")
    val channelName: String? = null,

    @field:SerializedName("agora_token")
    val agoraToken: String? = null,

    @field:SerializedName("you_user_firebase_token")
    val youUserFirebaseToken: String? = null,

    @field:SerializedName("slot_price")
    val slotPrice: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("booked_status")
    val bookedStatus: String? = null,

    @field:SerializedName("base_url")
    val baseUrl: String? = null,


    )

data class Conversation(

    @field:SerializedName("my_id")
    val myId: String? = null,

    @field:SerializedName("you_id")
    val youId: String? = null,

    @field:SerializedName("slot_price")
    val slotPrice: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("booked_status")
    val bookedStatus: String? = null,

    @field:SerializedName("date_of_message")
    val dateOfMessage: String? = null,

    @field:SerializedName("book_time_start")
    val bookTimeStart: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("book_date")
    val bookDate: String? = null,

    @field:SerializedName("book_time_end")
    val bookTimeEnd: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("sending_type")
    val sendingType: String? = null,

    @field:SerializedName("myimage")
    val myimage: String? = null,

    @field:SerializedName("slot_booking_id")
    val slotBookingId: String? = null,

    @field:SerializedName("slot_request_status")
    val slotRequestStatus: String? = null,

    @field:SerializedName("slot_payment_status")
    val slotPaymentStatus: String? = null
)
