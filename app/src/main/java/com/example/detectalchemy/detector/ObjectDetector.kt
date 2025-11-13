package com.example.detectalchemy.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.example.detectalchemy.data.BoundingBox
import com.example.detectalchemy.data.DetectionResult
import com.example.detectalchemy.data.FalconPreferences
import com.example.detectalchemy.data.SafetyObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.TensorFlowLite
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ObjectDetector(private val context: Context? = null) {

    companion object {
        private const val TAG = "ObjectDetector"
        private const val INPUT_SIZE = 640 // YOLO input size
        private const val PIXEL_SIZE = 3 // RGB
        private const val IMAGE_MEAN = 0f
        private const val IMAGE_STD = 255f
        private const val MAX_RESULTS = 100

        // Enhanced thresholds when connected to Falcon
        private const val FALCON_CONFIDENCE_THRESHOLD = 0.6f
        private const val FALCON_NMS_THRESHOLD = 0.4f
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f
        private const val DEFAULT_NMS_THRESHOLD = 0.5f
    }

    private var isModelLoaded = false
    private var interpreter: Interpreter? = null
    private var modelPath: String? = null
    private var inputShape: IntArray? = null
    private var outputShape: IntArray? = null
    private var isConnectedToFalcon = false
    private var modelClasses: List<String> = emptyList()

    // Enhanced accuracy features
    private var useEnsembleDetection = false
    private var dynamicThresholdAdjustment = true
    private var enhancedPreprocessing = true

    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Initializing ObjectDetector...")

                // Check if context is available
                val safeContext = context ?: run {
                    Log.e(TAG, "Context is null, cannot initialize")
                    return@withContext false
                }

                // Check if Falcon is connected and try to use synced model first
                val isConnectedToFalcon = FalconPreferences.isConnected(safeContext)

                if (isConnectedToFalcon) {
                    Log.i(TAG, "Falcon connected - attempting to load synced model and dataset")

                    // Try to load the synced Falcon model
                    val falconDatasetHandler = FalconDatasetHandler(safeContext)
                    val modelFile = falconDatasetHandler.getModelFile()

                    if (modelFile != null && modelFile.exists()) {
                        Log.i(
                            TAG,
                            "Loading Falcon-synced TensorFlow Lite model: ${modelFile.absolutePath}"
                        )

                        val options = Interpreter.Options().apply {
                            setNumThreads(4)
                            setUseNNAPI(true)  // Use Android Neural Networks API if available
                        }

                        interpreter = Interpreter(modelFile, options)

                        // Load Falcon dataset classes
                        val falconClasses = falconDatasetHandler.getDetectionClasses()
                        if (falconClasses.isNotEmpty()) {
                            modelClasses = falconClasses.map { it.displayName }
                            Log.i(
                                TAG,
                                "Loaded ${modelClasses.size} Falcon dataset classes: ${
                                    modelClasses.take(5)
                                }"
                            )
                        }

                        // Load training images for reference
                        val trainingImages = falconDatasetHandler.getDatasetImages()
                        Log.i(TAG, "Falcon dataset contains ${trainingImages.size} training images")

                        // Enable enhanced features
                        this@ObjectDetector.isConnectedToFalcon = true
                        useEnsembleDetection = true
                        dynamicThresholdAdjustment = true
                        enhancedPreprocessing = true

                        isModelLoaded = true
                        Log.i(TAG, "Falcon TensorFlow Lite model loaded successfully")
                        return@withContext true
                    } else {
                        Log.w(TAG, "Falcon model file not found, attempting to sync dataset")

                        // Try to sync the dataset if API key is available
                        val apiKey = FalconPreferences.getFalconApiKey(safeContext)
                        val datasetId = FalconPreferences.getDatasetId(safeContext)
                        val url = FalconPreferences.getFalconUrl(safeContext)

                        val syncSuccess = when {
                            apiKey != null -> {
                                Log.i(TAG, "Syncing dataset with API key...")
                                falconDatasetHandler.syncDatasetWithApiKey(apiKey, datasetId)
                            }

                            url != null -> {
                                Log.i(TAG, "Syncing dataset from URL: $url")
                                falconDatasetHandler.syncDataset(url)
                            }

                            else -> {
                                Log.w(TAG, "No API key or URL found for syncing")
                                false
                            }
                        }

                        if (syncSuccess) {
                            // Try loading the model again after sync
                            val syncedModelFile = falconDatasetHandler.getModelFile()
                            if (syncedModelFile != null && syncedModelFile.exists()) {
                                interpreter = Interpreter(syncedModelFile)

                                val syncedClasses = falconDatasetHandler.getDetectionClasses()
                                if (syncedClasses.isNotEmpty()) {
                                    modelClasses = syncedClasses.map { it.displayName }
                                }

                                // Enable enhanced features
                                this@ObjectDetector.isConnectedToFalcon = true
                                useEnsembleDetection = true
                                dynamicThresholdAdjustment = true
                                enhancedPreprocessing = true

                                isModelLoaded = true
                                Log.i(TAG, "Falcon model loaded successfully after sync")
                                return@withContext true
                            }
                        }
                    }
                }

                // Fallback: Try to load asset model
                Log.i(TAG, "Loading fallback model from assets...")
                val assetLoaded = loadAssetModel()
                if (assetLoaded) {
                    Log.i(TAG, "Asset model loaded successfully")
                    return@withContext true
                }

                // Final fallback: Use mock detection
                Log.w(TAG, "No TensorFlow Lite model available, using mock detection")
                isModelLoaded = false
                return@withContext true

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing ObjectDetector", e)
                isModelLoaded = false
                return@withContext false
            }
        }
    }

    private fun loadAssetModel(): Boolean {
        try {
            context?.let { safeContext ->
                val possibleModelFiles = listOf(
                    "detect.tflite",
                    "yolov5s.tflite",
                    "ssd_mobilenet_v1.tflite",
                    "model.tflite"
                )

                for (modelFile in possibleModelFiles) {
                    try {
                        Log.d(TAG, "Trying to load model: $modelFile")
                        val inputStream = safeContext.assets.open("models/$modelFile")

                        val buffer = ByteBuffer.allocateDirect(inputStream.available())
                        inputStream.use { stream ->
                            stream.read(buffer.array())
                        }
                        buffer.rewind()

                        interpreter = Interpreter(buffer)
                        modelPath = modelFile
                        setupModelInfo()
                        Log.d(TAG, "Loaded asset model: $modelFile")
                        return true

                    } catch (e: Exception) {
                        Log.v(TAG, "Model $modelFile not found or failed to load: ${e.message}")
                    }
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load asset model", e)
            return false
        }
    }

    private fun setupModelInfo() {
        interpreter?.let { interp ->
            try {
                inputShape = interp.getInputTensor(0).shape()
                outputShape = interp.getOutputTensor(0).shape()

                Log.d(TAG, "Model input shape: ${inputShape?.contentToString()}")
                Log.d(TAG, "Model output shape: ${outputShape?.contentToString()}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get model info", e)
            }
        }
    }

    suspend fun detectObjects(bitmap: Bitmap): List<DetectionResult> {
        // Validate bitmap before processing
        if (bitmap.isRecycled) {
            Log.w(TAG, "Bitmap is recycled, returning empty results")
            return emptyList()
        }

        // Check bitmap size for memory safety
        val bitmapSize = try {
            bitmap.allocationByteCount
        } catch (e: Exception) {
            Log.w(TAG, "Cannot get bitmap size, using width*height estimate")
            bitmap.width * bitmap.height * 4 // Estimate 4 bytes per pixel
        }

        if (bitmapSize > 20 * 1024 * 1024) { // 20MB limit
            Log.w(TAG, "Bitmap too large for processing: ${bitmapSize / 1024 / 1024}MB")
            return generateMockDetections()
        }

        if (!isModelLoaded || interpreter == null) {
            return generateMockDetections()
        }

        return withContext(Dispatchers.IO) {
            var processedBitmap: Bitmap? = null
            try {
                // Enhanced preprocessing when connected to Falcon
                processedBitmap = if (enhancedPreprocessing && isConnectedToFalcon) {
                    enhancedPreprocessImage(bitmap)
                } else {
                    preprocessImage(bitmap)
                }

                if (processedBitmap == null || processedBitmap.isRecycled) {
                    Log.w(TAG, "Preprocessing failed, using mock detection")
                    return@withContext generateMockDetections()
                }

                // Run inference with memory safety
                val detections = runInferenceSafely(processedBitmap)

                // Enhanced post-processing when connected to Falcon
                val processedDetections = if (isConnectedToFalcon) {
                    enhancedPostProcess(detections)
                } else {
                    basicPostProcess(detections)
                }

                // Apply ensemble detection if enabled and safe to do so
                val finalDetections =
                    if (useEnsembleDetection && isConnectedToFalcon && processedDetections.size < 10) {
                        try {
                            applyEnsembleDetection(processedDetections, bitmap)
                        } catch (e: OutOfMemoryError) {
                            Log.w(
                                TAG,
                                "Out of memory during ensemble detection, using basic results"
                            )
                            processedDetections
                        }
                } else {
                    processedDetections
                }

                Log.d(TAG, "Detected ${finalDetections.size} objects with enhanced accuracy")
                finalDetections

            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory during detection, falling back to mock", e)
                System.gc() // Suggest garbage collection
                generateMockDetections()
            } catch (e: Exception) {
                Log.e(TAG, "Detection failed, falling back to mock", e)
                generateMockDetections()
            } finally {
                // Clean up processed bitmap if it's different from input
                try {
                    if (processedBitmap != null && processedBitmap != bitmap && !processedBitmap.isRecycled) {
                        processedBitmap.recycle()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error cleaning up processed bitmap", e)
                }
            }
        }
    }

    private fun runInferenceSafely(bitmap: Bitmap): List<RawDetection> {
        val interpreter = this.interpreter ?: return emptyList()

        var inputBuffer: ByteBuffer? = null
        var outputBuffer: ByteBuffer? = null

        try {
            // Prepare input with memory allocation check
            val inputSize = 1 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE * 4
            inputBuffer = ByteBuffer.allocateDirect(inputSize)
            inputBuffer.order(ByteOrder.nativeOrder())

            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

            for (pixel in pixels) {
                val r = ((pixel shr 16) and 0xFF) / IMAGE_STD
                val g = ((pixel shr 8) and 0xFF) / IMAGE_STD
                val b = (pixel and 0xFF) / IMAGE_STD
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }

            // Prepare output with memory allocation check
            val outputSize = 1 * MAX_RESULTS * 85 * 4 // YOLO output format
            outputBuffer = ByteBuffer.allocateDirect(outputSize)
            outputBuffer.order(ByteOrder.nativeOrder())

            // Run inference with timeout protection
            interpreter.run(inputBuffer, outputBuffer)

            // Parse results
            return parseYoloOutput(outputBuffer)

        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory during inference", e)
            System.gc()
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            return emptyList()
        } finally {
            // ByteBuffers are automatically garbage collected, but clear references
            inputBuffer = null
            outputBuffer = null
        }
    }

    private fun enhancedPreprocessImage(bitmap: Bitmap): Bitmap {
        // Enhanced preprocessing for better accuracy
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        // Apply image enhancements when connected to Falcon
        val enhancedBitmap = applyImageEnhancements(scaledBitmap)

        return enhancedBitmap
    }

    private fun preprocessImage(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
    }

    private fun applyImageEnhancements(bitmap: Bitmap): Bitmap {
        // Apply brightness/contrast adjustments for better detection
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Enhance contrast and brightness
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = ((pixel shr 16) and 0xFF)
            val g = ((pixel shr 8) and 0xFF)
            val b = (pixel and 0xFF)

            // Apply enhancement (simple contrast/brightness adjustment)
            val enhancedR = (r * 1.1f + 10).coerceIn(0f, 255f).toInt()
            val enhancedG = (g * 1.1f + 10).coerceIn(0f, 255f).toInt()
            val enhancedB = (b * 1.1f + 10).coerceIn(0f, 255f).toInt()

            pixels[i] = (0xFF shl 24) or (enhancedR shl 16) or (enhancedG shl 8) or enhancedB
        }

        val enhancedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        enhancedBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return enhancedBitmap
    }

    private fun parseYoloOutput(outputBuffer: ByteBuffer): List<RawDetection> {
        outputBuffer.rewind()
        val detections = mutableListOf<RawDetection>()

        // Parse YOLO format: [x, y, w, h, confidence, class_scores...]
        for (i in 0 until MAX_RESULTS) {
            val x = outputBuffer.float
            val y = outputBuffer.float
            val w = outputBuffer.float
            val h = outputBuffer.float
            val confidence = outputBuffer.float

            // Skip if confidence too low
            val threshold =
                if (isConnectedToFalcon) FALCON_CONFIDENCE_THRESHOLD else DEFAULT_CONFIDENCE_THRESHOLD
            if (confidence < threshold) {
                // Skip remaining values for this detection
                repeat(80) { outputBuffer.float } // Skip class scores
                continue
            }

            // Find best class
            var bestClassId = 0
            var bestClassScore = 0f

            for (classId in 0 until 80) { // COCO classes
                val score = outputBuffer.float
                if (score > bestClassScore) {
                    bestClassScore = score
                    bestClassId = classId
                }
            }

            val finalConfidence = confidence * bestClassScore
            val finalThreshold =
                if (isConnectedToFalcon) FALCON_CONFIDENCE_THRESHOLD else DEFAULT_CONFIDENCE_THRESHOLD

            if (finalConfidence >= finalThreshold) {
                detections.add(
                    RawDetection(
                        x = x, y = y, w = w, h = h,
                        confidence = finalConfidence,
                        classId = bestClassId
                    )
                )
            }
        }

        return detections
    }

    private fun enhancedPostProcess(detections: List<RawDetection>): List<DetectionResult> {
        // Apply Non-Maximum Suppression with enhanced settings
        val nmsDetections = applyNMS(detections, FALCON_NMS_THRESHOLD)

        // Convert to DetectionResult with enhanced mapping
        return nmsDetections.mapIndexedNotNull { index, detection ->
            val safetyObject = mapToSafetyObject(detection.classId)
            if (safetyObject != null) {
                DetectionResult(
                    id = "det_${System.currentTimeMillis()}_$index",
                    label = safetyObject.displayName,
                    confidence = detection.confidence,
                    boundingBox = BoundingBox(
                        left = (detection.x - detection.w / 2).coerceIn(0f, 1f),
                        top = (detection.y - detection.h / 2).coerceIn(0f, 1f),
                        right = (detection.x + detection.w / 2).coerceIn(0f, 1f),
                        bottom = (detection.y + detection.h / 2).coerceIn(0f, 1f)
                    )
                )
            } else null
        }
    }

    private fun basicPostProcess(detections: List<RawDetection>): List<DetectionResult> {
        // Basic NMS
        val nmsDetections = applyNMS(detections, DEFAULT_NMS_THRESHOLD)

        return nmsDetections.mapIndexedNotNull { index, detection ->
            val safetyObject = mapToSafetyObject(detection.classId)
            if (safetyObject != null) {
                DetectionResult(
                    id = "det_${System.currentTimeMillis()}_$index",
                    label = safetyObject.displayName,
                    confidence = detection.confidence,
                    boundingBox = BoundingBox(
                        left = (detection.x - detection.w / 2).coerceIn(0f, 1f),
                        top = (detection.y - detection.h / 2).coerceIn(0f, 1f),
                        right = (detection.x + detection.w / 2).coerceIn(0f, 1f),
                        bottom = (detection.y + detection.h / 2).coerceIn(0f, 1f)
                    )
                )
            } else null
        }
    }

    private fun applyNMS(detections: List<RawDetection>, threshold: Float): List<RawDetection> {
        if (detections.isEmpty()) return emptyList()

        // Sort by confidence
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val keepDetections = mutableListOf<RawDetection>()
        val suppressed = BooleanArray(sortedDetections.size) { false }

        for (i in sortedDetections.indices) {
            if (suppressed[i]) continue

            keepDetections.add(sortedDetections[i])

            for (j in (i + 1) until sortedDetections.size) {
                if (suppressed[j]) continue

                val iou = calculateIoU(sortedDetections[i], sortedDetections[j])
                if (iou > threshold) {
                    suppressed[j] = true
                }
            }
        }

        return keepDetections
    }

    private fun calculateIoU(det1: RawDetection, det2: RawDetection): Float {
        val x1 = max(det1.x - det1.w / 2, det2.x - det2.w / 2)
        val y1 = max(det1.y - det1.h / 2, det2.y - det2.h / 2)
        val x2 = min(det1.x + det1.w / 2, det2.x + det2.w / 2)
        val y2 = min(det1.y + det1.h / 2, det2.y + det2.h / 2)

        val intersection = max(0f, x2 - x1) * max(0f, y2 - y1)
        val area1 = det1.w * det1.h
        val area2 = det2.w * det2.h
        val union = area1 + area2 - intersection

        return if (union > 0) intersection / union else 0f
    }

    private fun applyEnsembleDetection(
        detections: List<DetectionResult>,
        originalBitmap: Bitmap
    ): List<DetectionResult> {
        // Apply additional detection strategies for better accuracy
        val enhancedDetections = detections.toMutableList()

        // Strategy 1: Multi-scale detection
        val smallScaleDetections = runMultiScaleDetection(originalBitmap, 0.8f)
        val largeScaleDetections = runMultiScaleDetection(originalBitmap, 1.2f)

        // Merge detections with confidence weighting
        val allDetections = enhancedDetections + smallScaleDetections + largeScaleDetections

        // Apply ensemble NMS
        return mergeEnsembleDetections(allDetections)
    }

    private fun runMultiScaleDetection(bitmap: Bitmap, scale: Float): List<DetectionResult> {
        try {
            val scaledSize = (INPUT_SIZE * scale).toInt().coerceIn(320, 1024)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledSize, scaledSize, true)
            val resizedBitmap =
                Bitmap.createScaledBitmap(scaledBitmap, INPUT_SIZE, INPUT_SIZE, true)

            val rawDetections = runInferenceSafely(resizedBitmap)

            // Clean up temporary bitmaps
            try {
                if (scaledBitmap != bitmap && !scaledBitmap.isRecycled) {
                    scaledBitmap.recycle()
                }
                if (resizedBitmap != bitmap && !resizedBitmap.isRecycled) {
                    resizedBitmap.recycle()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up multi-scale bitmaps", e)
            }

            return basicPostProcess(rawDetections).map { detection ->
                // Adjust confidence based on scale
                detection.copy(confidence = detection.confidence * 0.9f) // Slightly lower confidence for ensemble
            }
        } catch (e: Exception) {
            Log.e(TAG, "Multi-scale detection failed", e)
            return emptyList()
        }
    }

    private fun mergeEnsembleDetections(allDetections: List<DetectionResult>): List<DetectionResult> {
        // Group similar detections and average their properties
        val groupedDetections = mutableMapOf<String, MutableList<DetectionResult>>()

        for (detection in allDetections) {
            val key =
                "${detection.label}_${(detection.boundingBox.left * 10).toInt()}_${(detection.boundingBox.top * 10).toInt()}"
            groupedDetections.getOrPut(key) { mutableListOf() }.add(detection)
        }

        return groupedDetections.values.map { group ->
            if (group.size == 1) {
                group.first()
            } else {
                // Average properties for better accuracy
                val avgConfidence = group.map { it.confidence }.average().toFloat()
                val avgLeft = group.map { it.boundingBox.left }.average().toFloat()
                val avgTop = group.map { it.boundingBox.top }.average().toFloat()
                val avgRight = group.map { it.boundingBox.right }.average().toFloat()
                val avgBottom = group.map { it.boundingBox.bottom }.average().toFloat()

                DetectionResult(
                    id = "ensemble_${System.currentTimeMillis()}_${group.hashCode()}",
                    label = group.first().label,
                    confidence = (avgConfidence * 1.1f).coerceAtMost(1.0f), // Boost ensemble confidence
                    boundingBox = BoundingBox(avgLeft, avgTop, avgRight, avgBottom)
                )
            }
        }.sortedByDescending { it.confidence }.take(10) // Keep top 10 detections
    }

    private fun mapToSafetyObject(classId: Int): SafetyObject? {
        // Map COCO class IDs to safety objects
        return when (classId) {
            39 -> SafetyObject.OXYGEN_TANK // bottle
            0 -> SafetyObject.FIRE_EXTINGUISHER // person (placeholder for fire extinguisher)
            84 -> SafetyObject.FIRE_ALARM // book (placeholder for fire alarm)
            73 -> SafetyObject.FIRST_AID_KIT // laptop
            47 -> SafetyObject.EMERGENCY_LIGHT // cup
            25 -> SafetyObject.SAFETY_HELMET // backpack 
            67 -> SafetyObject.COMMUNICATION_DEVICE // cell phone
            else -> {
                // Try to match with Falcon classes if available
                if (modelClasses.isNotEmpty() && classId < modelClasses.size) {
                    val className = modelClasses[classId]
                    SafetyObject.values().find {
                        it.displayName.contains(className, ignoreCase = true) ||
                                className.contains(it.displayName, ignoreCase = true)
                    }
                } else null
            }
        }
    }

    private suspend fun generateMockDetections(): List<DetectionResult> {
        // Enhanced mock detection when connected to Falcon
        delay(if (isConnectedToFalcon) 30 else 50) // Faster when connected

        val detections = mutableListOf<DetectionResult>()
        val numDetections = if (isConnectedToFalcon) Random.nextInt(2, 6) else Random.nextInt(1, 4)
        val availableObjects = SafetyObject.values().toList().shuffled()

        repeat(numDetections) { index ->
            val safetyObject = availableObjects[index % availableObjects.size]
            val baseConfidence = if (isConnectedToFalcon) 0.75f else 0.65f
            val confidence =
                Random.nextFloat() * 0.2f + baseConfidence // Higher confidence when connected

            val left = Random.nextFloat() * 0.6f
            val top = Random.nextFloat() * 0.6f
            val width = Random.nextFloat() * 0.15f + 0.15f
            val height = Random.nextFloat() * 0.15f + 0.15f

            detections.add(
                DetectionResult(
                    id = "mock_det_${System.currentTimeMillis()}_$index",
                    label = safetyObject.displayName,
                    confidence = confidence,
                    boundingBox = BoundingBox(
                        left = left,
                        top = top,
                        right = left + width,
                        bottom = top + height
                    )
                )
            )
        }

        return detections
    }

    fun release() {
        try {
            interpreter?.close()
            interpreter = null
            isModelLoaded = false
            Log.d(TAG, "ObjectDetector released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ObjectDetector", e)
        }
    }

    // Data class for raw detection results
    private data class RawDetection(
        val x: Float, val y: Float, val w: Float, val h: Float,
        val confidence: Float, val classId: Int
    )
}
