package com.example.detectalchemy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Feature(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val features = listOf(
        Feature(
            id = "detection",
            title = "Real-Time Detection",
            description = "Scan for safety equipment using camera",
            icon = Icons.Default.Home,
            color = Color(0xFF2196F3)
        ),
        Feature(
            id = "inventory",
            title = "Safety Inventory",
            description = "View and manage detected equipment",
            icon = Icons.Default.Star,
            color = Color(0xFF4CAF50)
        ),
        Feature(
            id = "analytics",
            title = "Analytics Dashboard",
            description = "View detection statistics and trends",
            icon = Icons.Default.Settings,
            color = Color(0xFFFF9800)
        ),
        Feature(
            id = "reports",
            title = "Safety Reports",
            description = "Generate and view safety reports",
            icon = Icons.Default.Info,
            color = Color(0xFF9C27B0)
        ),
        Feature(
            id = "settings",
            title = "Settings",
            description = "Configure app preferences and model",
            icon = Icons.Default.Warning,
            color = Color(0xFF607D8B)
        ),
        Feature(
            id = "about",
            title = "About & Help",
            description = "Learn more about DETECTALCHEMY",
            icon = Icons.Default.Phone,
            color = Color(0xFF00BCD4)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DETECTALCHEMY",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Space Station Safety Monitor",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Welcome Back!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Select a feature to begin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Feature Grid
                Text(
                    "Feature Library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(features) { feature ->
                        FeatureCard(
                            feature = feature,
                            onClick = { onNavigateToFeature(feature.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    feature: Feature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = feature.enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (feature.enabled) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (feature.enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            feature.color.copy(alpha = 0.1f),
                            feature.color.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = feature.color.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = feature.color
                        )
                    }
                }

                // Text Content
                Column {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (feature.enabled)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = feature.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (feature.enabled)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
