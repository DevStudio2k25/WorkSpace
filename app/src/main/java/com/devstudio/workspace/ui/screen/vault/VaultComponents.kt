package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import coil.compose.AsyncImage
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import android.content.Context
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderCard(
    type: VaultItemType,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    // Determine colors based on type
    val (containerColor, iconColor) = when (type) {
        VaultItemType.IMAGE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        VaultItemType.VIDEO -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        VaultItemType.AUDIO -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        VaultItemType.DOCUMENT -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        VaultItemType.NOTE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f), // Slightly taller
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(24.dp), // More rounded
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Icon Decoration
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp),
                tint = iconColor.copy(alpha = 0.15f)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Top Icon
                Surface(
                    color = iconColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = iconColor
                        )
                    }
                }
                
                // Label
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultImageItem(
    item: VaultItem,
    isSelected: Boolean,
    selectionMode: Boolean,
    isMasonry: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    // State for High-Res (Decoded) Bitmap
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Load high quality image for grid
    LaunchedEffect(item) {
        if (item.itemType == VaultItemType.IMAGE) {
            // Decode to ~300-400px width which is enough for grid but better than low-res thumb
             val bitmap = decodeEncryptedBitmap(context, item, targetWidth = 400)
             if (bitmap != null) {
                 imageBitmap = bitmap.asImageBitmap()
             }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .let {
                if (!isMasonry) it.aspectRatio(1f) else it
            }
    ) {
        // Show Decoded Bitmap if available, else Thumbnail
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = item.title,
                contentScale = if (isMasonry) ContentScale.FillWidth else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .let { 
                        if (isMasonry) it.wrapContentHeight() else it.fillMaxSize()
                    }
                    .let {
                        if (isSelected) it.padding(8.dp).clip(RoundedCornerShape(8.dp)) else it
                    }
            )
        } else if (item.thumbnailPath != null && File(item.thumbnailPath).exists()) {
            AsyncImage(
                model = File(item.thumbnailPath),
                contentDescription = item.title,
                contentScale = if (isMasonry) ContentScale.FillWidth else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .let { 
                        if (isMasonry) it.wrapContentHeight() else it.fillMaxSize()
                    }
                    .let {
                        if (isSelected) it.padding(8.dp).clip(RoundedCornerShape(8.dp)) else it
                    }
            )
        } else {
            // Fallback: square aspect ratio if no image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        
        // Selection Checkmark Overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize() // Cover the whole item
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp).size(24.dp)
                )
            }
        } else if (selectionMode) {
             Icon(
                Icons.Default.RadioButtonUnchecked,
                contentDescription = "Unselected",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EmptyVaultState(modifier: Modifier = Modifier, message: String) {
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
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.padding(30.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.2f KB", kb)
        else -> "$size B"
    }
}


fun getGalleryHeader(timestamp: Long): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return when {
        now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR) -> "Today"
        
        now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - 1 == time.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        
        now.get(Calendar.YEAR) == time.get(Calendar.YEAR) -> 
            SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
            
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

suspend fun decodeEncryptedBitmap(context: Context, item: VaultItem, targetWidth: Int = 300): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val encryptedPath = item.encryptedFilePath ?: return@withContext null
            val file = File(encryptedPath)
            if (!file.exists()) return@withContext null

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            // 1. Decode bounds
            var options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
             encryptedFile.openFileInput().use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            
            // 2. Calculate sample size
            var sampleSize = 1
            if (options.outWidth > targetWidth) {
                sampleSize = options.outWidth / targetWidth
                if (sampleSize < 1) sampleSize = 1
            }
            
            // 3. Decode actual bitmap
            options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            encryptedFile.openFileInput().use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
             null
        }
    }
}
