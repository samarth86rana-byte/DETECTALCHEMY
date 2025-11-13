package com.example.detectalchemy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detectalchemy.data.DetectionStats
import com.example.detectalchemy.viewmodel.DetectionPerformanceMetrics
import kotlin.math.roundToInt

@Composable
fun StatsCard(
    stats: DetectionStats,
    isConnectedToFalcon: Boolean = false,
    enhancedModeActive: Boolean = false,
    performanceMetrics: DetectionPerformanceMetrics? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    // Enhanced colors when connected to Falcon
    val backgroundColor = if (isConnectedToFalcon) {
        animateColorAsState(
            targetValue = Color(0xFF0D47A1).copy(alpha = 0.95f),
            animationSpec = tween(500), label = "bg_color"
        ).value
    } else {
        Color.Black.copy(alpha = 0.7f)
    }

    val borderColor = if (isConnectedToFalcon) {
        Color(0xFF00BCD4) // Cyan accent for Falcon
    } else {
        Color(0xFF1976D2)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enhancedModeActive) Modifier.scale(pulseScale) else Modifier
            )
            .border(
                width = if (isConnectedToFalcon) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Enhanced header with Falcon status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detection Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (isConnectedToFalcon) {
                    FalconStatusIndicator(enhancedModeActive)
                }
            }

            // Main stats grid with enhanced styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = stats.totalDetections.toString(),
                    icon = Icons.Default.Search,
                    color = if (isConnectedToFalcon) Color(0xFF4CAF50) else Color(0xFF2196F3),
                    isEnhanced = isConnectedToFalcon
                )

                StatItem(
                    label = "Critical",
                    value = stats.criticalItemsDetected.toString(),
                    icon = Icons.Default.Warning,
                    color = if (stats.criticalItemsMissing > 0) Color(0xFFF44336) else Color(
                        0xFF4CAF50
                    ),
                    isEnhanced = isConnectedToFalcon
                )

                StatItem(
                    label = "Missing",
                    value = stats.criticalItemsMissing.toString(),
                    icon = Icons.Default.Close,
                    color = if (stats.criticalItemsMissing > 0) Color(0xFFFF9800) else Color(
                        0xFF4CAF50
                    ),
                    isEnhanced = isConnectedToFalcon
                )
            }

            // Enhanced confidence meter
            ConfidenceMeter(
                confidence = stats.averageConfidence,
                isEnhanced = isConnectedToFalcon,
                enhancedModeActive = enhancedModeActive
            )

            // Performance metrics when connected to Falcon
            if (isConnectedToFalcon && performanceMetrics != null) {
                EnhancedPerformanceSection(performanceMetrics)
            }
        }
    }
}

@Composable
private fun FalconStatusIndicator(enhancedModeActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "falcon_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = "falcon_alpha"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Falcon Connected",
            tint = Color(0xFF00BCD4).copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (enhancedModeActive) "ENHANCED" else "FALCON",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF00BCD4).copy(alpha = alpha),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isEnhanced: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(if (isEnhanced) 24.dp else 20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = if (isEnhanced) 20.sp else 18.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = if (isEnhanced) 11.sp else 10.sp
        )
    }
}

@Composable
private fun ConfidenceMeter(
    confidence: Float,
    isEnhanced: Boolean,
    enhancedModeActive: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Average Confidence",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Text(
                text = "${(confidence * 100).roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isEnhanced) Color(0xFF00BCD4) else Color.White
            )
        }

        // Enhanced progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isEnhanced) 8.dp else 6.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(50)
                )
        ) {
            val progressColor = when {
                confidence >= 0.8f -> Color(0xFF4CAF50)
                confidence >= 0.6f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }

            val enhancedColor = if (isEnhanced) {
                Brush.horizontalGradient(
                    colors = listOf(
                        progressColor,
                        Color(0xFF00BCD4),
                        progressColor
                    )
                )
            } else {
                Brush.horizontalGradient(colors = listOf(progressColor, progressColor))
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(confidence.coerceIn(0f, 1f))
                    .background(enhancedColor, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun EnhancedPerformanceSection(metrics: DetectionPerformanceMetrics) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚀 Enhanced Metrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4)
                )

                if (metrics.enhancedModeActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Enhanced",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EnhancedMetricItem(
                    label = "Critical Rate",
                    value = "${(metrics.criticalDetectionRate * 100).roundToInt()}%",
                    color = if (metrics.criticalDetectionRate > 0.7f) Color(0xFF4CAF50) else Color(
                        0xFFFF9800
                    )
                )

                EnhancedMetricItem(
                    label = "Total Scans",
                    value = metrics.totalDetections.toString(),
                    color = Color(0xFF00BCD4)
                )

                EnhancedMetricItem(
                    label = "Accuracy",
                    value = "${(metrics.averageConfidence * 100).roundToInt()}%",
                    color = if (metrics.averageConfidence > 0.8f) Color(0xFF4CAF50) else Color(
                        0xFFFF9800
                    )
                )
            }
        }
    }
}

@Composable
private fun EnhancedMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
    }
}
