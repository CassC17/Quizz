package com.supdevinci.quizz.data

import com.supdevinci.quizz.service.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL="https://opentdb.com/"

    private val retrofit by lazy{
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService: ApiService by lazy { retrofit.create(com.supdevinci.quizz.service.ApiService::class.java) }
}

