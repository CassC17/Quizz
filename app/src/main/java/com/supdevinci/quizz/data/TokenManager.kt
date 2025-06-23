package com.supdevinci.quizz.data

object TokenManager {
    private var token: String? = null

    suspend fun getToken(): String {
        if (token == null) {
            val response = RetrofitInstance.apiService.getToken()
            if (response.response_code == 0) {
                token = response.token
            } else {
                throw Exception("Failed to fetch token")
            }
        }
        return token!!
    }

    fun resetToken() {
        token = null
    }
}
