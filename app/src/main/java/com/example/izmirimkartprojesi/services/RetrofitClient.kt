package com.example.izmirimkartprojesi.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://acikveri.bizizmir.com/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val durakApi: DurakAPI by lazy {
        retrofit.create(DurakAPI::class.java)
    }
}