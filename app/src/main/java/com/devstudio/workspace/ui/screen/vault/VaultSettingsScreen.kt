package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSettingsScreen(
    onBack: () -> Unit = {},
    onChangePattern: () -> Unit = {},
    onChangeKeyword: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val securePrefs = remember { com.devstudio.workspace.util.SecurePreferences(context) }
    val scope = rememberCoroutineScope()
    
    var biometricEnabled by remember { mutableStateOf(false) }
    var autoLock by remember { mutableStateOf(true) }
    var showKeywordDialog by remember { mutableStateOf(false) }
    var vaultKeyword by remember { mutableStateOf("OPEN VAULT") }
    
    // Load current keyword and biometric setting
    LaunchedEffect(Unit) {
        securePrefs.vaultKeyword.collect { keyword ->
            vaultKeyword = keyword
        }
    }
    
    LaunchedEffect(Unit) {
        securePrefs.useBiometric.collect { enabled ->
            biometricEnabled = enabled
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Vault Settings",
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Security Section Header
            item {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            // Set PIN Card
            item {
                SettingCard(
                    icon = Icons.Default.Pin,
                    title = "Change PIN",
                    subtitle = "Update your vault unlock PIN",
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onChangePattern
                )
            }
            
            // Fingerprint Toggle Card
            item {
                SettingCard(
                    icon = Icons.Default.Fingerprint,
                    title = "Fingerprint Unlock",
                    subtitle = if (biometricEnabled) "Enabled" else "Disabled",
                    iconColor = MaterialTheme.colorScheme.secondary,
                    trailing = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { 
                                biometricEnabled = it
                                scope.launch {
                                    securePrefs.setBiometric(it)
                                }
                            }
                        )
                    }
                )
            }
            
            // Auto-Lock Toggle Card
            item {
                SettingCard(
                    icon = Icons.Default.Lock,
                    title = "Auto-Lock Vault",
                    subtitle = if (autoLock) "Lock when app closes" else "Manual lock only",
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    trailing = {
                        Switch(
                            checked = autoLock,
                            onCheckedChange = { autoLock = it }
                        )
                    }
                )
            }
            
            // Access Section Header
            item {
                Text(
                    text = "Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 4.dp)
                )
            }
            
            // Vault Keyword Card
            item {
                SettingCard(
                    icon = Icons.Default.Key,
                    title = "Vault Keyword",
                    subtitle = "Current: \"$vaultKeyword\"",
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { showKeywordDialog = true }
                )
            }
        }
        
        // Keyword Change Dialog
        if (showKeywordDialog) {
            var newKeyword by remember { mutableStateOf(vaultKeyword) }
            
            AlertDialog(
                onDismissRequest = { showKeywordDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { 
                    Text(
                        "Change Vault Keyword",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column {
                        Text(
                            "Enter a keyword to access vault via search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newKeyword,
                            onValueChange = { newKeyword = it.uppercase() },
                            label = { Text("Keyword") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKeyword.isNotBlank()) {
                                scope.launch {
                                    securePrefs.setVaultKeyword(newKeyword)
                                    vaultKeyword = newKeyword
                                }
                                showKeywordDialog = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showKeywordDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Trailing content (switch or arrow)
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
