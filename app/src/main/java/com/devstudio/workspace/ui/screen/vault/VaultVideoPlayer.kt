package com.devstudio.workspace.ui.screen.vault

import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.ui.viewmodel.VaultViewModel
import java.io.File
import androidx.compose.ui.text.font.FontWeight
import com.devstudio.workspace.ui.screen.vault.formatFileSize
import androidx.compose.ui.unit.dp

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Divider

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.devstudio.workspace.data.model.VaultItemType

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultVideoPlayer(
    viewModel: VaultViewModel,
    item: VaultItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var videoFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Player State
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var videoDuration by remember { mutableStateOf(0) }
    var showControls by remember { mutableStateOf(true) }
    
    // Sheet State
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    // VideoView Reference
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }



    // Internal Item State for Navigation
    var currentVaultItem by remember { mutableStateOf(item) }
    
    // Get all videos for navigation
    val allItems by viewModel.vaultItems.collectAsState()
    val videoItems = remember(allItems) { allItems.filter { it.itemType == VaultItemType.VIDEO } }
    
    // Decrypt file when item changes
    LaunchedEffect(currentVaultItem) {
        isLoading = true
        // 10GB Support
        val file = viewModel.getDecryptedFile(context, currentVaultItem)
        if (file != null) {
            videoFile = file
            isLoading = false
        } else {
            error = "Failed to load video"
            isLoading = false
        }
    }
    
    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            videoFile?.delete()
        }
    }
    
    // Progress Ticker
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                videoViewRef?.let { vv ->
                    if (vv.isPlaying) {
                        currentPosition = vv.currentPosition
                        videoDuration = vv.duration
                    }
                }
                kotlinx.coroutines.delay(50)
            }
        }
    }
    
    // Auto-hide controls
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            kotlinx.coroutines.delay(3000)
            showControls = false
        }
    }

    // Auto Pause/Play on Sheet Toggle
    LaunchedEffect(showSheet) {
        videoViewRef?.let { vv ->
            if (showSheet) {
                if (vv.isPlaying) {
                    vv.pause()
                    isPlaying = false
                }
            } else {
                if (!vv.isPlaying && !isLoading && error == null) {
                    vv.start()
                    isPlaying = true
                }
            }
        }
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // Theme Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Use Theme Background always. This makes "bars" match the app theme.
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls }
                    )
                }
                .pointerInput(currentVaultItem) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -20) { // Swipe Up
                             showSheet = true
                        }
                    }
                }
                .pointerInput(currentVaultItem) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -50) { // Swipe Left -> Next
                            val index = videoItems.indexOfFirst { it.id == currentVaultItem.id }
                            if (index != -1 && index < videoItems.size - 1) {
                                currentVaultItem = videoItems[index + 1]
                                isPlaying = false // Stop prev playback
                            }
                        } else if (dragAmount > 50) { // Swipe Right -> Prev
                            val index = videoItems.indexOfFirst { it.id == currentVaultItem.id }
                            if (index != -1 && index > 0) {
                                currentVaultItem = videoItems[index - 1]
                                isPlaying = false // Stop prev playback
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Decrypting Secure Video...",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (currentVaultItem.fileSize != null && currentVaultItem.fileSize!! > 100 * 1024 * 1024) {
                         Text(
                             "Large files may take a moment.", 
                             style = MaterialTheme.typography.bodySmall, 
                             color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                         )
                    }
                }
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (videoFile != null) {
                AndroidVideoView(
                    videoUri = Uri.fromFile(videoFile!!),
                    onVideoViewCreated = { vv -> videoViewRef = vv },
                    onPrepared = { duration -> 
                        videoDuration = duration 
                        isPlaying = true
                    },
                    onError = { msg -> error = msg }
                )
            }
            
            // Custom Controls Overlay
            if (!isLoading && error == null && showControls) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter)
                            .zIndex(1f), // Ensure clickability
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    }

                    // Center Play/Pause
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                    showControls = false // Auto hide on play
                                }
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.Center)
                            .zIndex(1f)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.8f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Bottom Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .zIndex(1f)
                    ) {
                        // REMOVED FILENAME TEXT AS REQUESTED
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                             Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.7f))
                             Text("Swipe up for details", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(formatDuration(currentPosition.toLong()), color = Color.White, style = MaterialTheme.typography.bodySmall)
                            
                            // Standard Slider
                            Slider(
                                 value = if (videoDuration > 0) currentPosition.toFloat() / videoDuration else 0f,
                                 onValueChange = { ratio ->
                                     val newPos = (ratio * videoDuration).toInt()
                                     currentPosition = newPos
                                     videoViewRef?.seekTo(newPos)
                                 },
                                 modifier = Modifier
                                     .weight(1f)
                                    .padding(horizontal = 16.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            
                            Text(formatDuration(videoDuration.toLong()), color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        
        // Detailed Bottom Sheet
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Video Details & Actions", 
                        style = MaterialTheme.typography.headlineSmall, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    
                    InfoRow(Icons.Default.VideoFile, "Name", currentVaultItem.originalFileName ?: "Unknown")
                    InfoRow(Icons.Default.SdStorage, "Format", currentVaultItem.originalFileName?.substringAfterLast('.', "mp4")?.uppercase() ?: "VIDEO")
                    InfoRow(Icons.Default.DataUsage, "Size", formatFileSize(currentVaultItem.fileSize ?: 0))
                    InfoRow(Icons.Default.Timer, "Duration", formatDuration(videoDuration.toLong()))
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { 
                                viewModel.unhideItem(context, currentVaultItem) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.LockOpen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Unhide Video")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                viewModel.deleteVaultItem(currentVaultItem)
                                onBack() // Go back after delete
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AndroidVideoView(
    videoUri: Uri,
    onVideoViewCreated: (VideoView) -> Unit,
    onPrepared: (Int) -> Unit,
    onError: (String) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setZOrderMediaOverlay(true)
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
                
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.start()
                    onPrepared(mp.duration)
                }
                
                setOnErrorListener { _, what, extra ->
                    onError("Playback Error: $what, $extra")
                    true
                }
                
                setVideoURI(videoUri)
                onVideoViewCreated(this)
            }
        },
        update = {},
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

