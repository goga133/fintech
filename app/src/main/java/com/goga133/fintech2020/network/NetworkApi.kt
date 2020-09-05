package com.goga133.fintech2020.network

import com.goga133.fintech2020.models.Card
import com.goga133.fintech2020.models.RequestCard
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NetworkApi {
    @GET("{section}/{pageId}?json=true")
    fun getCards(@Path("section") section: String, @Path("pageId") pageId: Int): Call<RequestCard>

    @GET("/random?json=true")
    fun getRandomCard(): Call<Card>
}