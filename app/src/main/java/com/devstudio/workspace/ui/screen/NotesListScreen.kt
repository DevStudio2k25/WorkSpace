package com.devstudio.workspace.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.Note
import com.devstudio.workspace.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: com.devstudio.workspace.ui.viewmodel.NoteViewModel,
    onNoteClick: (Note) -> Unit = {},
    onCreateNote: () -> Unit = {},
    onSettings: () -> Unit = {},
    onVaultAccess: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val securePrefs = remember { com.devstudio.workspace.util.SecurePreferences(context) }

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var vaultKeyword by remember { mutableStateOf("OPEN VAULT") }

    // Selection mode
    var selectionMode by remember { mutableStateOf(false) }
    var selectedNotes by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // Get notes from ViewModel
    val notes by viewModel.notes.collectAsState()

    // Load vault keyword
    LaunchedEffect(Unit) {
        securePrefs.vaultKeyword.collect { keyword ->
            vaultKeyword = keyword
        }
    }

    // Check for vault keyword in search
    LaunchedEffect(searchQuery) {
        if (searchQuery.equals(vaultKeyword, ignoreCase = true)) {
            searchQuery = ""
            showSearch = false
            onVaultAccess()
        }
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                // Selection Mode TopAppBar
                TopAppBar(
                    title = {
                        Text(
                            "${selectedNotes.size} selected",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedNotes = emptySet()
                        }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                // Delete selected notes
                                selectedNotes.forEach { noteId ->
                                    notes.find { it.id == noteId }?.let { note ->
                                        viewModel.deleteNote(note)
                                    }
                                }
                                selectionMode = false
                                selectedNotes = emptySet()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else if (showSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        showSearch = false
                        searchQuery = ""
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "Workspace",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                Icons.Default.Search,
                                "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onSettings) {
                            Icon(
                                Icons.Default.Settings,
                                "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    "Create Note",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            NotesGrid(
                notes = notes.filter {
                    searchQuery.isEmpty() ||
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.content.contains(searchQuery, ignoreCase = true)
                },
                onNoteClick = { note ->
                    if (selectionMode) {
                        // Toggle selection
                        selectedNotes = if (selectedNotes.contains(note.id)) {
                            selectedNotes - note.id
                        } else {
                            selectedNotes + note.id
                        }
                    } else {
                        onNoteClick(note)
                    }
                },
                onNoteLongClick = { note ->
                    // Enter selection mode
                    selectionMode = true
                    selectedNotes = setOf(note.id)
                },
                selectionMode = selectionMode,
                selectedNotes = selectedNotes,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesGrid(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit = {},
    selectionMode: Boolean = false,
    selectedNotes: Set<Long> = emptySet(),
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
        modifier = modifier
    ) {
        items(notes.size) { index ->
            NoteCard(
                note = notes[index],
                onClick = { onNoteClick(notes[index]) },
                onLongClick = { onNoteLongClick(notes[index]) },
                isSelected = selectedNotes.contains(notes[index].id),
                selectionMode = selectionMode
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val noteColor = if (note.color != 0) Color(note.color) else Color.Unspecified
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (noteColor != Color.Unspecified) {
                noteColor
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
            pressedElevation = 3.dp
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Selection checkbox (only in selection mode)
                if (selectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Title
                if (note.title.isNotEmpty()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Content preview
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Empty note indicator
                if (note.title.isEmpty() && note.content.isEmpty()) {
                    Text(
                        text = "Empty note",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Footer - Category and Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category
                    if (note.category.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = note.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Date
                    Text(
                        text = formatDate(note.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Pin indicator - Overlay in top-right corner (Box scope)
            if (note.isPinned && !selectionMode) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.padding(6.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search notes...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.ArrowBack,
                    "Close search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        "Clear",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Note,
                contentDescription = null,
                modifier = Modifier.padding(30.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "No notes yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first note",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun getNoteColor(colorInt: Int, isDark: Boolean): Color {
    val lightColor = Color(colorInt)
    
    // If it's default color or transparent, return surfaceVariant
    if (lightColor == Color.Transparent || colorInt == NoteColorDefault.hashCode()) {
        return Color.Unspecified
    }
    
    // Map light colors to dark colors
    return if (isDark) {
        when (colorInt) {
            NoteColorYellow.hashCode() -> NoteColorYellowDark
            NoteColorOrange.hashCode() -> NoteColorOrangeDark
            NoteColorRed.hashCode() -> NoteColorRedDark
            NoteColorPurple.hashCode() -> NoteColorPurpleDark
            NoteColorBlue.hashCode() -> NoteColorBlueDark
            NoteColorGreen.hashCode() -> NoteColorGreenDark
            NoteColorGray.hashCode() -> NoteColorGrayDark
            else -> NoteColorDefaultDark
        }
    } else {
        lightColor
    }
}
