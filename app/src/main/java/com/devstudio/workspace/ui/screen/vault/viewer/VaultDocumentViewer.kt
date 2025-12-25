package com.devstudio.workspace.ui.screen.vault

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import com.devstudio.workspace.ui.viewmodel.VaultViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDocumentViewer(
    viewModel: VaultViewModel,
    item: VaultItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var documentFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnhideDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    // Internal Item State for Navigation
    var currentVaultItem by remember { mutableStateOf(item) }
    
    // Get all documents for navigation
    val allItems by viewModel.vaultItems.collectAsState()
    val documentItems = remember(allItems) { 
        allItems.filter { it.itemType == VaultItemType.DOCUMENT }
            .sortedByDescending { it.createdAt }
    }
    
    // Get file extension
    val extension = remember(currentVaultItem.originalFileName) {
        currentVaultItem.originalFileName?.substringAfterLast(".", "")?.uppercase() ?: "FILE"
    }
    
    // Decrypt file when item changes
    LaunchedEffect(currentVaultItem) {
        isLoading = true
        
        val file = viewModel.getDecryptedFile(context, currentVaultItem)
        if (file != null) {
            documentFile = file
            isLoading = false
        } else {
            error = "Failed to decrypt document"
            isLoading = false
        }
    }
    
    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            documentFile?.delete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Document Viewer",
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
                        "Decrypting Document...",
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
                // Document Preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Document Icon
                    val (icon, iconColor) = remember(extension) {
                        when (extension) {
                            "PDF" -> Icons.Default.PictureAsPdf to androidx.compose.ui.graphics.Color(0xFFE53935)
                            "DOC", "DOCX" -> Icons.Default.Description to androidx.compose.ui.graphics.Color(0xFF1976D2)
                            "TXT" -> Icons.Default.TextSnippet to androidx.compose.ui.graphics.Color(0xFF757575)
                            "XLS", "XLSX" -> Icons.Default.TableChart to androidx.compose.ui.graphics.Color(0xFF388E3C)
                            "PPT", "PPTX" -> Icons.Default.Slideshow to androidx.compose.ui.graphics.Color(0xFFD84315)
                            "ZIP", "RAR" -> Icons.Default.FolderZip to androidx.compose.ui.graphics.Color(0xFFF57C00)
                            else -> Icons.Default.InsertDriveFile to androidx.compose.ui.graphics.Color(0xFF616161)
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Surface(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = iconColor.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = iconColor
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Document Info
                    Text(
                        text = currentVaultItem.originalFileName ?: currentVaultItem.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "$extension Document",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // File Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("File Name", currentVaultItem.originalFileName ?: "Unknown")
                            Spacer(Modifier.height(8.dp))
                            DetailRow("File Type", extension)
                            Spacer(Modifier.height(8.dp))
                            DetailRow("File Size", formatFileSize(currentVaultItem.fileSize ?: 0))
                            Spacer(Modifier.height(8.dp))
                            DetailRow("Encrypted", java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(currentVaultItem.createdAt)))
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Previous
                        OutlinedButton(
                            onClick = {
                                val index = documentItems.indexOfFirst { it.id == currentVaultItem.id }
                                if (index > 0) {
                                    currentVaultItem = documentItems[index - 1]
                                }
                            },
                            enabled = documentItems.indexOfFirst { it.id == currentVaultItem.id } > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, "Previous")
                            Spacer(Modifier.width(4.dp))
                            Text("Previous")
                        }
                        
                        // Next
                        OutlinedButton(
                            onClick = {
                                val index = documentItems.indexOfFirst { it.id == currentVaultItem.id }
                                if (index < documentItems.size - 1) {
                                    currentVaultItem = documentItems[index + 1]
                                }
                            },
                            enabled = documentItems.indexOfFirst { it.id == currentVaultItem.id } < documentItems.size - 1
                        ) {
                            Text("Next")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, "Next")
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Info Text
                    Text(
                        text = "Document is encrypted and secure.\nUse 'Unhide' to restore to Downloads folder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = { Icon(Icons.Default.Info, null) },
            title = { Text("Document Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Name", currentVaultItem.originalFileName ?: currentVaultItem.title)
                    DetailRow("Type", extension)
                    DetailRow("Size", formatFileSize(currentVaultItem.fileSize ?: 0))
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
            title = { Text("Unhide Document?") },
            text = { Text("Decrypt and restore to Downloads folder?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnhideDialog = false
                    viewModel.unhideItem(context, currentVaultItem) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success && documentItems.size == 1) {
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
                        if (documentItems.size == 1) {
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
