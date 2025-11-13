package com.example.detectalchemy.data

data class AlertEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val severity: AlertSeverity,
    val relatedObject: SafetyObject?
)

enum class AlertSeverity {
    INFO, LOW, MEDIUM, HIGH, CRITICAL
}

data class DetectionStats(
    val totalDetections: Int = 0,
    val criticalItemsDetected: Int = 0,
    val criticalItemsMissing: Int = 0,
    val averageConfidence: Float = 0f,
    val lastUpdateTime: Long = System.currentTimeMillis()
)
