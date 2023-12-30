package com.wecareapp.android.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Category(

    @field:SerializedName("category_name")
    val categoryName: String? = null,

    @field:SerializedName("category_id")
    val categoryId: String? = null,

    @field:SerializedName("icon")
    val icon: String? = null,

    var selected: Boolean? = false
) : Serializable
