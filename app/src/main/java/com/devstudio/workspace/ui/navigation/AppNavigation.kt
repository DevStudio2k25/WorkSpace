package com.devstudio.workspace.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.devstudio.workspace.ui.screen.*
import com.devstudio.workspace.ui.screen.vault.VaultScreen
import com.devstudio.workspace.ui.theme.AppTheme
import com.devstudio.workspace.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    noteViewModel: NoteViewModel,
    startDestination: String = Screen.Splash.route
) {
    // Shared ViewModels
    val vaultViewModel: com.devstudio.workspace.ui.viewmodel.VaultViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.NotesList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Notes List
        composable(Screen.NotesList.route) {
            NotesListScreen(
                viewModel = noteViewModel,
                onNoteClick = { note ->
                    navController.navigate(Screen.NoteEditor.createRoute(note.id))
                },
                onCreateNote = {
                    navController.navigate(Screen.NoteEditor.createRoute())
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onVaultAccess = {
                    // Navigate to PIN lock
                    navController.navigate(Screen.PinLock.route)
                }
            )
        }
        
        // Note Editor
        composable(Screen.NoteEditor.route) { backStackEntry ->
            val noteIdString = backStackEntry.arguments?.getString("noteId")
            val noteId = noteIdString?.toLongOrNull()
            
            NoteEditorScreen(
                noteId = noteId,
                viewModel = noteViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // PIN Lock - Smart flow
        composable(Screen.PinLock.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val securePrefs = remember { com.devstudio.workspace.util.SecurePreferences(context) }
            var isPinSet by remember { mutableStateOf<Boolean?>(null) }
            
            // Check if PIN is set
            LaunchedEffect(Unit) {
                isPinSet = securePrefs.vaultPin.first() != null
            }
            
            when (isPinSet) {
                null -> {
                    // Loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                false -> {
                    // No PIN set - go directly to vault
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Vault.route) {
                            popUpTo(Screen.NotesList.route)
                        }
                    }
                }
                true -> {
                    // PIN is set - check biometric first
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    val activity = context as? androidx.fragment.app.FragmentActivity
                    var showWrongPinError by remember { mutableStateOf(false) }
                    var showPinScreen by remember { mutableStateOf(false) }
                    var biometricChecked by remember { mutableStateOf(false) }
                    
                    // Check biometric on first load
                    LaunchedEffect(Unit) {
                        val biometricEnabled = securePrefs.useBiometric.first()
                        val isBiometricAvailable = activity?.let { 
                            com.devstudio.workspace.util.BiometricUtil.isBiometricAvailable(it)
                        } ?: false
                        
                        if (biometricEnabled && isBiometricAvailable && activity != null) {
                            // Show biometric prompt
                            com.devstudio.workspace.util.BiometricUtil.showBiometricPrompt(
                                activity = activity,
                                title = "Unlock Vault",
                                subtitle = "Use your fingerprint to unlock",
                                onSuccess = {
                                    // Biometric success - navigate to vault
                                    navController.navigate(Screen.Vault.route) {
                                        popUpTo(Screen.NotesList.route)
                                    }
                                },
                                onError = { error ->
                                    // Biometric error - show PIN screen
                                    showPinScreen = true
                                    biometricChecked = true
                                },
                                onFailed = {
                                    // Biometric failed - show PIN screen
                                    showPinScreen = true
                                    biometricChecked = true
                                }
                            )
                        } else {
                            // No biometric - show PIN screen directly
                            showPinScreen = true
                            biometricChecked = true
                        }
                    }
                    
                    // Show error effect
                    LaunchedEffect(showWrongPinError) {
                        if (showWrongPinError) {
                            kotlinx.coroutines.delay(2000)
                            showWrongPinError = false
                        }
                    }
                    
                    // Show PIN screen if biometric not used or failed
                    if (showPinScreen) {
                        PinLockScreen(
                            isSetup = false,
                            showError = showWrongPinError,
                            onPinComplete = { enteredPin ->
                                scope.launch {
                                    val savedPin = securePrefs.vaultPin.first()
                                    if (enteredPin == savedPin) {
                                        // Correct PIN - navigate to vault
                                        navController.navigate(Screen.Vault.route) {
                                            popUpTo(Screen.NotesList.route)
                                        }
                                    } else {
                                        // Wrong PIN - vibrate and show error
                                        showWrongPinError = true
                                        
                                        // Vibrate
                                        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator.vibrate(500)
                                        }
                                    }
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } else if (!biometricChecked) {
                        // Loading state while checking biometric
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
        
        // Vault
        composable(Screen.Vault.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            VaultScreen(
                viewModel = vaultViewModel,
                onItemClick = { item ->
                    if (item.itemType == com.devstudio.workspace.data.model.VaultItemType.VIDEO) {
                         navController.navigate(Screen.VaultVideoPlayer.createRoute(item.id))
                    } else {
                        // Navigate to Full Screen Image Viewer
                        navController.navigate(Screen.VaultImageViewer.createRoute(item.id))
                    }
                },
                onLockVault = {
                    navController.navigate(Screen.NotesList.route) {
                        popUpTo(Screen.NotesList.route) {
                            inclusive = false
                        }
                    }
                },
                onVaultSettings = {
                    navController.navigate(Screen.VaultSettings.route)
                }
            )
        }

        // Vault Image Viewer
        composable(Screen.VaultImageViewer.route) { backStackEntry ->
            val itemIdString = backStackEntry.arguments?.getString("itemId")
            val itemId = itemIdString?.toLongOrNull() ?: 0L

            com.devstudio.workspace.ui.screen.vault.VaultImageViewer(
                viewModel = vaultViewModel,
                initialItemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }

        // Vault Video Player
        composable(Screen.VaultVideoPlayer.route) { backStackEntry ->
            val itemIdString = backStackEntry.arguments?.getString("itemId")
            val itemId = itemIdString?.toLongOrNull() ?: 0L
            val item = vaultViewModel.vaultItems.collectAsState().value.find { it.id == itemId }

            if (item != null) {
                com.devstudio.workspace.ui.screen.vault.VaultVideoPlayer(
                    viewModel = vaultViewModel,
                    item = item,
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Handle error
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
        
        // Vault Item Editor (TODO: Create separate VaultItemEditor screen)
        composable(Screen.VaultItemEditor.route) { backStackEntry ->
            val itemIdString = backStackEntry.arguments?.getString("itemId")
            val itemId = itemIdString?.toLongOrNull()
            
            // Temporary: Using NoteEditor for vault items
            NoteEditorScreen(
                noteId = itemId,
                viewModel = noteViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Vault Settings (Hidden)
        composable(Screen.VaultSettings.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            VaultSettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onChangePattern = {
                    // Navigate to PIN setup
                    navController.navigate("pin_setup")
                },
                onChangeKeyword = {
                    // TODO: Handle keyword change
                }
            )
        }
        
        // PIN Setup (from vault settings)
        composable("pin_setup") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val securePrefs = remember { com.devstudio.workspace.util.SecurePreferences(context) }
            
            PinLockScreen(
                isSetup = true,
                onPinComplete = { newPin ->
                    kotlinx.coroutines.GlobalScope.launch {
                        securePrefs.setVaultPin(newPin)
                        navController.popBackStack()
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
