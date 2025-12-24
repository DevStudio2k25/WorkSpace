package com.devstudio.workspace.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.Note
import com.devstudio.workspace.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long? = null,
    viewModel: com.devstudio.workspace.ui.viewmodel.NoteViewModel,
    onBack: () -> Unit = {}
) {
    // Load note if editing
    var currentNote by remember { mutableStateOf<Note?>(null) }
    val notes by viewModel.notes.collectAsState()
    
    // Load note data
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            currentNote = notes.find { it.id == noteId }
        }
    }
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(NoteColorDefault.hashCode()) }
    var selectedCategory by remember { mutableStateOf("General") }
    var isPinned by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    
    // Update state when note loads
    LaunchedEffect(currentNote) {
        currentNote?.let { note ->
            title = note.title
            content = note.content
            selectedColor = note.color
            selectedCategory = note.category
            isPinned = note.isPinned
        }
    }
    
    val categories = listOf("General", "Personal", "Work", "Ideas", "Shopping", "Study", "Health", "Other")
    
    // Note colors
    val noteColors = listOf<Pair<Color, String>>(
        Pair(NoteColorDefault, "Default"),
        Pair(NoteColorYellow, "Yellow"),
        Pair(NoteColorOrange, "Orange"),
        Pair(NoteColorRed, "Red"),
        Pair(NoteColorPurple, "Purple"),
        Pair(NoteColorBlue, "Blue"),
        Pair(NoteColorGreen, "Green"),
        Pair(NoteColorGray, "Gray")
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            if (currentNote == null) "New Note" else "Edit Note",
                            fontWeight = FontWeight.Bold
                        )
                        if (currentNote != null) {
                            Text(
                                "Last edited: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(currentNote!!.updatedAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Pin button
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // More options
                    IconButton(onClick = { showMoreOptions = true }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                    
                    // Save button
                    IconButton(
                        onClick = {
                            if (title.isNotEmpty() || content.isNotEmpty()) {
                                val savedNote = if (currentNote != null) {
                                    currentNote!!.copy(
                                        title = title,
                                        content = content,
                                        color = selectedColor,
                                        category = selectedCategory,
                                        isPinned = isPinned,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                } else {
                                    Note(
                                        title = title,
                                        content = content,
                                        color = selectedColor,
                                        category = selectedCategory,
                                        isPinned = isPinned
                                    )
                                }
                                viewModel.insertNote(savedNote)
                            }
                            onBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.Check, 
                            "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(selectedColor).copy(alpha = 0.3f).takeIf { selectedColor != NoteColorDefault.hashCode() }
                        ?: MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Bottom toolbar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color picker button
                    IconButton(onClick = { showColorPicker = true }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColor))
                        )
                    }
                    
                    // Category button
                    AssistChip(
                        onClick = { showCategoryPicker = true },
                        label = { Text(selectedCategory) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Label,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    
                    // Word count
                    Text(
                        "${content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size} words",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Character count
                    Text(
                        "${content.length} chars",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color(selectedColor).copy(alpha = 0.1f).takeIf { selectedColor != NoteColorDefault.hashCode() }
                        ?: MaterialTheme.colorScheme.background
                )
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { 
                    Text(
                        "Title",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text(
                        "Start typing your note...",
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp)
            )
        }
        
        // Color Picker Dialog
        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                icon = {
                    Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                },
                title = { 
                    Text(
                        "Choose Color",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        noteColors.chunked(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { (color, name) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                selectedColor = color.hashCode()
                                                showColorPicker = false
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(color),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (selectedColor == color.hashCode()) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showColorPicker = false }) {
                        Text("Done")
                    }
                }
            )
        }
        
        // Category Picker Dialog
        if (showCategoryPicker) {
            AlertDialog(
                onDismissRequest = { showCategoryPicker = false },
                icon = {
                    Icon(Icons.Default.Label, null, tint = MaterialTheme.colorScheme.primary)
                },
                title = { 
                    Text(
                        "Choose Category",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategory = category
                                        showCategoryPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryPicker = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    category,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCategoryPicker = false }) {
                        Text("Done")
                    }
                }
            )
        }
        
        // More Options Menu
        if (showMoreOptions) {
            AlertDialog(
                onDismissRequest = { showMoreOptions = false },
                icon = {
                    Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                },
                title = { 
                    Text(
                        "Note Options",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Delete option (only for existing notes)
                        if (currentNote != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.deleteNote(currentNote!!)
                                        showMoreOptions = false
                                        onBack()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Delete Note",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        // Share option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // TODO: Share functionality
                                    showMoreOptions = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Share Note",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMoreOptions = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
