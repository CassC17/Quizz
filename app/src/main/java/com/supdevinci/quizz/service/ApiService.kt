package com.supdevinci.quizz.service

import com.supdevinci.quizz.model.Category
import com.supdevinci.quizz.model.QuizResponse
import com.supdevinci.quizz.model.QuizToken
import com.supdevinci.quizz.model.UserEntity
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("api.php")
    suspend fun getQuestion(
        @Query("amount") amount: Int = 10,
        @Query("category") category: Int?,
        @Query("difficulty") difficulty: String?,
        @Query("token") token: String
    ): QuizResponse

    @GET("api_token.php")
    suspend fun getToken(@Query("command") command: String = "request"): QuizToken

    @GET("api_category.php")
    suspend fun getCategories(): Map<String, List<Category>>

}