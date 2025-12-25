package com.devstudio.workspace.ui.screen.noteeditor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.ui.viewmodel.NoteViewModel
import com.devstudio.workspace.util.SecurePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class for diff segments
data class DiffSegment(
    val text: String,
    val isUnchanged: Boolean
)

@Composable
fun InlineAiAssistant(
    isVisible: Boolean,
    onToggle: () -> Unit,
    viewModel: NoteViewModel,
    currentContent: String,
    currentTitle: String,
    onContentUpdate: (newContent: String) -> Unit,
    onTitleUpdate: (newTitle: String) -> Unit,
    onScrollToBottom: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // AI state
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    
    // Typing effect state
    var isTypingInEditor by remember { mutableStateOf(false) }
    var typingText by remember { mutableStateOf("") }
    var typingIndex by remember { mutableStateOf(0) }
    
    // Input state
    var userInput by remember { mutableStateOf("") }
    var lastQuery by remember { mutableStateOf("") }
    
    // Detect if query is for replacement or addition
    fun shouldReplaceContent(query: String): Boolean {
        val lowerQuery = query.lowercase()
        
        // Keywords that indicate replacement
        val replaceKeywords = listOf(
            "edit", "update", "improve", "fix", "rewrite", "change", "modify",
            "better", "correct", "enhance", "refine", "summarize", "shorten", "simplify",
            "explain", "elaborate", "detail", "expand on"
        )
        
        // Keywords that indicate addition
        val addKeywords = listOf(
            "add more", "write more", "continue writing", "add new", "create new"
        )
        
        // Check for add keywords first (more specific)
        if (addKeywords.any { lowerQuery.contains(it) }) {
            return false
        }
        
        // Check for replace keywords
        if (replaceKeywords.any { lowerQuery.contains(it) }) {
            return true
        }
        
        // Default: if content exists and query is short, assume replacement
        return currentContent.isNotBlank()
    }
    
    // Smart diff-based content merge
    fun smartMergeContent(oldContent: String, newContent: String): List<DiffSegment> {
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()
        val segments = mutableListOf<DiffSegment>()
        
        var oldIndex = 0
        var newIndex = 0
        
        while (newIndex < newLines.size) {
            val newLine = newLines[newIndex]
            
            // Check if this line exists in old content (unchanged)
            if (oldIndex < oldLines.size && oldLines[oldIndex].trim() == newLine.trim()) {
                // Line unchanged - keep it as is (no typing needed)
                segments.add(DiffSegment(newLine, isUnchanged = true))
                oldIndex++
                newIndex++
            } else {
                // Line is new or modified - needs typing
                segments.add(DiffSegment(newLine, isUnchanged = false))
                newIndex++
            }
        }
        
        return segments
    }
    
    // Handle AI response - Smart diff-based typing
    LaunchedEffect(aiResponse) {
        if (aiResponse != null && !aiLoading && !isTypingInEditor) {
            var processedContent = aiResponse!!.trim()
            var extractedTitle: String? = null
            
            // Extract title if present
            if (processedContent.startsWith("TITLE:", ignoreCase = true)) {
                val lines = processedContent.lines()
                extractedTitle = lines.first()
                    .removePrefix("TITLE:")
                    .removePrefix("Title:")
                    .removePrefix("title:")
                    .trim()
                processedContent = lines.drop(1).joinToString("\n").trim()
            }
            
            // Clean markdown
            processedContent = processedContent
                .replace("**", "")
                .replace("__", "")
                .replace("~~", "")
                .replace(Regex("^#+\\s", RegexOption.MULTILINE), "")
            
            // Update title if extracted
            if (extractedTitle != null && (currentTitle.isBlank() || currentTitle == "Untitled")) {
                onTitleUpdate(extractedTitle)
            }
            
            // Determine if we should replace or append
            val shouldReplace = shouldReplaceContent(lastQuery)
            
            if (currentContent.isBlank() || !shouldReplace) {
                // Simple mode: empty content or append mode
                isTypingInEditor = true
                typingText = processedContent
                typingIndex = 0
                
                while (typingIndex < typingText.length) {
                    delay(15)
                    typingIndex++
                    
                    val textToInsert = typingText.substring(0, typingIndex)
                    val newContent = if (currentContent.isBlank()) {
                        textToInsert
                    } else {
                        currentContent.trimEnd() + "\n\n" + textToInsert
                    }
                    
                    onContentUpdate(newContent)
                    
                    if (typingIndex % 10 == 0) {
                        onScrollToBottom()
                    }
                }
                
                onScrollToBottom()
                isTypingInEditor = false
            } else {
                // Smart diff mode: replace with intelligent typing
                isTypingInEditor = true
                
                val diffSegments = smartMergeContent(currentContent, processedContent)
                val resultLines = mutableListOf<String>()
                
                for (segment in diffSegments) {
                    if (segment.isUnchanged) {
                        // Keep unchanged line as is (instant, no typing)
                        resultLines.add(segment.text)
                        onContentUpdate(resultLines.joinToString("\n"))
                        delay(50) // Small delay to show progression
                    } else {
                        // Type new/modified line character by character
                        var charIndex = 0
                        val lineToType = segment.text
                        
                        while (charIndex < lineToType.length) {
                            delay(15)
                            charIndex++
                            
                            val partialLine = lineToType.substring(0, charIndex)
                            val tempLines = resultLines.toMutableList()
                            tempLines.add(partialLine)
                            
                            onContentUpdate(tempLines.joinToString("\n"))
                            
                            if (charIndex % 10 == 0) {
                                onScrollToBottom()
                            }
                        }
                        
                        // Add complete line to result
                        resultLines.add(lineToType)
                        onContentUpdate(resultLines.joinToString("\n"))
                    }
                }
                
                onScrollToBottom()
                isTypingInEditor = false
            }
            
            viewModel.clearAiState()
        }
    }
    
    Box(modifier = modifier) {
        // Permanent Bottom Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Input field
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            if (aiLoading || isTypingInEditor) "AI is working..." 
                            else "Ask AI to write, edit, or continue..."
                        ) 
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    enabled = !aiLoading && !isTypingInEditor,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                // Send button
                FilledIconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            lastQuery = userInput // Store query for detection
                            viewModel.generateAiContent(userInput, currentContent)
                            userInput = ""
                        }
                    },
                    enabled = userInput.isNotBlank() && !aiLoading && !isTypingInEditor,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, "Send")
                    }
                }
            }
        }
        
        // Error display only
        if (aiError != null) {
            LaunchedEffect(aiError) {
                delay(3000)
                viewModel.clearAiState()
            }
            
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        aiError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
