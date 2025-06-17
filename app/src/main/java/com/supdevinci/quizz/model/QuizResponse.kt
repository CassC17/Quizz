package com.supdevinci.quizz.model

data class QuizResponse (
    val response_code: Int,
    val results: List<QuizQuestion>
)