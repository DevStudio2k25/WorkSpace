package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.util.SecurePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    title: String,
    onTitleChange: (String) -> Unit,
    onAiClick: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    showAiLanguageSelector: Boolean = true // New parameter
) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }
    val scope = rememberCoroutineScope()
    
    var currentLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        securePrefs.aiLanguage.collect { currentLanguage = it }
    }
    
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        title = {
            // Editable Title Field
            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Untitled", fontWeight = FontWeight.Bold) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true
            )
        },
        actions = {
            // Language Selector - Show only if AI enabled
            if (showAiLanguageSelector) {
                Box {
                    IconButton(onClick = { showLanguageMenu = true }) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        listOf("English", "Hindi", "Hinglish").forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (currentLanguage == lang) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(20.dp))
                                        }
                                        Text(lang)
                                    }
                                },
                                onClick = {
                                    scope.launch {
                                        securePrefs.setAiLanguage(lang)
                                        currentLanguage = lang
                                    }
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Save
            IconButton(onClick = onSave) {
                Icon(Icons.Default.Check, "Save")
            }
        }
    )
}
