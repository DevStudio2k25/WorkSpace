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
import com.devstudio.workspace.util.SecurePreferences
import kotlinx.coroutines.launch

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
    
    // AI Settings State
    val context = androidx.compose.ui.platform.LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }
    val scope = rememberCoroutineScope()
    
    var aiEnabled by remember { mutableStateOf(false) }
    var aiApiKey by remember { mutableStateOf("") }
    var aiModel by remember { mutableStateOf("") }
    var showAiConfigDialog by remember { mutableStateOf(false) }
    
    // Load AI Settings
    LaunchedEffect(Unit) {
        securePrefs.aiEnabled.collect { aiEnabled = it }
    }
    LaunchedEffect(Unit) {
        securePrefs.aiApiKey.collect { aiApiKey = it }
    }
    LaunchedEffect(Unit) {
        securePrefs.aiModel.collect { aiModel = it }
    }
    
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
            
            // AI Assistant Section
            item {
                SectionHeader(
                    title = "AI Assistant",
                    icon = Icons.Default.Face
                )
            }
            
            item {
                ModernSettingCard(
                    icon = Icons.Default.Face,
                    iconBackground = Color(0xFFF3E5F5), // Purple-ish
                    iconTint = Color(0xFF7B1FA2),
                    title = "Enable AI Assistant",
                    subtitle = if (aiEnabled) "AI features enabled" else "Disabled",
                    trailing = {
                        Switch(
                            checked = aiEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && (aiApiKey.isBlank() || aiModel.isBlank())) {
                                    showAiConfigDialog = true
                                } else {
                                    aiEnabled = enabled
                                    scope.launch {
                                        securePrefs.setAiEnabled(enabled)
                                    }
                                }
                            }
                        )
                    }
                )
            }
            
            if (aiEnabled || aiApiKey.isNotBlank()) {
                item {
                    ModernSettingCard(
                        icon = Icons.Default.Settings,
                        iconBackground = Color(0xFFE0F7FA), // Cyan-ish
                        iconTint = Color(0xFF0097A7),
                        title = "AI Configuration",
                        subtitle = "OpenRouter API Key & Model",
                        onClick = { showAiConfigDialog = true }
                    )
                }
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
        
        // AI Configuration Dialog
        if (showAiConfigDialog) {
            var inputKey by remember { mutableStateOf(aiApiKey) }
            var inputModel by remember { mutableStateOf(if (aiModel.isBlank()) "mistralai/mistral-7b-instruct:free" else aiModel) }
            var error by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showAiConfigDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Face,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { 
                    Text(
                        "Configure AI",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Enter your OpenRouter API Key and Model. This is required to use AI features.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            label = { Text("OpenRouter API Key") },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = inputModel,
                            onValueChange = { inputModel = it },
                            label = { Text("Model Name") },
                            placeholder = { Text("e.g. google/gemini-pro") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (error.isNotEmpty()) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputKey.isBlank() || inputModel.isBlank()) {
                                error = "Both fields are required."
                            } else {
                                scope.launch {
                                    securePrefs.setAiConfig(inputKey.trim(), inputModel.trim())
                                    // If setting up for first time, auto-enable
                                    if (!aiEnabled) {
                                        securePrefs.setAiEnabled(true)
                                        aiEnabled = true
                                    }
                                }
                                showAiConfigDialog = false
                            }
                        }
                    ) {
                        Text("Save & Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAiConfigDialog = false }) {
                        Text("Cancel")
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
