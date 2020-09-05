package com.goga133.fintech2020.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Card(
    @SerializedName("description") val description: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("previewURL") val previewURL : String?,
    @SerializedName("gifURL") val gifURL: String?
) : Serializable