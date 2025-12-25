package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VaultInfoSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Data Loss Warning (Most Important - RED)
        InfoCard(
            icon = Icons.Default.Warning,
            title = "Crucial Warning: Data Loss Risk",
            content = "If you uninstall or delete this app, all hidden files will be permanently lost. There is NO cloud backup. We cannot recover your data if the app is removed. Please restore your files before uninstalling.",
            iconTint = MaterialTheme.colorScheme.error,
            backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            textColor = MaterialTheme.colorScheme.error
        )

        // Section 2: Folder Access (Important)
        InfoCard(
            icon = Icons.Default.Lightbulb,
            title = "Can't Hide Some Files?",
            content = "Android restricts access to certain system folders (like Camera) for privacy. If you can't see or hide specific files, please move them to your 'Downloads' folder first, then hide them from there.",
            iconTint = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )

        // Section 3: About Secure Vault
        InfoCard(
            icon = Icons.Default.Security,
            title = "Private & Offline",
            content = "Your hidden files are stored securely inside this app on your device. They are not visible in your gallery or other apps. No data is ever shared or uploaded to the cloud.",
            iconTint = MaterialTheme.colorScheme.onSurface,
            backgroundColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    iconTint: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (textColor == MaterialTheme.colorScheme.error) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.4f)
                )
            }
        }
    }
}
