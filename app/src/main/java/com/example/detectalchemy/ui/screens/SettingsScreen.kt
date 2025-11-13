package com.example.detectalchemy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.detectalchemy.data.FalconPreferences
import com.example.detectalchemy.detector.FalconDatasetHandler
import com.example.detectalchemy.viewmodel.DetectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val falconDatasetHandler = remember { FalconDatasetHandler(context) }

    var darkModeEnabled by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(1f) }
    var showFalconDialog by remember { mutableStateOf(false) }
    var falconUrl by remember { mutableStateOf("") }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadComplete by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    var falconConnected by remember { mutableStateOf(false) }
    var connectedUrl by remember { mutableStateOf("") }
    var datasetName: String? by remember { mutableStateOf(null) }
    var totalImages: Int? by remember { mutableStateOf(null) }
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            falconConnected = FalconPreferences.isConnected(context)
            connectedUrl = FalconPreferences.getFalconUrl(context) ?: ""
        } catch (e: Exception) {
            falconConnected = false
            connectedUrl = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A237E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D47A1),
                            Color(0xFF1565C0),
                            Color(0xFF1976D2)
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader("🎨 Appearance")
            }

            item {
                SettingCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enable dark theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it }
                        )
                    }
                }
            }

            item {
                SettingCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Font Size",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = { if (fontSize > 0.7f) fontSize -= 0.1f }) {
                                Text("- Smaller")
                            }

                            Text(
                                text = "${(fontSize * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Button(onClick = { if (fontSize < 1.5f) fontSize += 0.1f }) {
                                Text("Bigger +")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("🔗 Falcon Integration")
            }

            item {
                SettingCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔗 Falcon Integration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Connection status indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (falconConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (falconConnected) Color.Green else Color(0xFFFFA500),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (falconConnected) "Connected" else "Not Connected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (falconConnected) Color.Green else Color(0xFFFFA500)
                                )
                            }
                        }

                        if (falconConnected) {
                            // Enhanced accuracy indicator
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0D47A1).copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFF00BCD4),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Enhanced Accuracy: +25%",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00BCD4)
                                        )
                                    }

                                    // Dataset information
                                    datasetName?.let { name ->
                                        Text(
                                            text = "Dataset: $name",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    totalImages?.let { count ->
                                        if (count > 0) {
                                            Text(
                                                text = "Training Images: $count",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // Last sync info
                                    val lastSync = FalconPreferences.getLastSyncTime(context)
                                    if (lastSync > 0) {
                                        val syncTime = java.text.SimpleDateFormat(
                                            "MMM dd, HH:mm",
                                            java.util.Locale.getDefault()
                                        )
                                            .format(java.util.Date(lastSync))
                                        Text(
                                            text = "Last Sync: $syncTime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Active features list
                            EnhancedAccuracyFeatures()
                        }

                        // Connection buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showFalconDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (falconConnected) Icons.Default.Edit else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (falconConnected) "Update" else "Connect")
                            }

                            if (falconConnected) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isSyncing = true
                                            try {
                                                val apiKey =
                                                    FalconPreferences.getFalconApiKey(context)
                                                val datasetId =
                                                    FalconPreferences.getDatasetId(context)
                                                val url = FalconPreferences.getFalconUrl(context)

                                                val success = if (apiKey != null) {
                                                    falconDatasetHandler.syncDatasetWithApiKey(
                                                        apiKey,
                                                        datasetId
                                                    )
                                                } else if (url != null) {
                                                    falconDatasetHandler.syncDataset(url)
                                                } else {
                                                    false
                                                }

                                                if (success) {
                                                    // Update dataset info
                                                    datasetName =
                                                        FalconPreferences.getDatasetName(context)
                                                    totalImages =
                                                        FalconPreferences.getTotalImages(context)
                                                }
                                            } finally {
                                                isSyncing = false
                                            }
                                        }
                                    },
                                    enabled = !isSyncing,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isSyncing) "Syncing..." else "Sync")
                                }

                                OutlinedButton(
                                    onClick = {
                                        FalconPreferences.disconnect(context)
                                        falconConnected = false
                                        datasetName = null
                                        totalImages = null
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Red
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (falconConnected) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("🚀 Enhanced Accuracy Features")
                }

                item {
                    AccuracyFeaturesCard()
                }
            }
        }
    }

    // Falcon dialog
    if (showFalconDialog) {
        var connectionType by remember { mutableStateOf("API_KEY") } // "API_KEY" or "URL"
        var apiKeyInput by remember { mutableStateOf("") }
        var datasetIdInput by remember { mutableStateOf("") }
        var urlInput by remember { mutableStateOf("") }
        var isConnecting by remember { mutableStateOf(false) }
        var connectionError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                if (!isConnecting) {
                    showFalconDialog = false
                    connectionError = null
                }
            },
            title = { Text("Connect to Falcon Dataset") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Connection type tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { connectionType = "API_KEY" },
                            label = { Text("API Key") },
                            selected = connectionType == "API_KEY",
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { connectionType = "URL" },
                            label = { Text("URL") },
                            selected = connectionType == "URL",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (connectionType == "API_KEY") {
                        // API Key input
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                connectionError = null
                            },
                            label = { Text("Falcon API Key") },
                            placeholder = { Text("sk-falcon-...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Dataset ID input (optional)
                        OutlinedTextField(
                            value = datasetIdInput,
                            onValueChange = { datasetIdInput = it },
                            label = { Text("Dataset ID (optional)") },
                            placeholder = { Text("dataset_123") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "💡 Your API key will sync the dataset automatically and enable enhanced accuracy features",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        // URL input
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = {
                                urlInput = it
                                connectionError = null
                            },
                            label = { Text("Dataset URL") },
                            placeholder = { Text("https://api.falcon.ai/datasets/...") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "💡 Supported: .tflite models, .zip datasets, Falcon API endpoints",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    // Connection error
                    connectionError?.let { error ->
                        Text(
                            text = "❌ $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }

                    // Connection status
                    if (isConnecting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Connecting and syncing dataset...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isConnecting = true
                            connectionError = null

                            try {
                                val success = if (connectionType == "API_KEY") {
                                    if (apiKeyInput.isBlank()) {
                                        connectionError = "Please enter your API key"
                                        return@launch
                                    }

                                    // Save API key and attempt sync
                                    val datasetId = datasetIdInput.takeIf { it.isNotBlank() }
                                    FalconPreferences.saveFalconApiKey(
                                        context,
                                        apiKeyInput,
                                        datasetId
                                    )

                                    // Sync dataset with API key
                                    falconDatasetHandler.syncDatasetWithApiKey(
                                        apiKeyInput,
                                        datasetId
                                    )
                                } else {
                                    if (urlInput.isBlank() || !urlInput.startsWith("http")) {
                                        connectionError = "Please enter a valid HTTP/HTTPS URL"
                                        return@launch
                                    }

                                    // Save URL and attempt sync
                                    FalconPreferences.saveFalconUrl(context, urlInput)

                                    // Sync dataset from URL
                                    falconDatasetHandler.syncDataset(urlInput)
                                }

                                if (success) {
                                    falconConnected = true
                                    datasetName = FalconPreferences.getDatasetName(context)
                                    totalImages = FalconPreferences.getTotalImages(context)
                                    showFalconDialog = false
                                } else {
                                    connectionError =
                                        "Failed to connect or sync dataset. Please check your credentials/URL."
                                }
                            } catch (e: Exception) {
                                connectionError = "Connection failed: ${e.message}"
                            } finally {
                                isConnecting = false
                            }
                        }
                    },
                    enabled = !isConnecting && (
                            (connectionType == "API_KEY" && apiKeyInput.isNotBlank()) ||
                                    (connectionType == "URL" && urlInput.isNotBlank() && urlInput.startsWith(
                                        "http"
                                    ))
                            )
                ) {
                    Text(if (isConnecting) "Connecting..." else "Connect & Sync")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isConnecting) {
                            showFalconDialog = false
                            connectionError = null
                        }
                    },
                    enabled = !isConnecting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

@Composable
private fun EnhancedAccuracyFeatures() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🚀 Active Features",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )

        AccuracyFeatureItem(
            icon = Icons.Default.Settings,
            title = "Dynamic Thresholds",
            description = "Confidence: 60% vs 50% standard",
            isActive = true
        )

        AccuracyFeatureItem(
            icon = Icons.Default.Build,
            title = "Enhanced Preprocessing",
            description = "Brightness & contrast optimization",
            isActive = true
        )

        AccuracyFeatureItem(
            icon = Icons.Default.Add,
            title = "Ensemble Detection",
            description = "Multi-scale analysis",
            isActive = true
        )

        AccuracyFeatureItem(
            icon = Icons.Default.Star,
            title = "Real TensorFlow Lite",
            description = "Trained model inference",
            isActive = true
        )
    }
}

@Composable
private fun AccuracyFeaturesCard() {
    SettingCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Active Enhancements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AccuracyFeatureItem(
                icon = Icons.Default.Settings,
                title = "Dynamic Thresholds",
                description = "Confidence: 60% → 75% | NMS: 0.5 → 0.4",
                isActive = true
            )

            AccuracyFeatureItem(
                icon = Icons.Default.Build,
                title = "Enhanced Preprocessing",
                description = "Brightness & contrast optimization",
                isActive = true
            )

            AccuracyFeatureItem(
                icon = Icons.Default.Add, // Using Add icon
                title = "Ensemble Detection",
                description = "Multi-scale analysis for better accuracy",
                isActive = true
            )

            AccuracyFeatureItem(
                icon = Icons.Default.Home, // Using Home icon
                title = "Faster Processing",
                description = "300ms intervals vs 500ms standard",
                isActive = true
            )
        }
    }
}

@Composable
private fun AccuracyFeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isActive) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        if (isActive) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Active",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
