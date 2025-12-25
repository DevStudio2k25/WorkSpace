package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.data.model.VaultItemType

@Composable
fun VaultFolderGrid(
    modifier: Modifier = Modifier,
    onFolderClick: (VaultItemType) -> Unit
) {
    val folders = listOf(
        Triple(VaultItemType.IMAGE, Icons.Default.Image, "Images"),
        Triple(VaultItemType.VIDEO, Icons.Default.VideoLibrary, "Videos"),
        Triple(VaultItemType.AUDIO, Icons.Default.AudioFile, "Audio"),
        Triple(VaultItemType.DOCUMENT, Icons.Default.Description, "Documents")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(folders) { (type, icon, label) ->
            FolderCard(type, icon, label, onClick = { onFolderClick(type) })
        }
    }
}
