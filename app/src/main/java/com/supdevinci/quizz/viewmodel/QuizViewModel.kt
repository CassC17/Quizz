package com.supdevinci.quizz.viewmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.quizz.data.RetrofitInstance
import com.supdevinci.quizz.data.TokenManager
import com.supdevinci.quizz.data.local.UserDatabase
import com.supdevinci.quizz.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val _question = MutableStateFlow<QuizQuestion?>(null)
    val question: StateFlow<QuizQuestion?> = _question

    private val _answers = MutableStateFlow<List<String>>(emptyList())
    val answers: StateFlow<List<String>> = _answers

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer

    private val _isAnswerValidated = MutableStateFlow(false)
    val isAnswerValidated: StateFlow<Boolean> = _isAnswerValidated

    private val userDao = UserDatabase.getDatabase(application.applicationContext).userDao()

    private var questions: List<QuizQuestion> = emptyList()
    private var pseudo: String? = null

    fun initUser(pseudo: String) {
        this.pseudo = pseudo
    }

    fun loadQuestions(category: Int?, difficulty: String) {
        viewModelScope.launch {
            runCatching {
                val token = TokenManager.getToken()
                val response = RetrofitInstance.apiService.getQuestion(
                    amount = 10,
                    category = category,
                    difficulty = difficulty,
                    token = token
                )

                when (response.response_code) {
                    0 -> {
                        questions = response.results.map { q ->
                            val decodedQuestion = q.question.decodeBase64()
                            val decodedCorrectAnswer = q.correct_answer.decodeBase64()
                            val decodedIncorrectAnswers = q.incorrect_answers.map { it.decodeBase64() }

                            Log.d("QUIZ", "Q: $decodedQuestion")
                            Log.d("QUIZ", "Correct: $decodedCorrectAnswer")
                            decodedIncorrectAnswers.forEachIndexed { i, ans ->
                                Log.d("QUIZ", "Incorrect[$i]: $ans")
                            }

                            q.copy(
                                question = decodedQuestion,
                                correct_answer = decodedCorrectAnswer,
                                incorrect_answers = decodedIncorrectAnswers
                            )
                        }

                        if (questions.isNotEmpty()) {
                            _currentIndex.value = 0
                            updateQuestion()
                            _errorMessage.value = null
                        } else {
                            _errorMessage.value = "questions not recieved"
                        }
                    }
                    4 -> {
                        TokenManager.resetToken()
                        loadQuestions(category, difficulty)
                    }
                    else -> _errorMessage.value = "invalid code: ${response.response_code}"
                }
            }.onFailure {
                _errorMessage.value = "network error: ${it.localizedMessage ?: "inconnue"}"
            }
        }
    }

    private fun updateQuestion() {
        questions.getOrNull(_currentIndex.value)?.let { q ->
            _question.value = q
            _answers.value = (q.incorrect_answers + q.correct_answer).shuffled()
            _selectedAnswer.value = null
            _isAnswerValidated.value = false
        }
    }

    fun selectAnswer(answer: String) {
        if (!_isAnswerValidated.value) {
            _selectedAnswer.value = answer
        }
    }

    fun validateAnswer() {
        if (_isAnswerValidated.value || _selectedAnswer.value == null) return

        val correct = _question.value?.correct_answer
        if (_selectedAnswer.value == correct) {
            _score.value++
        }
        _isAnswerValidated.value = true
    }

    fun nextQuestion() {
        if (_currentIndex.value < questions.lastIndex) {
            _currentIndex.value++
            updateQuestion()
        } else {
            updateUserScoreIfBetter()
            _errorMessage.value = "END"
        }
    }

    fun resetQuiz() {
        _score.value = 0
        _currentIndex.value = 0
        _selectedAnswer.value = null
        _isAnswerValidated.value = false
    }

    private fun updateUserScoreIfBetter() {
        val currentPseudo = pseudo ?: return

        viewModelScope.launch {
            runCatching {
                val user = userDao.findUserByPseudo(currentPseudo)
                if (user != null) {
                    val currentScore = _score.value
                    val updatedUser = user.copy(
                        score = currentScore,
                        maxScore = maxOf(user.maxScore, currentScore),
                        updatedAt = Date()
                    )
                    userDao.updateUser(updatedUser)
                }
            }.onFailure {
                _errorMessage.value = "update error: ${it.localizedMessage}"
            }
        }
    }

    private fun String.decodeBase64(): String {
        return String(Base64.decode(this, Base64.DEFAULT), Charsets.UTF_8)
    }
}
