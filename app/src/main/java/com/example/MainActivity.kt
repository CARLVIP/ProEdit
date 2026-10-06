package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Project
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.ProEditTheme
import com.example.viewmodel.EditorViewModel

enum class AppScreen {
    HOME,
    EDITOR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProEditTheme {
                val editorViewModel: EditorViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                val userProjects by editorViewModel.userProjects.collectAsState()

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            recentProjects = userProjects,
                            onOpenProject = { project: Project ->
                                editorViewModel.loadTemplate(project)
                                currentScreen = AppScreen.EDITOR
                            },
                            onNewProject = {
                                currentScreen = AppScreen.EDITOR
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppScreen.EDITOR -> {
                        BackHandler {
                            editorViewModel.pause()
                            currentScreen = AppScreen.HOME
                        }

                        EditorScreen(
                            viewModel = editorViewModel,
                            onNavigateBack = {
                                editorViewModel.pause()
                                currentScreen = AppScreen.HOME
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
