package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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

@Composable
fun NoteContentArea(
    content: String,
    onContentChange: (String) -> Unit,
    highlightedLines: List<Int> = emptyList(),
    onScrollToBottom: () -> Unit = {}
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    // Calculate line count dynamically
    val lineCount = remember(content) {
        if (content.isEmpty()) 1 else content.count { it == '\n' } + 1
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Line numbers column - synced with content scroll
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
        ) {
            // Background for line numbers
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
            
            // Horizontal lines under numbers (synced)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(verticalScrollState, enabled = false)
                    .padding(top = 12.dp)
            ) {
                repeat(lineCount) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {
                        // Bottom border for each line
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            // Line numbers on top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(verticalScrollState, enabled = false)
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                repeat(lineCount) { index ->
                    val isHighlighted = highlightedLines.contains(index)
                    Box(
                        modifier = Modifier.height(24.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isHighlighted) 
                                    Color(0xFF4CAF50) 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
        
        // Vertical divider
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
        
        // Content area - multi-line text field with horizontal scroll
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Horizontal lines background (notebook style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(verticalScrollState, enabled = false)
                    .padding(start = 12.dp, top = 12.dp)
            ) {
                repeat(lineCount) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {
                        // Bottom border for each line (notebook line)
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // More visible
                        )
                    }
                }
            }
            
            // Line highlights overlay
            if (highlightedLines.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(verticalScrollState, enabled = false)
                        .padding(start = 12.dp, end = 16.dp, top = 12.dp)
                ) {
                    repeat(lineCount) { index ->
                        val isHighlighted = highlightedLines.contains(index)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(
                                    if (isHighlighted) 
                                        Color(0xFF4CAF50).copy(alpha = 0.15f) 
                                    else 
                                        Color.Transparent
                                )
                                .border(
                                    width = if (isHighlighted) 1.dp else 0.dp,
                                    color = if (isHighlighted) 
                                        Color(0xFF4CAF50).copy(alpha = 0.3f) 
                                    else 
                                        Color.Transparent
                                )
                        )
                    }
                }
            }
            
            // Text field on top
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp, // Match line height
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Default,
                    baselineShift = androidx.compose.ui.text.style.BaselineShift(0.2f) // Shift text down to sit on line
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 100.dp), // Adjusted top padding
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
                        }
                        innerTextField()
                    }
                },
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )
        }
    }
}
