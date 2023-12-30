package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContactsResponse(

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("inbox")
    val inbox: List<InboxItem?>? = null,

    @field:SerializedName("status")
    val status: String? = null
) : Serializable

data class InboxItem(

    @field:SerializedName("customer_type")
    val customerType: String? = null,

    @field:SerializedName("unread_count")
    val unreadCount: String? = null,

    @field:SerializedName("profile_image")
    val profileImage: String? = null,

    @field:SerializedName("last_seen")
    val lastSeen: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("you_id")
    val youId: String? = null,

    @field:SerializedName("user_name")
    val userName: String? = null,

    @field:SerializedName("last_message")
    val lastMessage: String? = null,

    @field:SerializedName("is_online")
    val isOnline: String? = null
) : Serializable
