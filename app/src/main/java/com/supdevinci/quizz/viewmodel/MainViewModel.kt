package com.supdevinci.quizz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.quizz.data.RetrofitInstance
import com.supdevinci.quizz.data.local.UserDatabase
import com.supdevinci.quizz.data.local.UserDao
import com.supdevinci.quizz.model.Category
import com.supdevinci.quizz.model.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _pseudo = MutableStateFlow("")
    val pseudo: StateFlow<String> get() = _pseudo

    private val _selectedCategory = MutableStateFlow(Category(-1, "Any"))
    val selectedCategory: StateFlow<Category> get() = _selectedCategory

    private val _selectedDifficulty = MutableStateFlow("easy")
    val selectedDifficulty: StateFlow<String> get() = _selectedDifficulty

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories

    private val _bestScores = MutableStateFlow<List<UserEntity>>(emptyList())
    val bestScores: StateFlow<List<UserEntity>> get() = _bestScores

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val userDao: UserDao =
        UserDatabase.getDatabase(application.applicationContext).userDao()

    // ✅ Setters publics
    fun updatePseudo(value: String) {
        _pseudo.value = value
    }

    fun updateCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun updateDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun loadCategories() {
        viewModelScope.launch {
            runCatching {
                val response = RetrofitInstance.apiService.getCategories()
                val list = response["trivia_categories"] ?: emptyList()
                _categories.value = listOf(Category(-1, "Any")) + list
            }.onFailure {
                _errorMessage.value = "Erreur chargement catégories : ${it.localizedMessage}"
            }
        }
    }

    fun insertUser(pseudo: String, score: Int = 0) {
        viewModelScope.launch {
            runCatching {
                val now = Date()
                val user = UserEntity(
                    pseudo = pseudo,
                    score = score,
                    maxScore = score,
                    createdAt = now,
                    updatedAt = now
                )
                userDao.insertUser(user)
                loadBestScores()
            }.onFailure {
                _errorMessage.value = "Erreur ajout utilisateur : ${it.localizedMessage}"
            }
        }
    }

    fun updateUserScoreAndReturn(userId: Int, newScore: Int, onUpdated: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val updatedRows = userDao.updateScore(userId, newScore, Date())
                if (updatedRows > 0) {
                    val updatedUser = userDao.findById(userId)
                    loadBestScores()
                    onUpdated(updatedUser)
                } else {
                    _errorMessage.value = "Aucun utilisateur trouvé avec l’ID $userId"
                    onUpdated(null)
                }
            }.onFailure {
                _errorMessage.value = "Erreur mise à jour score : ${it.localizedMessage}"
                onUpdated(null)
            }
        }
    }

    fun loadBestScores() {
        viewModelScope.launch {
            runCatching {
                _bestScores.value = userDao.getBestScores()
            }.onFailure {
                _errorMessage.value = "Erreur chargement scores : ${it.localizedMessage}"
            }
        }
    }

    suspend fun findUserByPseudo(pseudo: String): UserEntity? {
        return runCatching {
            userDao.findUserByPseudo(pseudo)
        }.onFailure {
            _errorMessage.value = "Erreur recherche utilisateur : ${it.localizedMessage}"
        }.getOrNull()
    }

    fun getCategoryId(): Int? = _selectedCategory.value.takeIf { it.id != -1 }?.id
}
