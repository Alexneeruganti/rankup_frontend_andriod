package com.simats.rankup.coding

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface PistonApi {
    @POST("execute-code")
    fun executeCode(@Body request: PistonExclude): Call<PistonResponse>
}

object CompilerService {
    // Reroute compilation directly into the Python Backend to securely pass HackerEarth secrets
    private const val BASE_URL = com.simats.rankup.network.BackendApiService.BASE_URL

    val api: PistonApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PistonApi::class.java)
    }
}
