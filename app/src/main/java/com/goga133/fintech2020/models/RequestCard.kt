package com.goga133.fintech2020.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RequestCard(
    @SerializedName("result") val result: List<Card>?,
    @SerializedName("totalCount") val totalCount: Int?
) : Serializable