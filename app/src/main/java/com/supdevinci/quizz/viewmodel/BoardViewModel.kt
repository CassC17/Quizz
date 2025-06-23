package com.supdevinci.quizz.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.quizz.data.local.UserDatabase
import com.supdevinci.quizz.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoardViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = UserDatabase.getDatabase(application.applicationContext).userDao()

    private val _leaderboard = MutableStateFlow<List<UserEntity>>(emptyList())
    val leaderboard: StateFlow<List<UserEntity>> = _leaderboard

    init {
        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        viewModelScope.launch {
            try {
                val scores = withContext(Dispatchers.IO) {
                    userDao.getBestScores()
                }
                _leaderboard.value = scores
                Log.d("LEADERBOARD", "Scores loaded: $scores")
            } catch (e: Exception) {
                Log.e("LEADERBOARD", "Failed to load scores", e)
            }
        }
    }
}
