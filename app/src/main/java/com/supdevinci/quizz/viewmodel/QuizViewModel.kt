package com.supdevinci.quizz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.quizz.data.RetrofitInstance
import com.supdevinci.quizz.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel: ViewModel()  {

    private val _question = MutableStateFlow<QuizQuestion?>(null)
    val question: StateFlow<QuizQuestion?> = _question

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchQuestion() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getQuestion() //reponse.isNoEmpty()
                if (response.response_code == 0) {
                    val questionResult = response.results.firstOrNull()
                    if (questionResult != null) {
                        _question.value = questionResult
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "not found"
                    }
                } else {
                    _errorMessage.value = "invalid code: ${response.response_code}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "network error: ${e.localizedMessage}"
            }
        }
    }
}