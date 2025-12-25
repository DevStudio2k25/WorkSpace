package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.VaultItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@Composable
fun VaultDocumentGallery(
    items: List<VaultItem>,
    onItemClick: (VaultItem) -> Unit,
    onItemLongClick: (VaultItem) -> Unit,
    selectedItems: Set<VaultItem>
) {
    // Group and sort items by date
    val groupedItems = remember(items) {
        items.sortedByDescending { it.createdAt }
            .groupBy { getGalleryHeader(it.createdAt) }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp, start = 8.dp, end = 8.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        groupedItems.forEach { (header, documentItems) ->
            // Date Header
            item(key = "header_$header") {
                GalleryHeader(header)
            }

            // Document Items
            items(
                items = documentItems,
                key = { it.id }
            ) { item ->
                val isSelected = selectedItems.contains(item)
                VaultDocumentItem(
                    item = item,
                    isSelected = isSelected,
                    selectionMode = selectedItems.isNotEmpty(),
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultDocumentItem(
    item: VaultItem,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Get file extension
    val extension = remember(item.originalFileName) {
        item.originalFileName?.substringAfterLast(".", "")?.uppercase() ?: "FILE"
    }
    
    // Get icon and color based on file type
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Icon / Selection Indicator
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            iconColor.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Document Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.originalFileName ?: item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$extension Document",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatFileSize(item.fileSize ?: 0),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = formatDate(item.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // View/Selection Icon
            if (selectionMode) {
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Unselected",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "View",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)
    
    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}
