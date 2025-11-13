package com.example.detectalchemy.data

import androidx.compose.ui.graphics.Color

data class DetectionResult(
    val id: String,
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

enum class SafetyObject(val displayName: String, val color: Color, val isCritical: Boolean) {
    OXYGEN_TANK("Oxygen Tank", Color(0xFF00BCD4), true),
    FIRE_EXTINGUISHER("Fire Extinguisher", Color(0xFFF44336), true),
    FIRE_ALARM("Fire Alarm", Color(0xFFFF5722), true),
    FIRST_AID_KIT("First Aid Kit", Color(0xFF4CAF50), false),
    EMERGENCY_LIGHT("Emergency Light", Color(0xFFFFEB3B), false),
    SAFETY_HELMET("Safety Helmet", Color(0xFF9C27B0), false),
    COMMUNICATION_DEVICE("Communication Device", Color(0xFF2196F3), false);

    companion object {
        fun fromLabel(label: String): SafetyObject? {
            return values().find {
                it.displayName.equals(label, ignoreCase = true) ||
                        it.name.equals(label, ignoreCase = true)
            }
        }
    }
}
