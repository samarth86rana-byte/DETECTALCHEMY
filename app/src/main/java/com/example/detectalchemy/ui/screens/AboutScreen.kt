package com.example.detectalchemy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Logo Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🚀",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DETECTALCHEMY",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = "Space Station Safety Monitor",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Version 1.0.0",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Description Section
            item {
                InfoCard(
                    title = "📋 About This App",
                    content = "DETECTALCHEMY is an advanced AI-powered safety monitoring system designed for space station environments. Using real-time computer vision and YOLO object detection, it identifies and tracks critical safety equipment to ensure astronaut safety in orbital facilities."
                )
            }

            // Features Section
            item {
                InfoCard(
                    title = "✨ Key Features",
                    content = """
                        • Real-time safety equipment detection
                        • Live camera monitoring with bounding boxes
                        • AI-powered object recognition (YOLO)
                        • Safety equipment inventory management
                        • Analytics dashboard with accuracy metrics
                        • Automated safety reports generation
                        • System-level notifications for alerts
                        �� Multiple lighting simulation modes
                        • Falcon API integration support
                    """.trimIndent()
                )
            }

            // Safety Objects Section
            item {
                InfoCard(
                    title = "🛡️ Detected Safety Equipment",
                    content = """
                        Critical Items:
                        • Fire Extinguisher
                        • Oxygen Tank
                        • Fire Alarm
                        
                        Standard Items:
                        • First Aid Kit
                        • Emergency Light
                        • Safety Helmet
                        • Communication Device
                    """.trimIndent()
                )
            }

            // Technical Details
            item {
                InfoCard(
                    title = "🔧 Technical Details",
                    content = """
                        • Built with Kotlin & Jetpack Compose
                        • MVVM Architecture Pattern
                        • TensorFlow Lite for ML inference
                        • CameraX for camera integration
                        • Material Design 3 UI
                        • Android 7.0+ (API 24+)
                    """.trimIndent()
                )
            }

            // Credits Section
            item {
                InfoCard(
                    title = "👨‍🚀 Credits",
                    content = """
                        Developed for NASA Space Apps Challenge
                        
                        Technologies:
                        • YOLO (You Only Look Once) - Object Detection
                        • Falcon API - Synthetic Data Generation
                        • Android Jetpack - Modern Android Development
                        • Material Design 3 - UI Framework
                        
                        Special Thanks:
                        • NASA for inspiration
                        • Open-source community
                        • TensorFlow team
                    """.trimIndent()
                )
            }

            // License Section
            item {
                InfoCard(
                    title = "📜 License & Copyright",
                    content = """
                        © 2025 DETECTALCHEMY
                        
                        This application is developed for educational and demonstration purposes as part of the NASA Space Apps Challenge.
                        
                        For more information, visit:
                        github.com/detectalchemy
                    """.trimIndent()
                )
            }

            // Contact Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌟 Thank You for Using DETECTALCHEMY! 🌟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Stay safe in space! 🚀",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
            )
        }
    }
}
