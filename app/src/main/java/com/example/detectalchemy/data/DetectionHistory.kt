package com.example.detectalchemy.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DetectionHistory {
    private val _detectionSessions = MutableStateFlow<List<DetectionSession>>(emptyList())
    val detectionSessions: StateFlow<List<DetectionSession>> = _detectionSessions.asStateFlow()

    private val _totalScans = MutableStateFlow(0)
    val totalScans: StateFlow<Int> = _totalScans.asStateFlow()

    private val _detectedItems = MutableStateFlow<Map<SafetyObject, DetectionStats>>(emptyMap())
    val detectedItems: StateFlow<Map<SafetyObject, DetectionStats>> = _detectedItems.asStateFlow()

    private val _currentSessionDetections = MutableStateFlow<Set<SafetyObject>>(emptySet())
    val currentSessionDetections: StateFlow<Set<SafetyObject>> =
        _currentSessionDetections.asStateFlow()

    data class DetectionSession(
        val timestamp: Long,
        val duration: Long,
        val detectedObjects: List<DetectionResult>,
        val uniqueItemsDetected: Set<SafetyObject>
    )

    data class DetectionStats(
        val totalDetections: Int,
        val averageConfidence: Float,
        val lastSeen: Long,
        val successfulDetections: Int
    )

    fun recordDetection(results: List<DetectionResult>) {
        _totalScans.value++

        // Update unique items detected in current session
        val uniqueItems = results.mapNotNull { SafetyObject.fromLabel(it.label) }.toSet()
        _currentSessionDetections.value = _currentSessionDetections.value + uniqueItems

        // Update detection stats for each item
        val currentStats = _detectedItems.value.toMutableMap()

        results.forEach { detection ->
            SafetyObject.fromLabel(detection.label)?.let { safetyObject ->
                val existingStats = currentStats[safetyObject]
                val newTotal = (existingStats?.totalDetections ?: 0) + 1
                val newSuccessful = if (detection.confidence >= 0.7f)
                    (existingStats?.successfulDetections ?: 0) + 1
                else
                    (existingStats?.successfulDetections ?: 0)

                val newAvgConfidence = if (existingStats != null) {
                    (existingStats.averageConfidence * existingStats.totalDetections + detection.confidence) / newTotal
                } else {
                    detection.confidence
                }

                currentStats[safetyObject] = DetectionStats(
                    totalDetections = newTotal,
                    averageConfidence = newAvgConfidence,
                    lastSeen = System.currentTimeMillis(),
                    successfulDetections = newSuccessful
                )
            }
        }

        _detectedItems.value = currentStats
    }

    fun startNewSession() {
        _currentSessionDetections.value = emptySet()
    }

    fun endSession(results: List<DetectionResult>) {
        val session = DetectionSession(
            timestamp = System.currentTimeMillis(),
            duration = 0L, // Could track actual duration
            detectedObjects = results,
            uniqueItemsDetected = _currentSessionDetections.value
        )

        _detectionSessions.value =
            listOf(session) + _detectionSessions.value.take(49) // Keep last 50
    }

    fun getOverallAccuracy(): Float {
        if (_detectedItems.value.isEmpty()) return 0f

        val totalSuccessful = _detectedItems.value.values.sumOf { it.successfulDetections }
        val totalDetections = _detectedItems.value.values.sumOf { it.totalDetections }

        return if (totalDetections > 0) {
            (totalSuccessful.toFloat() / totalDetections * 100)
        } else {
            0f
        }
    }

    fun getSafetyPercentage(): Int {
        val totalRequired = SafetyObject.values().size
        val detected = _currentSessionDetections.value.size
        return ((detected.toFloat() / totalRequired) * 100).toInt()
    }

    fun getDetectedEquipment(): List<SafetyObject> {
        return _currentSessionDetections.value.toList()
    }

    fun getMissingEquipment(): List<SafetyObject> {
        return SafetyObject.values().filter { it !in _currentSessionDetections.value }
    }

    fun getCriticalMissing(): List<SafetyObject> {
        return getMissingEquipment().filter { it.isCritical }
    }

    fun clearHistory() {
        _detectionSessions.value = emptyList()
        _totalScans.value = 0
        _detectedItems.value = emptyMap()
        _currentSessionDetections.value = emptySet()
    }

    /**
     * Get recent detection results for performance analysis
     */
    fun getRecentDetections(maxResults: Int = 100): List<DetectionResult> {
        return _detectionSessions.value
            .take(maxResults / 10) // Take recent sessions
            .flatMap { session -> session.detectedObjects }
            .take(maxResults)
    }
}
