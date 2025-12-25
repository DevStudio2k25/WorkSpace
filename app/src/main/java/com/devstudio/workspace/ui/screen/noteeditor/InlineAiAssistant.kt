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
import com.github.difflib.DiffUtils
import com.github.difflib.patch.*

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
    onHighlightLines: (List<Int>) -> Unit = {}, // Highlight changed lines
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
    
    // Smart Myers Diff-based content merge
    fun applyMyersDiff(oldContent: String, newContent: String): String {
        val originalLines = oldContent.lines()
        val newLines = newContent.lines()
        
        // Generate Myers diff patch
        val patch = DiffUtils.diff(originalLines, newLines)
        
        // Apply patch to get final content
        val finalLines = originalLines.toMutableList()
        
        // Process deltas in reverse to avoid index shifting issues
        patch.deltas.reversed().forEach { delta ->
            when (delta) {
                is InsertDelta -> {
                    // Insert new lines
                    finalLines.addAll(
                        delta.source.position,
                        delta.target.lines
                    )
                }
                is DeleteDelta -> {
                    // Delete lines
                    repeat(delta.source.lines.size) {
                        if (delta.source.position < finalLines.size) {
                            finalLines.removeAt(delta.source.position)
                        }
                    }
                }
                is ChangeDelta -> {
                    // Replace lines
                    repeat(delta.source.lines.size) {
                        if (delta.source.position < finalLines.size) {
                            finalLines.removeAt(delta.source.position)
                        }
                    }
                    finalLines.addAll(
                        delta.source.position,
                        delta.target.lines
                    )
                }
            }
        }
        
        return finalLines.joinToString("\n")
    }
    
    // Get line-by-line diff segments for typing animation
    fun getTypingSegments(oldContent: String, newContent: String): List<DiffSegment> {
        val originalLines = oldContent.lines()
        val newLines = newContent.lines()
        
        val patch = DiffUtils.diff(originalLines, newLines)
        val segments = mutableListOf<DiffSegment>()
        
        var currentLine = 0
        
        patch.deltas.forEach { delta ->
            // Add unchanged lines before this delta
            while (currentLine < delta.source.position) {
                if (currentLine < originalLines.size) {
                    segments.add(DiffSegment(originalLines[currentLine], isUnchanged = true))
                }
                currentLine++
            }
            
            // Add changed/new lines
            when (delta) {
                is InsertDelta -> {
                    delta.target.lines.forEach { line ->
                        segments.add(DiffSegment(line, isUnchanged = false))
                    }
                }
                is DeleteDelta -> {
                    // Skip deleted lines
                    currentLine += delta.source.lines.size
                }
                is ChangeDelta -> {
                    delta.target.lines.forEach { line ->
                        segments.add(DiffSegment(line, isUnchanged = false))
                    }
                    currentLine += delta.source.lines.size
                }
            }
        }
        
        // Add remaining unchanged lines
        while (currentLine < originalLines.size) {
            segments.add(DiffSegment(originalLines[currentLine], isUnchanged = true))
            currentLine++
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
            
            // Clean markdown (just in case AI uses it)
            processedContent = processedContent
                .replace("**", "")
                .replace("__", "")
                .replace("~~", "")
                .replace(Regex("^#+\\s", RegexOption.MULTILINE), "")
                .replace(Regex("^[*-]\\s", RegexOption.MULTILINE), "") // Remove bullet points
            
            // Update title if extracted
            if (extractedTitle != null && (currentTitle.isBlank() || currentTitle == "Untitled")) {
                onTitleUpdate(extractedTitle)
            }
            
            // Determine if we should replace or append
            val shouldReplace = shouldReplaceContent(lastQuery)
            
            if (currentContent.isBlank() || !shouldReplace) {
                // Simple mode: empty content or append mode - INSTANT UPDATE
                isTypingInEditor = true
                
                val newContent = if (currentContent.isBlank()) {
                    processedContent
                } else {
                    currentContent.trimEnd() + "\n\n" + processedContent
                }
                
                onContentUpdate(newContent)
                onScrollToBottom()
                isTypingInEditor = false
            } else {
                // Myers Diff mode: smart line-by-line editing - INSTANT UPDATE
                isTypingInEditor = true
                
                val typingSegments = getTypingSegments(currentContent, processedContent)
                
                // Track which lines are being changed
                val changedLineIndices = mutableListOf<Int>()
                
                // Build final content directly
                val finalLines = mutableListOf<String>()
                typingSegments.forEachIndexed { index, segment ->
                    finalLines.add(segment.text)
                    if (!segment.isUnchanged) {
                        changedLineIndices.add(index)
                    }
                }
                
                // Update content instantly
                onContentUpdate(finalLines.joinToString("\n"))
                
                // Highlight changed lines
                onHighlightLines(changedLineIndices)
                
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
                            // Clear previous highlights when new input is sent
                            onHighlightLines(emptyList())
                            
                            lastQuery = userInput // Store query for detection
                            // Send content directly to AI (no line numbers)
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
