package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.layout.*
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

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val chunkedFolders = folders.chunked(2)
        chunkedFolders.forEach { rowFolders ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowFolders.forEach { (type, icon, label) ->
                    Box(modifier = Modifier.weight(1f)) {
                        FolderCard(type, icon, label, onClick = { onFolderClick(type) })
                    }
                }
                // If row has only 1 item, add spacer to keep alignment
                if (rowFolders.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
