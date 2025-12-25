package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.VaultItem

@Composable
fun VaultImageGallery(
    items: List<VaultItem>,
    onItemClick: (VaultItem) -> Unit,
    onItemLongClick: (VaultItem) -> Unit,
    selectedItems: Set<VaultItem>,
    isMasonryMode: Boolean
) {
    // Group and sort items by date
    val groupedItems = remember(items) {
        items.sortedByDescending { it.createdAt }
             .groupBy { getGalleryHeader(it.createdAt) }
    }

    if (isMasonryMode) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 100.dp, start = 4.dp, end = 4.dp, top = 4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groupedItems.forEach { (header, galleryItems) ->
                // Date Header
                item(
                    span = StaggeredGridItemSpan.FullLine,
                    key = "header_$header"
                ) {
                    GalleryHeader(header)
                }

                // Image Items
                items(
                    items = galleryItems,
                    key = { it.id }
                ) { item ->
                    val isSelected = selectedItems.contains(item)
                    VaultImageItem(
                        item = item,
                        isSelected = isSelected,
                        selectionMode = selectedItems.isNotEmpty(),
                        isMasonry = true,
                        onClick = { onItemClick(item) },
                        onLongClick = { onItemLongClick(item) }
                    )
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groupedItems.forEach { (header, galleryItems) ->
                // Date Header
                item(
                    span = { GridItemSpan(3) },
                    key = "header_$header"
                ) {
                    GalleryHeader(header)
                }

                // Image Items
                items(
                    items = galleryItems,
                    key = { it.id }
                ) { item ->
                    val isSelected = selectedItems.contains(item)
                    VaultImageItem(
                        item = item,
                        isSelected = isSelected,
                        selectionMode = selectedItems.isNotEmpty(),
                        isMasonry = false,
                        onClick = { onItemClick(item) },
                        onLongClick = { onItemLongClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryHeader(header: String) {
    Text(
        text = header,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 24.dp, bottom = 12.dp)
    )
}
