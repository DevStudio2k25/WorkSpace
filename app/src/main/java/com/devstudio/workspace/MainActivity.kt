package com.devstudio.workspace

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.devstudio.workspace.ui.navigation.AppNavigation
import com.devstudio.workspace.ui.theme.AppTheme
import com.devstudio.workspace.ui.theme.WorkspaceTheme
import com.devstudio.workspace.util.ThemeManager
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge immersive mode
        enableEdgeToEdge()
        
        // Hide system UI (status bar and navigation bar)
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager(context) }
            val selectedTheme by themeManager.selectedTheme.collectAsState(initial = AppTheme.FOREST)
            val scope = rememberCoroutineScope()
            
            // Initialize ViewModel
            val noteViewModel: com.devstudio.workspace.ui.viewmodel.NoteViewModel = 
                androidx.lifecycle.viewmodel.compose.viewModel()
            
            WorkspaceTheme(theme = selectedTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        currentTheme = selectedTheme,
                        onThemeChange = { newTheme ->
                            scope.launch {
                                themeManager.setTheme(newTheme)
                            }
                        },
                        noteViewModel = noteViewModel
                    )
                }
            }
        }
    }
}