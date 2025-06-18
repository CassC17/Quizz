package com.supdevinci.quizz.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.supdevinci.quizz.viewmodel.QuizViewModel
import com.supdevinci.quizz.ui.theme.QuizzTheme

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val category = intent.getIntExtra("category", -1).takeIf { it != -1 }
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"

        val viewModel: QuizViewModel by viewModels()
        viewModel.loadQuestions(category, difficulty)

        setContent {
            QuizzTheme {
                QuizScreen(viewModel = viewModel, onFinished = {
                    finish()
                })
            }
        }
    }
}

@Composable
fun QuizScreen(viewModel: QuizViewModel, onFinished: () -> Unit) {
    val question by viewModel.question.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val index by viewModel.currentIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isValidated by viewModel.isAnswerValidated.collectAsState()

    if (error != null) {
        Column(Modifier.padding(16.dp)) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            if (error!!.contains("END")) {
                Button(onClick = onFinished) {
                    Text("Voir le score")
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Question ${index + 1}/10", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = question!!.question)
        Spacer(modifier = Modifier.height(16.dp))

        val correctAnswer = question!!.correct_answer

        answers.forEach { answer ->
            val targetColor = when {
                isValidated && answer == correctAnswer -> MaterialTheme.colorScheme.primaryContainer
                isValidated && answer == selectedAnswer && answer != correctAnswer -> MaterialTheme.colorScheme.errorContainer
                selectedAnswer == answer -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                label = "AnswerButtonColor"
            )

            Button(
                onClick = { viewModel.selectAnswer(answer) },
                enabled = !isValidated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedColor,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(answer)
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
                Text("Valider")
            }

            Button(
                onClick = { viewModel.nextQuestion() },
                enabled = isValidated
            ) {
                Text("Suivant")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Score: $score", style = MaterialTheme.typography.bodyLarge)
    }
}
