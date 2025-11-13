package com.example.detectalchemy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.detectalchemy.data.DetectionResult
import com.example.detectalchemy.data.SafetyObject

@Composable
fun DetectionOverlay(
    detections: List<DetectionResult>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        detections.forEach { detection ->
            val safetyObject = SafetyObject.fromLabel(detection.label)
            val color = safetyObject?.color ?: Color.White

            // Convert normalized coordinates to canvas coordinates
            val rect = Rect(
                left = detection.boundingBox.left * size.width,
                top = detection.boundingBox.top * size.height,
                right = detection.boundingBox.right * size.width,
                bottom = detection.boundingBox.bottom * size.height
            )

            // Draw bounding box
            drawRect(
                color = color,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw filled rectangle for label background
            val labelHeight = 30.dp.toPx()
            drawRect(
                color = color.copy(alpha = 0.8f),
                topLeft = Offset(rect.left, rect.top - labelHeight),
                size = Size(rect.width.coerceAtLeast(100.dp.toPx()), labelHeight)
            )
        }
    }

    // Draw text labels separately using Compose Text
    Box(modifier = modifier.fillMaxSize()) {
        detections.forEach { detection ->
            val rect = Rect(
                left = detection.boundingBox.left,
                top = detection.boundingBox.top,
                right = detection.boundingBox.right,
                bottom = detection.boundingBox.bottom
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .absoluteOffset(
                        x = (rect.left * 1000).dp / 1000,  // Placeholder for actual size calculation
                        y = ((rect.top - 0.035f).coerceAtLeast(0f) * 1000).dp / 1000
                    )
            ) {
                Text(
                    text = "${detection.label} ${(detection.confidence * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
