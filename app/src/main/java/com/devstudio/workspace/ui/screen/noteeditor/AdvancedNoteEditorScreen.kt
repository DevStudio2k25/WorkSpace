package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devstudio.workspace.data.model.Note
import com.devstudio.workspace.ui.viewmodel.NoteViewModel
import com.devstudio.workspace.util.SecurePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedNoteEditorScreen(
    noteId: Long? = null,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }
    
    var currentNote by remember { mutableStateOf<Note?>(null) }
    
    // UI State
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var highlightedLines by remember { mutableStateOf<List<Int>>(emptyList()) }
    
    // AI enabled state from settings
    val aiEnabled by securePrefs.aiEnabled.collectAsState(initial = false)
    
    // Coroutine scope
    val scope = rememberCoroutineScope()
    
    // Load Note
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            val validNote = notes.find { it.id == noteId }
            if (validNote != null) {
                currentNote = validNote
                title = validNote.title
                content = validNote.content
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                EditorTopBar(
                    title = title,
                    onTitleChange = { title = it },
                    onAiClick = { }, // No action needed, AI input always visible
                    onBack = onBack,
                    showAiLanguageSelector = aiEnabled, // Show only if AI enabled
                    onSave = {
                        // Clear highlights on save
                        highlightedLines = emptyList()
                        
                        // Save note
                        val noteToSave = currentNote?.copy(
                            title = title.ifBlank { "Untitled" },
                            content = content,
                            updatedAt = System.currentTimeMillis()
                        ) ?: Note(
                            title = title.ifBlank { "Untitled" },
                            content = content,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.saveNote(noteToSave)
                        
                        // Go back after save
                        onBack()
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Main Content Area with line numbers
                NoteContentArea(
                    content = content,
                    onContentChange = { newContent ->
                        content = newContent
                    },
                    highlightedLines = highlightedLines
                )
                
                // Inline AI Assistant (permanent at bottom) - Show only if AI enabled
                if (aiEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                    ) {
                        InlineAiAssistant(
                            isVisible = true, // Always visible when AI enabled
                            onToggle = { }, // No toggle needed
                            viewModel = viewModel,
                            currentContent = content,
                            currentTitle = title,
                            onContentUpdate = { newContent ->
                                content = newContent
                            },
                            onTitleUpdate = { newTitle ->
                                title = newTitle
                            },
                            onScrollToBottom = {
                                // Scroll will happen automatically in NoteContentArea
                            },
                            onHighlightLines = { lines ->
                                highlightedLines = lines
                            }
                        )
                    }
                }
            }
        }
    }
}

