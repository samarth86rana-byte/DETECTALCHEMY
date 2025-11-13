package com.example.detectalchemy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detectalchemy.data.SafetyObject
import java.text.SimpleDateFormat
import java.util.*

data class InventoryItem(
    val id: String,
    val safetyObject: SafetyObject,
    val location: String,
    val lastSeen: Long,
    val confidence: Float,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock inventory data
    val inventoryItems = listOf(
        InventoryItem(
            "1",
            SafetyObject.FIRE_EXTINGUISHER,
            "Section A-12",
            System.currentTimeMillis() - 300000,
            0.95f,
            "Active"
        ),
        InventoryItem(
            "2",
            SafetyObject.OXYGEN_TANK,
            "Section B-5",
            System.currentTimeMillis() - 600000,
            0.88f,
            "Active"
        ),
        InventoryItem(
            "3",
            SafetyObject.FIRST_AID_KIT,
            "Section C-3",
            System.currentTimeMillis() - 900000,
            0.92f,
            "Active"
        ),
        InventoryItem(
            "4",
            SafetyObject.FIRE_ALARM,
            "Section A-15",
            System.currentTimeMillis() - 1200000,
            0.79f,
            "Warning"
        ),
        InventoryItem(
            "5",
            SafetyObject.EMERGENCY_LIGHT,
            "Section D-8",
            System.currentTimeMillis() - 1800000,
            0.85f,
            "Active"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Inventory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SummaryItem("Total Items", inventoryItems.size.toString(), Color(0xFF2196F3))
                    SummaryItem(
                        "Active",
                        inventoryItems.count { it.status == "Active" }.toString(),
                        Color(0xFF4CAF50)
                    )
                    SummaryItem(
                        "Warnings",
                        inventoryItems.count { it.status == "Warning" }.toString(),
                        Color(0xFFFF9800)
                    )
                }
            }

            Text(
                "Equipment List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(inventoryItems) { item ->
                    InventoryItemCard(item)
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun InventoryItemCard(item: InventoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    text = item.safetyObject.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: ${item.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Last seen: ${formatTime(item.lastSeen)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.status == "Active")
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else
                        Color(0xFFFF9800).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = item.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.status == "Active") Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(item.confidence * 100).toInt()}% confidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} min ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
