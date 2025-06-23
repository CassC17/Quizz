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
import com.supdevinci.quizz.model.UserEntity
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
        Log.d("QUIZ", "initialised pseudo: $pseudo")
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun loadQuestions(category: Int?, difficulty: String) {
        if (pseudo == null) {
            setError("no pseudo")
            Log.e("QUIZ", "error: pseudo not initialised before loadQuestions()")
            return
        }

        viewModelScope.launch {
            try {
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
                            setError("questions not received")
                        }
                    }
                    4 -> {
                        TokenManager.resetToken()
                        loadQuestions(category, difficulty)
                    }
                    else -> setError("invalid code: ${response.response_code}")
                }
            } catch (e: Exception) {
                setError("network error: ${e.localizedMessage ?: "unknown"}")
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
            setError("END")
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

        Thread {
            try {
                val user = userDao.findUserByPseudo(currentPseudo)
                val currentScore = _score.value
                val updatedAt = Date()

                if (user != null) {
                    val updatedUser = user.copy(
                        score = currentScore,
                        maxScore = maxOf(user.maxScore, currentScore),
                        updatedAt = updatedAt
                    )
                    Log.d("QUIZ", "update user: $updatedUser")
                    userDao.updateUser(updatedUser)
                } else {
                    val newUser = UserEntity(
                        pseudo = currentPseudo,
                        score = currentScore,
                        maxScore = currentScore,
                        createdAt = updatedAt,
                        updatedAt = updatedAt
                    )
                    Log.d("QUIZ", "insert user: $newUser")
                    userDao.insertUser(newUser)
                }
            } catch (e: Exception) {
                setError("save error: ${e.localizedMessage}")
            }
        }.start()
    }

    private fun String.decodeBase64(): String {
        return String(Base64.decode(this, Base64.DEFAULT), Charsets.UTF_8)
    }
}
