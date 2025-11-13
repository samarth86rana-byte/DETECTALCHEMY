package com.example.detectalchemy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detectalchemy.data.DetectionHistory
import com.example.detectalchemy.data.SafetyObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyReportsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use real detection data from DetectionHistory
    val currentSessionDetections by DetectionHistory.currentSessionDetections.collectAsState()

    val detectedItems = currentSessionDetections.toList()
    val missingItems = DetectionHistory.getMissingEquipment()

    val totalRequired = SafetyObject.values().size
    val detectedCount = detectedItems.size
    val safetyPercentage = DetectionHistory.getSafetyPercentage()
    val isSafe = safetyPercentage >= 70
    val criticalMissing = DetectionHistory.getCriticalMissing()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Report") },
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
            // Safety Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSafe) Color(0xFF4CAF50) else Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$safetyPercentage%",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSafe) Color(0xFF4CAF50) else Color(0xFFFF5722)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (detectedCount == 0) {
                                "⚠️ NO DETECTION RUN YET"
                            } else if (criticalMissing.isNotEmpty()) {
                                "❌ CRITICAL ITEMS MISSING"
                            } else if (isSafe) {
                                "✅ SAFE TO OPERATE"
                            } else {
                                "⚠️ SAFETY CHECK REQUIRED"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                detectedCount == 0 -> "Please run the Real-Time Detection to generate a safety report."
                                criticalMissing.isNotEmpty() -> "Critical safety equipment missing: ${
                                    criticalMissing.joinToString(
                                        ", "
                                    ) { it.displayName }
                                }. Station is NOT SECURE!"

                                isSafe -> "All critical safety equipment detected. Station is secure."
                                else -> "Some equipment missing. Review recommendations below."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Statistics Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatBox("Total Required", "$totalRequired", Color(0xFF2196F3))
                        StatBox("Detected", "$detectedCount", Color(0xFF4CAF50))
                        StatBox("Missing", "${missingItems.size}", Color(0xFFFF5722))
                    }
                }
            }

            // Detected Items Section
            item {
                SectionTitle("✅ Detected Equipment ($detectedCount)")
            }

            items(detectedItems.size) { index ->
                EquipmentCard(
                    item = detectedItems[index],
                    status = "Present",
                    statusColor = Color(0xFF4CAF50)
                )
            }

            // Missing Items Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("❌ Missing Equipment (${missingItems.size})")
            }

            items(missingItems.size) { index ->
                EquipmentCard(
                    item = missingItems[index],
                    status = "Missing",
                    statusColor = Color(0xFFFF5722)
                )
            }

            // Recommendations
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "⚠️ Recommendations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (missingItems.isNotEmpty()) {
                            missingItems.forEach { item ->
                                Text(
                                    text = "• Locate and install ${item.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "• All equipment present and accounted for\n• Continue regular maintenance schedule",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun EquipmentCard(
    item: SafetyObject,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (item.isCritical) "Critical Item" else "Standard Item",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isCritical) Color(0xFFFF5722) else Color.Gray
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
