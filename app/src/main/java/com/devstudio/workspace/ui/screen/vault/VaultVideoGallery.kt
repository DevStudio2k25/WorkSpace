package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.VaultItem

@Composable
fun VaultVideoGallery(
    items: List<VaultItem>,
    onItemClick: (VaultItem) -> Unit,
    onItemLongClick: (VaultItem) -> Unit,
    selectedItems: Set<VaultItem>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            val isSelected = selectedItems.contains(item)
            Box(contentAlignment = Alignment.Center) {
                VaultImageItem(
                    item = item,
                    isSelected = isSelected,
                    selectionMode = selectedItems.isNotEmpty(),
                    isMasonry = false, // Force square grid for consistency
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) }
                )
                // Play Icon Overlay
                if (!isSelected && selectedItems.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = "Play",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
