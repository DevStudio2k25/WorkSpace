package com.devstudio.workspace.ui.screen.vault

import android.media.MediaPlayer
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import com.devstudio.workspace.ui.viewmodel.VaultViewModel
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultAudioPlayer(
    viewModel: VaultViewModel,
    item: VaultItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var audioFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Player State
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var audioDuration by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnhideDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    // Internal Item State for Navigation
    var currentVaultItem by remember { mutableStateOf(item) }
    
    // Get all audios for navigation
    val allItems by viewModel.vaultItems.collectAsState()
    val audioItems = remember(allItems) { 
        allItems.filter { it.itemType == VaultItemType.AUDIO }
            .sortedByDescending { it.createdAt }
    }
    
    // Decrypt file when item changes
    LaunchedEffect(currentVaultItem) {
        isLoading = true
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        
        val file = viewModel.getDecryptedFile(context, currentVaultItem)
        if (file != null) {
            audioFile = file
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    setOnCompletionListener {
                        isPlaying = false
                        currentPosition = 0
                    }
                }
                mediaPlayer = mp
                audioDuration = mp.duration
                isLoading = false
            } catch (e: Exception) {
                error = "Failed to load audio: ${e.message}"
                isLoading = false
            }
        } else {
            error = "Failed to decrypt audio"
            isLoading = false
        }
    }
    
    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            audioFile?.delete()
        }
    }
    
    // Progress Ticker
    LaunchedEffect(isPlaying) {
        while (isActive && isPlaying) {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    currentPosition = mp.currentPosition
                }
            }
            delay(100)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Audio Player",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, "Info")
                    }
                    IconButton(onClick = { showUnhideDialog = true }) {
                        Icon(Icons.Default.LockOpen, "Unhide")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Decrypting Audio...",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Player UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(Modifier.height(32.dp))
                    
                    // Album Art Placeholder
                    Surface(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(48.dp))
                    
                    // Track Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentVaultItem.originalFileName ?: currentVaultItem.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Encrypted Audio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = if (audioDuration > 0) currentPosition.toFloat() / audioDuration else 0f,
                            onValueChange = { ratio ->
                                val newPos = (ratio * audioDuration).toInt()
                                currentPosition = newPos
                                mediaPlayer?.seekTo(newPos)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                formatDuration(currentPosition.toLong()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                formatDuration(audioDuration.toLong()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Player Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous
                        IconButton(
                            onClick = {
                                val index = audioItems.indexOfFirst { it.id == currentVaultItem.id }
                                if (index > 0) {
                                    currentVaultItem = audioItems[index - 1]
                                }
                            },
                            enabled = audioItems.indexOfFirst { it.id == currentVaultItem.id } > 0
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(40.dp),
                                tint = if (audioItems.indexOfFirst { it.id == currentVaultItem.id } > 0)
                                    MaterialTheme.colorScheme.onBackground
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                        
                        // Rewind
                        IconButton(
                            onClick = {
                                val newPos = (currentPosition - 10000).coerceAtLeast(0)
                                mediaPlayer?.seekTo(newPos)
                                currentPosition = newPos
                            }
                        ) {
                            Icon(
                                Icons.Default.Replay10,
                                contentDescription = "Rewind",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        // Play/Pause
                        FloatingActionButton(
                            onClick = {
                                mediaPlayer?.let { mp ->
                                    if (mp.isPlaying) {
                                        mp.pause()
                                        isPlaying = false
                                    } else {
                                        mp.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        // Forward
                        IconButton(
                            onClick = {
                                val newPos = (currentPosition + 10000).coerceAtMost(audioDuration)
                                mediaPlayer?.seekTo(newPos)
                                currentPosition = newPos
                            }
                        ) {
                            Icon(
                                Icons.Default.Forward10,
                                contentDescription = "Forward",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        // Next
                        IconButton(
                            onClick = {
                                val index = audioItems.indexOfFirst { it.id == currentVaultItem.id }
                                if (index < audioItems.size - 1) {
                                    currentVaultItem = audioItems[index + 1]
                                }
                            },
                            enabled = audioItems.indexOfFirst { it.id == currentVaultItem.id } < audioItems.size - 1
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(40.dp),
                                tint = if (audioItems.indexOfFirst { it.id == currentVaultItem.id } < audioItems.size - 1)
                                    MaterialTheme.colorScheme.onBackground
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = { Icon(Icons.Default.Info, null) },
            title = { Text("Audio Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Name", currentVaultItem.originalFileName ?: currentVaultItem.title)
                    DetailRow("Size", formatFileSize(currentVaultItem.fileSize ?: 0))
                    DetailRow("Duration", formatDuration(audioDuration.toLong()))
                    DetailRow("Date", java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(currentVaultItem.createdAt)))
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Unhide Dialog
    if (showUnhideDialog) {
        AlertDialog(
            onDismissRequest = { showUnhideDialog = false },
            title = { Text("Unhide Audio?") },
            text = { Text("Decrypt and restore to Music folder?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnhideDialog = false
                    viewModel.unhideItem(context, currentVaultItem) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success && audioItems.size == 1) {
                            onBack()
                        }
                    }
                }) {
                    Text("Unhide")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnhideDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Permanently?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteVaultItem(currentVaultItem)
                        if (audioItems.size == 1) {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
