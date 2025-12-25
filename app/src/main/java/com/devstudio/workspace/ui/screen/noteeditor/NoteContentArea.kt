package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun NoteContentArea(
    content: String,
    onContentChange: (String) -> Unit,
    highlightedLines: List<Int> = emptyList(),
    onScrollToBottom: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Auto-scroll when content changes
    LaunchedEffect(content.length) {
        if (scrollState.maxValue > 0) {
            scope.launch {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }
    
    val lines = remember(content) { content.lines() }
    var editingLineIndex by remember { mutableStateOf<Int?>(null) }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Line numbers column (VS Code style)
        Column(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .verticalScroll(scrollState)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            lines.forEachIndexed { index, _ ->
                val isHighlighted = highlightedLines.contains(index)
                Text(
                    text = "${index + 1}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isHighlighted) 
                            Color(0xFF4CAF50) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        // Vertical divider
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
        
        // Content area - line by line editing
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 100.dp)
        ) {
            if (lines.isEmpty() || (lines.size == 1 && lines[0].isEmpty())) {
                // Empty state
                Text(
                    text = "Start writing...",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                lines.forEachIndexed { index, line ->
                    val isHighlighted = highlightedLines.contains(index)
                    val isEditing = editingLineIndex == index
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    isHighlighted -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    isEditing -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isHighlighted) 1.dp else 0.dp,
                                color = if (isHighlighted) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color.Transparent
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        BasicTextField(
                            value = line,
                            onValueChange = { newLine ->
                                val newLines = lines.toMutableList()
                                newLines[index] = newLine
                                onContentChange(newLines.joinToString("\n"))
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 24.dp),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (line.isEmpty()) {
                                        Text(
                                            text = " ",
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp,
                                                color = Color.Transparent
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            onTextLayout = {
                                // Track which line is being edited
                                editingLineIndex = index
                            }
                        )
                    }
                }
            }
        }
    }
}
