package com.devstudio.workspace.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devstudio.workspace.ui.theme.AppTheme
import com.devstudio.workspace.ui.theme.getDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppTheme = AppTheme.FOREST,
    onThemeChange: (AppTheme) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSave by remember { mutableStateOf(true) }
    var defaultCategory by remember { mutableStateOf("General") }
    
    val categories = listOf("General", "Personal", "Work", "Ideas", "Shopping", "Study", "Health", "Other")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Appearance Section
            item {
                SectionHeader(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                )
            }
            
            item {
                ModernSettingCard(
                    icon = Icons.Default.Palette,
                    iconBackground = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF1976D2),
                    title = "Theme",
                    subtitle = currentTheme.getDisplayName(),
                    onClick = { showThemeDialog = true }
                )
            }
            
            // Notes Section
            item {
                SectionHeader(
                    title = "Notes",
                    icon = Icons.Default.Note
                )
            }
            
            item {
                ModernSettingCard(
                    icon = Icons.Default.Save,
                    iconBackground = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF388E3C),
                    title = "Auto-Save",
                    subtitle = "Automatically save changes",
                    trailing = {
                        Switch(
                            checked = autoSave,
                            onCheckedChange = { autoSave = it }
                        )
                    }
                )
            }
            
            item {
                ModernSettingCard(
                    icon = Icons.Default.Notifications,
                    iconBackground = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFF57C00),
                    title = "Notifications",
                    subtitle = "Reminders and updates",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                )
            }
            
            item {
                ModernSettingCard(
                    icon = Icons.Default.Category,
                    iconBackground = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF7B1FA2),
                    title = "Default Category",
                    subtitle = defaultCategory,
                    onClick = { showCategoryDialog = true }
                )
            }
            
            // About
            item {
                SectionHeader(
                    title = "About",
                    icon = Icons.Default.Info
                )
            }
            
            item {
                ModernSettingCard(
                    icon = Icons.Default.Info,
                    iconBackground = Color(0xFFEDE7F6),
                    iconTint = Color(0xFF512DA8),
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
        
        // Theme Selection Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Palette,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { 
                    Text(
                        "Choose Theme",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppTheme.values().forEach { theme ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onThemeChange(theme)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentTheme == theme,
                                    onClick = {
                                        onThemeChange(theme)
                                        showThemeDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = theme.getDisplayName(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
        
        // Category Selection Dialog
        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Category,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { 
                    Text(
                        "Default Category",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        defaultCategory = category
                                        showCategoryDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = defaultCategory == category,
                                    onClick = {
                                        defaultCategory = category
                                        showCategoryDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ModernSettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colorful icon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
