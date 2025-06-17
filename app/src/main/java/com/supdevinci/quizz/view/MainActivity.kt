package com.supdevinci.quizz.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.supdevinci.quizz.data.RetrofitInstance
import com.supdevinci.quizz.model.QuizQuestion
import com.supdevinci.quizz.ui.theme.QuizzTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzTheme {
                QuizScreen()
            }
        }
    }
}

@Composable
fun QuizScreen() {
    var question by  remember { mutableStateOf<QuizQuestion?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitInstance.apiService.getQuestion()
                if (response.response_code == 0) {
                    question = response.results.firstOrNull()
                    error = null
                } else {
                    error = "invalid code: ${response.response_code}"
                }
            } catch (e: Exception) {
                error = "network error: ${e.localizedMessage ?: "Unknown"}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        error?.let {
            Text(text = it, color = Color.Red)
        }

        question?.let { q ->
            Text(text = q.question)
            val answers = (q.incorrect_answers + q.correct_answer).shuffled()
            answers.forEach { answer ->
                Button(onClick = { /* gérer la réponse */ }) {
                    Text(answer)
                }
            }
        }
    }
}
