package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun NoteContentArea(
    content: String,
    onContentChange: (String) -> Unit,
    onScrollToBottom: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Expose scroll to bottom function
    LaunchedEffect(Unit) {
        // This will be triggered by parent
    }
    
    // Auto-scroll when content changes significantly
    LaunchedEffect(content.length) {
        if (scrollState.maxValue > 0) {
            scope.launch {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp), // Space for AI input bar
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = { Text("Start writing...") }
        )
    }
}
