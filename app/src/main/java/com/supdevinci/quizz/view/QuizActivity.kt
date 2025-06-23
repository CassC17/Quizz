package com.supdevinci.quizz.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.supdevinci.quizz.ui.theme.QuizzTheme
import com.supdevinci.quizz.viewmodel.QuizViewModel

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pseudo = intent.getStringExtra("pseudo")
        val category = intent.getIntExtra("category", -1).takeIf { it != -1 }
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"

        val viewModel: QuizViewModel by viewModels()

        if (!pseudo.isNullOrBlank()) {
            viewModel.initUser(pseudo)
            viewModel.loadQuestions(category, difficulty)
        } else {
            viewModel.setError("pseudo missing from intent")
        }

        setContent {
            QuizzTheme {
                QuizScreen(
                    viewModel = viewModel,
                    onFinished = {
                        startActivity(Intent(this, BoardActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, onFinished: () -> Unit) {
    val index by viewModel.currentIndex.collectAsState()
    val question by viewModel.question.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isValidated by viewModel.isAnswerValidated.collectAsState()
    val score by viewModel.score.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    if (error != null) {
        Column(Modifier.padding(16.dp)) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            if (error!!.contains("END", ignoreCase = true)) {
                Button(onClick = onFinished) {
                    Text("SCORE")
                }
            }
        }
        return
    }

    if (question == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Score: $score", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Question ${index + 1}/10", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = question!!.question, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        val correctAnswer = question!!.correct_answer

        answers.forEach { answer ->
            val backgroundColor = when {
                isValidated && answer == correctAnswer -> MaterialTheme.colorScheme.primaryContainer
                isValidated && answer == selectedAnswer && answer != correctAnswer -> MaterialTheme.colorScheme.errorContainer
                selectedAnswer == answer -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Surface(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isValidated) {
                        viewModel.selectAnswer(answer)
                    }
            ) {
                Text(
                    text = answer,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.validateAnswer() },
                enabled = selectedAnswer != null && !isValidated
            ) {
                Text("CONFIRM")
            }

            Button(
                onClick = { viewModel.nextQuestion() },
                enabled = isValidated
            ) {
                Text("NEXT")
            }
        }
    }
}
