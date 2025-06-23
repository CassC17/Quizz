package com.supdevinci.quizz.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.supdevinci.quizz.ui.theme.QuizzTheme
import com.supdevinci.quizz.viewmodel.BoardViewModel

class BoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val boardViewModel: BoardViewModel by viewModels()

        setContent {
            QuizzTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LeaderboardScreen(viewModel = boardViewModel)
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen(viewModel: BoardViewModel) {
    val users by viewModel.leaderboard.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaAnimation"
    )

    LaunchedEffect(users) {
        Log.d("LEADERBOARD", "Fetched users: $users")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("\uD83C\uDFC6 Leaderboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("users not found")
            }
        } else {
            users.forEachIndexed { index, user ->
                val borderColor = if (index == 0) MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha)
                else MaterialTheme.colorScheme.outlineVariant

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = if (index == 0) 3.dp else 1.dp,
                            color = borderColor,
                            shape = MaterialTheme.shapes.medium
                        ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.", modifier = Modifier.width(32.dp))
                        Text(user.pseudo, modifier = Modifier.weight(1f))
                        Text("Score: ${user.maxScore}")
                    }
                }
            }
        }
    }
}
