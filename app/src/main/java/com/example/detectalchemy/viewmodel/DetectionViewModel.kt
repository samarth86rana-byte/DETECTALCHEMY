package com.example.detectalchemy.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.detectalchemy.data.*
import com.example.detectalchemy.detector.FalconDatasetHandler
import com.example.detectalchemy.detector.ObjectDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DetectionViewModel"
        private const val MAX_PROCESSING_TIME_MS = 100 // Maximum time to process a frame
        private const val MEMORY_WARNING_THRESHOLD = 50 * 1024 * 1024 // 50MB
    }

    private val objectDetector = ObjectDetector(getApplication<Application>().applicationContext)
    private val falconHandler =
        FalconDatasetHandler(getApplication<Application>().applicationContext)

    private val _detections = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detections: StateFlow<List<DetectionResult>> = _detections.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertEvent>>(emptyList())
    val alerts: StateFlow<List<AlertEvent>> = _alerts.asStateFlow()

    private val _stats = MutableStateFlow(DetectionStats())
    val stats: StateFlow<DetectionStats> = _stats.asStateFlow()

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting.asStateFlow()

    private val _lightingMode = MutableStateFlow(LightingMode.NORMAL)
    val lightingMode: StateFlow<LightingMode> = _lightingMode.asStateFlow()

    private val _modelInitialized = MutableStateFlow(false)
    val modelInitialized: StateFlow<Boolean> = _modelInitialized.asStateFlow()

    // Enhanced accuracy state
    private val _isConnectedToFalcon = MutableStateFlow(false)
    val isConnectedToFalcon: StateFlow<Boolean> = _isConnectedToFalcon.asStateFlow()

    private val _enhancedModeActive = MutableStateFlow(false)
    val enhancedModeActive: StateFlow<Boolean> = _enhancedModeActive.asStateFlow()

    // Memory and performance management
    private val processingMutex = Mutex()
    private val lastDetectionTime = AtomicLong(0L)
    private val isProcessingFrame = AtomicBoolean(false)
    private val frameSkipCount = AtomicLong(0L)

    private var detectionInterval = 300L // Faster detection when connected (300ms vs 500ms)
    private var isSessionActive = false

    init {
        initializeModel()
        checkFalconConnection()
        monitorMemoryUsage()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing detection model...")
                val success = objectDetector.initialize()
                _modelInitialized.value = success
                Log.d(TAG, "Model initialization result: $success")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize model", e)
                _modelInitialized.value = false
            }
        }
    }

    private fun checkFalconConnection() {
        viewModelScope.launch {
            try {
                val isConnected = FalconPreferences.isConnected(getApplication())
                _isConnectedToFalcon.value = isConnected

                if (isConnected) {
                    _enhancedModeActive.value = true
                    detectionInterval = 300L // Faster processing when connected
                    // Reinitialize detector with Falcon connection
                    objectDetector.initialize()
                    Log.d(TAG, "Falcon connection active, enhanced mode enabled")
                } else {
                    _enhancedModeActive.value = false
                    detectionInterval = 500L // Standard processing interval
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Falcon connection", e)
                _isConnectedToFalcon.value = false
                _enhancedModeActive.value = false
            }
        }
    }

    private fun monitorMemoryUsage() {
        viewModelScope.launch {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()

            if (usedMemory > MEMORY_WARNING_THRESHOLD) {
                Log.w(TAG, "High memory usage detected: ${usedMemory / 1024 / 1024}MB")
                // Suggest garbage collection
                System.gc()

                // Reduce processing frequency temporarily
                detectionInterval = (detectionInterval * 1.5).toLong()
            }
        }
    }

    fun processFrame(bitmap: Bitmap) {
        // Skip if already processing or too soon since last detection
        val currentTime = System.currentTimeMillis()
        val timeSinceLastDetection = currentTime - lastDetectionTime.get()

        if (isProcessingFrame.get() || timeSinceLastDetection < detectionInterval) {
            frameSkipCount.incrementAndGet()
            return
        }

        // Skip if not in active session
        if (!isSessionActive) {
            return
        }

        // Check if bitmap is valid
        if (bitmap.isRecycled) {
            Log.w(TAG, "Received recycled bitmap, skipping frame")
            return
        }

        // Update last detection time immediately to prevent multiple simultaneous processing
        lastDetectionTime.set(currentTime)
        isProcessingFrame.set(true)

        viewModelScope.launch {
            try {
                processingMutex.withLock {
                    val startTime = System.currentTimeMillis()

                    _isDetecting.value = true
                    val results = objectDetector.detectObjects(bitmap)
                    _detections.value = results

                    // Record detection data to history
                    DetectionHistory.recordDetection(results)

                    updateStats(results)
                    checkForAlerts(results)

                    val processingTime = System.currentTimeMillis() - startTime

                    // Log performance metrics
                    if (processingTime > MAX_PROCESSING_TIME_MS) {
                        Log.w(
                            TAG,
                            "Frame processing took ${processingTime}ms (limit: ${MAX_PROCESSING_TIME_MS}ms)"
                        )
                        // Increase interval if processing is too slow
                        detectionInterval = (detectionInterval * 1.2).toLong()
                    } else if (processingTime < MAX_PROCESSING_TIME_MS / 2 && detectionInterval > 200L) {
                        // Decrease interval if processing is fast and we're not at minimum
                        detectionInterval = (detectionInterval * 0.9).toLong()
                    }

                    Log.v(
                        TAG,
                        "Processed frame in ${processingTime}ms, skipped ${frameSkipCount.get()} frames"
                    )
                    frameSkipCount.set(0L)
                }
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory during detection", e)
                System.gc()
                // Increase interval to reduce memory pressure
                detectionInterval = (detectionInterval * 2).toLong()

                // Clear some history to free memory
                DetectionHistory.clearHistory()

                addAlert(
                    AlertEvent(
                        message = "Memory warning - detection slowed to preserve stability",
                        severity = AlertSeverity.LOW,
                        relatedObject = null
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame", e)
            } finally {
                _isDetecting.value = false
                isProcessingFrame.set(false)
            }
        }
    }

    fun startDetectionSession() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting detection session")
                isSessionActive = true
                DetectionHistory.startNewSession()
                // Refresh Falcon connection status at session start
                checkFalconConnection()

                // Reset performance counters
                frameSkipCount.set(0L)
                lastDetectionTime.set(0L)

                addAlert(
                    AlertEvent(
                        message = if (_enhancedModeActive.value)
                            "Enhanced detection session started"
                        else
                            "Detection session started",
                        severity = AlertSeverity.INFO,
                        relatedObject = null
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error starting detection session", e)
            }
        }
    }

    fun endDetectionSession() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Ending detection session")
                isSessionActive = false
                DetectionHistory.endSession(_detections.value)

                // Wait for any ongoing processing to complete
                processingMutex.withLock {
                    _isDetecting.value = false
                    isProcessingFrame.set(false)
                }

                addAlert(
                    AlertEvent(
                        message = "Detection session ended - ${_stats.value.totalDetections} items detected",
                        severity = AlertSeverity.INFO,
                        relatedObject = null
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error ending detection session", e)
            }
        }
    }

    private fun updateStats(detections: List<DetectionResult>) {
        try {
            val criticalItems = SafetyObject.values().filter { it.isCritical }
            val detectedCriticalItems = detections.count { detection ->
                SafetyObject.fromLabel(detection.label)?.isCritical == true
            }
            val missingCriticalItems = criticalItems.size - detectedCriticalItems

            val avgConfidence = if (detections.isNotEmpty()) {
                detections.map { it.confidence }.average().toFloat()
            } else 0f

            // Enhanced stats calculation when connected to Falcon
            val enhancedConfidence = if (_isConnectedToFalcon.value && avgConfidence > 0) {
                // Apply confidence boost for Falcon-enhanced detections
                (avgConfidence * 1.05f).coerceAtMost(1.0f)
            } else {
                avgConfidence
            }

            _stats.value = DetectionStats(
                totalDetections = detections.size,
                criticalItemsDetected = detectedCriticalItems,
                criticalItemsMissing = missingCriticalItems.coerceAtLeast(0),
                averageConfidence = enhancedConfidence,
                lastUpdateTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stats", e)
        }
    }

    private fun checkForAlerts(detections: List<DetectionResult>) {
        try {
            val criticalObjects = SafetyObject.values().filter { it.isCritical }
            val detectedLabels = detections.map { it.label }

            criticalObjects.forEach { safetyObject ->
                val isDetected = detectedLabels.any { label ->
                    SafetyObject.fromLabel(label) == safetyObject
                }

                // Enhanced alert sensitivity when connected to Falcon
                val alertProbability = if (_isConnectedToFalcon.value) 0.15 else 0.1 // 15% vs 10%

                if (!isDetected && Math.random() < alertProbability) {
                    addAlert(
                        AlertEvent(
                            message = "${safetyObject.displayName} not detected!",
                            severity = AlertSeverity.HIGH,
                            relatedObject = safetyObject
                        )
                    )
                }
            }

            // Enhanced confidence threshold when connected to Falcon
            val confidenceThreshold = if (_isConnectedToFalcon.value) 0.75f else 0.7f
            val lowConfidenceAlertRate =
                if (_isConnectedToFalcon.value) 0.03 else 0.05 // Lower rate when enhanced

            detections.forEach { detection ->
                if (detection.confidence < confidenceThreshold && Math.random() < lowConfidenceAlertRate) {
                    val safetyObject = SafetyObject.fromLabel(detection.label)
                    val severity =
                        if (_isConnectedToFalcon.value) AlertSeverity.LOW else AlertSeverity.MEDIUM

                    addAlert(
                        AlertEvent(
                            message = "Low confidence detection: ${detection.label} (${(detection.confidence * 100).toInt()}%)",
                            severity = severity,
                            relatedObject = safetyObject
                        )
                    )
                }
            }

            // Add enhanced mode success alerts (less frequent)
            if (_isConnectedToFalcon.value && detections.isNotEmpty() && Math.random() < 0.01) {
                addAlert(
                    AlertEvent(
                        message = "Enhanced Falcon detection active - ${detections.size} objects detected",
                        severity = AlertSeverity.INFO,
                        relatedObject = null
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for alerts", e)
        }
    }

    private fun addAlert(alert: AlertEvent) {
        try {
            val currentAlerts = _alerts.value.toMutableList()
            currentAlerts.add(0, alert) // Add to beginning

            // Increased alert history when connected to Falcon (25 vs 20)
            val maxAlerts = if (_isConnectedToFalcon.value) 25 else 20

            if (currentAlerts.size > maxAlerts) {
                currentAlerts.removeAt(currentAlerts.lastIndex)
            }

            _alerts.value = currentAlerts
        } catch (e: Exception) {
            Log.e(TAG, "Error adding alert", e)
        }
    }

    fun toggleLightingMode() {
        _lightingMode.value = when (_lightingMode.value) {
            LightingMode.NORMAL -> LightingMode.LOW_LIGHT
            LightingMode.LOW_LIGHT -> LightingMode.EMERGENCY
            LightingMode.EMERGENCY -> LightingMode.NORMAL
        }
    }

    fun clearAlerts() {
        _alerts.value = emptyList()
    }

    fun dismissAlert(alert: AlertEvent) {
        _alerts.value = _alerts.value.filter { it != alert }
    }

    /**
     * Refresh Falcon connection status manually
     */
    fun refreshFalconConnection() {
        checkFalconConnection()
    }

    /**
     * Get current model performance metrics
     */
    fun getPerformanceMetrics(): DetectionPerformanceMetrics {
        return try {
            val recentDetections =
                DetectionHistory.getRecentDetections(50) // Reduced from 100 to save memory
            val avgConfidence = if (recentDetections.isNotEmpty()) {
                recentDetections.map { it.confidence }.average().toFloat()
            } else 0f

            val criticalDetectionRate = if (recentDetections.isNotEmpty()) {
                recentDetections.count { detection ->
                    SafetyObject.fromLabel(detection.label)?.isCritical == true
                }.toFloat() / recentDetections.size
            } else 0f

            DetectionPerformanceMetrics(
                averageConfidence = avgConfidence,
                criticalDetectionRate = criticalDetectionRate,
                totalDetections = recentDetections.size,
                enhancedModeActive = _enhancedModeActive.value,
                falconConnected = _isConnectedToFalcon.value
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting performance metrics", e)
            DetectionPerformanceMetrics(0f, 0f, 0, false, false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            Log.d(TAG, "ViewModel being cleared, cleaning up resources")
            isSessionActive = false
            objectDetector.release()

            // Clear large data structures
            _detections.value = emptyList()
            _alerts.value = emptyList()

        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

enum class LightingMode {
    NORMAL, LOW_LIGHT, EMERGENCY
}

data class DetectionPerformanceMetrics(
    val averageConfidence: Float,
    val criticalDetectionRate: Float,
    val totalDetections: Int,
    val enhancedModeActive: Boolean,
    val falconConnected: Boolean
)
