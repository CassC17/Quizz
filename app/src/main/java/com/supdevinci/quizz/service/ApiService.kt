package com.supdevinci.quizz.service

import com.supdevinci.quizz.model.QuizResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("api.php")
    suspend fun getQuestion(@Query("amount") amount: Int = 1): QuizResponse
}