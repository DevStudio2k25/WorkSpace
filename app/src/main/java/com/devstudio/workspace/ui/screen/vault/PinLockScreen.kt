package com.devstudio.workspace.ui.screen.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    isSetup: Boolean = false,
    showError: Boolean = false,
    onPinComplete: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var showLocalError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Update error message when showError changes
    LaunchedEffect(showError) {
        if (showError) {
            showLocalError = true
            errorMessage = "Wrong PIN! Try again"
            // Reset PIN
            kotlinx.coroutines.delay(500)
            pin = ""
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            isSetup && !isConfirming -> "Set PIN"
                            isSetup && isConfirming -> "Confirm PIN"
                            else -> "Enter PIN"
                        },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Lock Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = when {
                    isSetup && !isConfirming -> "Create Your PIN"
                    isSetup && isConfirming -> "Confirm Your PIN"
                    else -> "Unlock Vault"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    isSetup && !isConfirming -> "Enter a 4-digit PIN"
                    isSetup && isConfirming -> "Re-enter your PIN"
                    else -> "Enter your 4-digit PIN"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // PIN Dots Display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) { index ->
                    Surface(
                        shape = CircleShape,
                        color = if (pin.length > index) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(20.dp)
                    ) {}
                }
            }
            
            // Error Message
            if (showLocalError) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Custom Number Pad
            CustomNumberPad(
                onNumberClick = { number ->
                    if (pin.length < 4) {
                        pin += number
                        showLocalError = false
                        
                        // Auto-submit when 4 digits
                        if (pin.length == 4) {
                            if (isSetup) {
                                if (!isConfirming) {
                                    // First PIN entered
                                    confirmPin = pin
                                    isConfirming = true
                                    pin = ""
                                } else {
                                    // Confirm PIN
                                    if (pin == confirmPin) {
                                        onPinComplete(pin)
                                    } else {
                                        showLocalError = true
                                        errorMessage = "PINs don't match!"
                                        pin = ""
                                    }
                                }
                            } else {
                                // Unlock mode - call callback (don't reset here)
                                onPinComplete(pin)
                            }
                        }
                    }
                },
                onBackspace = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                        showLocalError = false
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CustomNumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1-3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NumberButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            NumberButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            NumberButton("3", Modifier.weight(1f)) { onNumberClick("3") }
        }
        
        // Row 4-6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NumberButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            NumberButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            NumberButton("6", Modifier.weight(1f)) { onNumberClick("6") }
        }
        
        // Row 7-9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NumberButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            NumberButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            NumberButton("9", Modifier.weight(1f)) { onNumberClick("9") }
        }
        
        // Row 0 and Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            
            // Backspace button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onBackspace() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NumberButton(
    number: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 32.sp
            )
        }
    }
}
