package com.supdevinci.quizz.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.supdevinci.quizz.viewmodel.MainViewModel
import com.supdevinci.quizz.components.DropdownMenuWithSelection

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.loadCategories()

        setContent {
            MainScreen(viewModel = viewModel,
                onStartQuiz = { pseudo, categoryId, difficulty ->
                    viewModel.insertUser(pseudo)
                    val intent = Intent(this, QuizActivity::class.java).apply {
                        putExtra("pseudo", pseudo)
                        putExtra("category", categoryId)
                        putExtra("difficulty", difficulty)
                    }
                    startActivity(intent)
                },
                onGoToLeaderboard = {
                    startActivity(Intent(this, BoardActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onStartQuiz: (String, Int?, String) -> Unit,
    onGoToLeaderboard: () -> Unit
) {
    val context = LocalContext.current

    val pseudo by viewModel.pseudo.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = pseudo,
            onValueChange = { viewModel.updatePseudo(it) },
            label = { Text("Pseudo") },
            modifier = Modifier.fillMaxWidth(),
            isError = pseudo.isBlank(),
            supportingText = {
                if (pseudo.isBlank()) Text("can not be empty")
            }
        )

        DropdownMenuWithSelection(
            items = categories,
            selectedItem = selectedCategory,
            onItemSelected = { viewModel.updateCategory(it) },
            label = "Category"
        )

        DropdownMenuWithSelection(
            items = listOf("easy", "medium", "hard"),
            selectedItem = selectedDifficulty,
            onItemSelected = { viewModel.updateDifficulty(it) },
            label = "Difficulty"
        )

        Button(
            onClick = {
                if (pseudo.isBlank()) {
                    Toast.makeText(context, "your pseudo", Toast.LENGTH_SHORT).show()
                } else {
                    onStartQuiz(pseudo, viewModel.getCategoryId(), selectedDifficulty)
                }
            },
            enabled = categories.isNotEmpty()
        ) {
            Text("START")
        }

        OutlinedButton(
            onClick = onGoToLeaderboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🏆 Leaderboard")
        }
    }
}
