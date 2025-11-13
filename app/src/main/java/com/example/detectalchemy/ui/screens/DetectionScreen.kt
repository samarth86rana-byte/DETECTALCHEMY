package com.example.detectalchemy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.detectalchemy.ui.components.*
import com.example.detectalchemy.viewmodel.DetectionViewModel
import com.example.detectalchemy.viewmodel.LightingMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Start detection session when screen is opened
    DisposableEffect(Unit) {
        viewModel.startDetectionSession()
        onDispose {
            viewModel.endDetectionSession()
        }
    }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    val detections by viewModel.detections.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val lightingMode by viewModel.lightingMode.collectAsStateWithLifecycle()
    val modelInitialized by viewModel.modelInitialized.collectAsStateWithLifecycle()

    // Enhanced accuracy states
    val isConnectedToFalcon by viewModel.isConnectedToFalcon.collectAsStateWithLifecycle()
    val enhancedModeActive by viewModel.enhancedModeActive.collectAsStateWithLifecycle()

    // Get performance metrics
    val performanceMetrics = remember(stats, isConnectedToFalcon) {
        if (isConnectedToFalcon) {
            viewModel.getPerformanceMetrics()
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                // Enhanced background when connected to Falcon
                if (isConnectedToFalcon) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color(0xFF0D47A1).copy(alpha = 0.3f),
                            Color.Black
                        )
                    )
                } else {
                    Brush.verticalGradient(colors = listOf(Color.Black, Color.Black))
                }
            )
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Camera Preview Layer
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    onFrameAnalyzed = { bitmap ->
                        viewModel.processFrame(bitmap)
                    }
                )

                // Lighting mode overlay
                when (lightingMode) {
                    LightingMode.LOW_LIGHT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                    }

                    LightingMode.EMERGENCY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red.copy(alpha = 0.15f))
                        )
                    }

                    else -> {}
                }
            }

            // Detection Overlay Layer
            DetectionOverlay(
                detections = detections,
                modifier = Modifier.fillMaxSize()
            )

            // UI Controls Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title
                    Column {
                        Text(
                            text = "DETECTALCHEMY",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Space Station Safety Monitor",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Lighting Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleLightingMode() },
                        modifier = Modifier
                            .background(
                                color = when (lightingMode) {
                                    LightingMode.NORMAL -> Color.White.copy(alpha = 0.2f)
                                    LightingMode.LOW_LIGHT -> Color.Blue.copy(alpha = 0.3f)
                                    LightingMode.EMERGENCY -> Color.Red.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Toggle lighting mode",
                            tint = when (lightingMode) {
                                LightingMode.NORMAL -> Color.White
                                LightingMode.LOW_LIGHT -> Color.Cyan
                                LightingMode.EMERGENCY -> Color.Red
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lighting Mode Indicator
                if (lightingMode != LightingMode.NORMAL) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (lightingMode) {
                                LightingMode.LOW_LIGHT -> Color.Blue.copy(alpha = 0.8f)
                                LightingMode.EMERGENCY -> Color.Red.copy(alpha = 0.8f)
                                else -> Color.Transparent
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (lightingMode) {
                                LightingMode.LOW_LIGHT -> "🌙 Low Light Mode"
                                LightingMode.EMERGENCY -> "🚨 Emergency Lighting"
                                else -> ""
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Stats Card
                StatsCard(
                    stats = stats,
                    isConnectedToFalcon = isConnectedToFalcon,
                    performanceMetrics = performanceMetrics
                )

                Spacer(modifier = Modifier.weight(1f))

                // Alert Panel
                AlertPanel(
                    alerts = alerts,
                    onDismissAlert = { viewModel.dismissAlert(it) },
                    onClearAll = { viewModel.clearAlerts() }
                )
            }

            // Model Status Indicator
            if (!modelInitialized) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Initializing Detection Model...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

        } else {
            // Permission Request Screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This app needs camera access to detect safety equipment in real-time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}
