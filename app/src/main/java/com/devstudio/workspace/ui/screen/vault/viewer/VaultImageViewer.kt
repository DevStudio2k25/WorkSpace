package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import com.devstudio.workspace.ui.viewmodel.VaultViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VaultImageViewer(
    viewModel: VaultViewModel,
    initialItemId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.vaultItems.collectAsState()
    
    // Filter only images for the pager and ensure we have a valid list
    val imageItems = remember(allItems) {
        allItems.filter { it.itemType == VaultItemType.IMAGE }.sortedByDescending { it.createdAt }
    }
    
    val initialIndex = remember(imageItems) {
        val index = imageItems.indexOfFirst { it.id == initialItemId }
        if (index >= 0) index else 0
    }

    // Pager state
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { imageItems.size }
    )

    // Current item being viewed
    val currentItem = if (imageItems.isNotEmpty()) imageItems[pagerState.currentPage] else null

    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showUnhideConfirm by remember { mutableStateOf(false) }
    
    // UI Visibility (Tap to toggle)
    var showControls by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            if (showControls) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${imageItems.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Default.Info, "Info", tint = Color.White)
                        }
                        IconButton(onClick = { showUnhideConfirm = true }) {
                            Icon(Icons.Default.LockOpen, "Unhide", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        contentColor = Color.White,
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(if (showControls) padding else PaddingValues(0.dp)) // Animate padding if possible, or just snap
        ) {
            if (imageItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { index -> imageItems[index].id }
                ) { page ->
                    val item = imageItems[page]
                    // Thumbnail (always available immediately)
                    val thumbFile = item.thumbnailPath?.let { File(it) }
                    
                    // High-res file state
                    var highResFile by remember { mutableStateOf<File?>(null) }
                    
                    // Load high-res when page is active (or close to active)
                    LaunchedEffect(page) {
                        // Only load if this is the current page or adjacent?
                        // For simplicity, load if rendered. Pager disposes off-screen items eventually.
                        val file = viewModel.getDecryptedFile(context, item)
                        highResFile = file
                    }
                    
                    // Cleanup high-res when disposed
                    DisposableEffect(page) {
                        onDispose {
                            highResFile?.delete()
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showControls = !showControls },
                        contentAlignment = Alignment.Center
                    ) {
                        // Show High Res if available, else Thumbnail
                        val imageModel = highResFile ?: thumbFile
                        
                        if (imageModel != null && imageModel.exists()) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = item.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.BrokenImage, null, tint = Color.White, modifier = Modifier.size(64.dp))
                                Text("Image not found", color = Color.White)
                            }
                        }
                        
                        // Show loading only if we are on thumbnail and waiting for high res? 
                        // Or just let it pop in. 
                    }
                }
            }
        }
    }

    // Dialogs using currentItem
    if (currentItem != null) {
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                icon = { Icon(Icons.Default.Info, null) },
                title = { Text("Details") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Name", currentItem.originalFileName ?: currentItem.title)
                        DetailRow("Size", formatFileSize(currentItem.fileSize ?: 0))
                        DetailRow("Date", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(currentItem.createdAt)))
                        DetailRow("Path", currentItem.encryptedFilePath ?: "Unknown")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showUnhideConfirm) {
            AlertDialog(
                onDismissRequest = { showUnhideConfirm = false },
                title = { Text("Unhide Image?") },
                text = { Text("Decrypt and restore to gallery?") },
                confirmButton = {
                    TextButton(onClick = {
                        showUnhideConfirm = false
                        viewModel.unhideItem(context, currentItem) { success, _ ->
                            if (success && imageItems.size == 1) {
                                onBack() // Go back if last item
                            }
                        }
                    }) {
                        Text("Unhide")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnhideConfirm = false }) { Text("Cancel") }
                }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Permanently?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            viewModel.deleteVaultItem(currentItem)
                            if (imageItems.size == 1) {
                                onBack() // Go back if last item
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}
