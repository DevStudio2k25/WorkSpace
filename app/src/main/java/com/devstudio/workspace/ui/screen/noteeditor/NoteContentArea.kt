package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteContentArea(
    content: String,
    onContentChange: (String) -> Unit,
    highlightedLines: List<Int> = emptyList(),
    onScrollToBottom: () -> Unit = {}
) {
    val verticalScrollState = rememberScrollState()
    
    // Build annotated string with green color for highlighted lines
    val annotatedContent = remember(content, highlightedLines) {
        if (highlightedLines.isEmpty()) {
            AnnotatedString(content)
        } else {
            buildAnnotatedString {
                val lines = content.lines()
                lines.forEachIndexed { index, line ->
                    if (highlightedLines.contains(index)) {
                        // Green text for changed lines
                        withStyle(style = SpanStyle(color = Color(0xFF4CAF50))) {
                            append(line)
                        }
                    } else {
                        // Normal text
                        append(line)
                    }
                    // Add newline except for last line
                    if (index < lines.size - 1) {
                        append("\n")
                    }
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Text field with green text for changed lines
        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Default
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (content.isEmpty()) {
                        Text(
                            text = "Start writing...",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    } else if (highlightedLines.isNotEmpty()) {
                        // Show annotated text with green color
                        Text(
                            text = annotatedContent,
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily.Default
                            )
                        )
                    }
                    // Always show the actual text field for editing
                    Box(modifier = Modifier.fillMaxSize()) {
                        innerTextField()
                    }
                }
            },
            maxLines = Int.MAX_VALUE,
            singleLine = false
        )
    }
}
