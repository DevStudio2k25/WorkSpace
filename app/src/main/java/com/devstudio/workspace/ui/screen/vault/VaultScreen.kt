package com.devstudio.workspace.ui.screen.vault

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: com.devstudio.workspace.ui.viewmodel.VaultViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onItemClick: (VaultItem) -> Unit = {}, // Deprecated in new flow, handled internally
    onLockVault: () -> Unit = {},
    onVaultSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var showMessage by remember { mutableStateOf<String?>(null) }
    var currentFolder by rememberSaveable { mutableStateOf<VaultItemType?>(null) }
    var selectedItems by remember { mutableStateOf<Set<VaultItem>>(emptySet()) }
    val isSelectionMode = remember(selectedItems) { selectedItems.isNotEmpty() }
    var isMasonryMode by remember { mutableStateOf(false) }

    // Collect vault items from ViewModel
    val allVaultItems by viewModel.vaultItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Filter items based on current folder
    val currentItems = remember(allVaultItems, currentFolder) {
        if (currentFolder == null) emptyList()
        else allVaultItems.filter { it.itemType == currentFolder }
    }

    // Image picker launcher
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<android.net.Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.hideImages(context, uris) { count ->
                showMessage = "Hidden $count images successfully"
            }
        }
    }
    
    // Video picker launcher
    val videoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<android.net.Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.hideVideos(context, uris) { count ->
                showMessage = "Hidden $count videos successfully"
            }
        }
    }
    
    // Audio picker launcher
    val audioPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<android.net.Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.hideAudios(context, uris) { count ->
                showMessage = "Hidden $count audio files successfully"
            }
        }
    }
    
    // Document picker launcher
    val documentPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<android.net.Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.hideDocuments(context, uris) { count ->
                showMessage = "Hidden $count documents successfully"
            }
        }
    }
    
    // Handle Back Press
    BackHandler(enabled = currentFolder != null || isSelectionMode) {
        if (isSelectionMode) {
            selectedItems = emptySet()
        } else if (currentFolder != null) {
            currentFolder = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                       Text(
                           "${selectedItems.size} Selected",
                           style = MaterialTheme.typography.titleLarge,
                           fontWeight = FontWeight.Bold
                       )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentFolder != null) {
                                IconButton(onClick = { currentFolder = null }) {
                                    Icon(Icons.Default.ArrowBack, "Back")
                                }
                            } else {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Text(
                                text = when (currentFolder) {
                                    VaultItemType.IMAGE -> "Images"
                                    VaultItemType.VIDEO -> "Videos"
                                    VaultItemType.DOCUMENT -> "Documents"
                                    VaultItemType.AUDIO -> "Audio"
                                    VaultItemType.NOTE -> "Secure Notes"
                                    else -> "Secure Vault"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        // Select All button
                        IconButton(onClick = {
                            selectedItems = currentItems.toSet()
                        }) {
                            Icon(Icons.Default.SelectAll, "Select All", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            // Bulk Unhide
                            var successCount = 0
                            selectedItems.forEach { item ->
                                viewModel.unhideItem(context, item) { success, _ -> 
                                    if (success) successCount++
                                }
                            }
                            showMessage = "Unhiding selected items..." // Simpler feedback for now due to async nature
                            selectedItems = emptySet()
                        }) {
                            Icon(Icons.Default.LockOpen, "Unhide Selected", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                             // Bulk Delete
                            selectedItems.forEach { viewModel.deleteVaultItem(it) }
                            showMessage = "Deleted ${selectedItems.size} items"
                            selectedItems = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { selectedItems = emptySet() }) {
                            Icon(Icons.Default.Close, "Close Selection")
                        }
                    } else if (currentFolder == VaultItemType.IMAGE) {
                        // Image Gallery Actions
                        IconButton(onClick = { isMasonryMode = !isMasonryMode }) {
                            Icon(
                                if (isMasonryMode) Icons.Default.ViewModule else Icons.Default.Dashboard,
                                "Toggle Layout"
                            )
                        }
                        IconButton(onClick = onVaultSettings) {
                            Icon(Icons.Default.Settings, "Vault Settings")
                        }
                        IconButton(onClick = onLockVault) {
                            Icon(Icons.Default.LockOpen, "Lock Vault", tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        IconButton(onClick = onVaultSettings) {
                            Icon(Icons.Default.Settings, "Vault Settings")
                        }
                        IconButton(onClick = onLockVault) {
                            Icon(Icons.Default.LockOpen, "Lock Vault", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isSelectionMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (currentFolder == VaultItemType.IMAGE) {
                FloatingActionButton(
                    onClick = {
                        imagePickerLauncher.launch(arrayOf("image/*"))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "add", tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else if (currentFolder == VaultItemType.VIDEO) {
                 FloatingActionButton(
                    onClick = {
                        videoPickerLauncher.launch(arrayOf("video/*"))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "add", tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else if (currentFolder == VaultItemType.AUDIO) {
                 FloatingActionButton(
                    onClick = {
                        audioPickerLauncher.launch(arrayOf("audio/*"))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "add", tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else if (currentFolder == VaultItemType.DOCUMENT) {
                 FloatingActionButton(
                    onClick = {
                        documentPickerLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip", "*/*"))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "add", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        snackbarHost = {
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(showMessage) {
                showMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    showMessage = null
                }
            }
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (currentFolder == null) {
                // Folder Grid View with Info Section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    VaultFolderGrid(
                        modifier = Modifier.fillMaxWidth(),
                        onFolderClick = { type ->
                            currentFolder = type
                        }
                    )
                    
                    // Information Section
                    VaultInfoSection()
                }
            } else {
                // Items Gallery View (Currently only optimized for Images)
                if (currentItems.isEmpty()) {
                    EmptyVaultState(
                        modifier = Modifier.fillMaxSize(),
                        message = "No encrypted ${currentFolder?.name?.lowercase()}s found"
                    )
                } else if (currentFolder == VaultItemType.IMAGE) {
                    VaultImageGallery(
                        items = currentItems,
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                selectedItems = if (selectedItems.contains(item)) selectedItems - item else selectedItems + item
                            } else {
                                onItemClick(item) 
                            }
                        },
                        onItemLongClick = { item ->
                             if (!isSelectionMode) {
                                 selectedItems = selectedItems + item
                             }
                        },
                        selectedItems = selectedItems,
                        isMasonryMode = isMasonryMode
                    )
                } else if (currentFolder == VaultItemType.VIDEO) {
                    VaultVideoGallery(
                        items = currentItems,
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                selectedItems = if (selectedItems.contains(item)) selectedItems - item else selectedItems + item
                            } else {
                                onItemClick(item)
                            }
                        },
                        onItemLongClick = { item ->
                             if (!isSelectionMode) {
                                 selectedItems = selectedItems + item
                             }
                        },
                        selectedItems = selectedItems
                    )
                } else if (currentFolder == VaultItemType.AUDIO) {
                    VaultAudioGallery(
                        items = currentItems,
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                selectedItems = if (selectedItems.contains(item)) selectedItems - item else selectedItems + item
                            } else {
                                onItemClick(item)
                            }
                        },
                        onItemLongClick = { item ->
                             if (!isSelectionMode) {
                                 selectedItems = selectedItems + item
                             }
                        },
                        selectedItems = selectedItems
                    )
                } else if (currentFolder == VaultItemType.DOCUMENT) {
                    VaultDocumentGallery(
                        items = currentItems,
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                selectedItems = if (selectedItems.contains(item)) selectedItems - item else selectedItems + item
                            } else {
                                onItemClick(item)
                            }
                        },
                        onItemLongClick = { item ->
                             if (!isSelectionMode) {
                                 selectedItems = selectedItems + item
                             }
                        },
                        selectedItems = selectedItems
                    )
                } else {
                    // Fallback list for other types (technically reachable only if we allow clicking other folders)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Coming Soon")
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {}, // Block touches
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
