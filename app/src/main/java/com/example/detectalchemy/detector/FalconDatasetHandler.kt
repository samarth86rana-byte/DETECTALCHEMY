package com.example.detectalchemy.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.detectalchemy.data.DetectionClass
import com.example.detectalchemy.data.FalconPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class FalconDatasetHandler(private val context: Context) {

    companion object {
        private const val TAG = "FalconDatasetHandler"
        private const val TIMEOUT_MS = 30000

        // API endpoints for Falcon
        private const val FALCON_API_BASE = "https://api.falcon.ai"
        private const val DATASET_ENDPOINT = "/datasets"
        private const val IMAGES_ENDPOINT = "/images"
        private const val CLASSES_ENDPOINT = "/classes"
    }

    private val cacheDir = File(context.cacheDir, "falcon_dataset")
    private val imagesDir = File(cacheDir, "images")
    private val modelDir = File(cacheDir, "models")
    private val annotationsDir = File(cacheDir, "annotations")

    init {
        // Create necessary directories
        cacheDir.mkdirs()
        imagesDir.mkdirs()
        modelDir.mkdirs()
        annotationsDir.mkdirs()
    }

    /**
     * Sync dataset using API Key
     */
    suspend fun syncDatasetWithApiKey(apiKey: String, datasetId: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Starting dataset sync with API key")
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.SYNCING)

                // Step 1: Get available datasets
                val datasets = getAvailableDatasets(apiKey)
                if (datasets.isEmpty()) {
                    Log.e(TAG, "No datasets found")
                    FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                    return@withContext false
                }

                // Step 2: Select dataset (use provided ID or first available)
                val selectedDataset = if (datasetId != null) {
                    datasets.find { it.getString("id") == datasetId }
                } else {
                    datasets.first()
                } ?: run {
                    Log.e(TAG, "Dataset not found: $datasetId")
                    FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                    return@withContext false
                }

                val finalDatasetId = selectedDataset.getString("id")
                val datasetName = selectedDataset.getString("name")

                Log.i(TAG, "Selected dataset: $datasetName (ID: $finalDatasetId)")

                // Step 3: Get dataset classes/categories
                val classes = getDatasetClasses(apiKey, finalDatasetId)
                saveDetectionClasses(classes)

                // Step 4: Download dataset images
                val images = getDatasetImages(apiKey, finalDatasetId)
                val downloadedCount = downloadImages(apiKey, images)

                // Step 5: Download annotations if available
                downloadAnnotations(apiKey, finalDatasetId)

                // Step 6: Download or create model if available
                downloadModel(apiKey, finalDatasetId)

                // Step 7: Save dataset info
                FalconPreferences.saveDatasetInfo(context, datasetName, downloadedCount)
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.SYNCED)

                Log.i(
                    TAG,
                    "Dataset sync completed successfully. Downloaded $downloadedCount images"
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing dataset with API key", e)
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                false
            }
        }
    }

    /**
     * Get available datasets from Falcon API
     */
    private suspend fun getAvailableDatasets(apiKey: String): List<JSONObject> {
        return withContext(Dispatchers.IO) {
            val datasets: MutableList<JSONObject> = mutableListOf()

            try {
                val url = URL("$FALCON_API_BASE$DATASET_ENDPOINT")
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.has("datasets")) {
                        val datasetsArray = jsonResponse.getJSONArray("datasets")
                        for (i in 0 until datasetsArray.length()) {
                            datasets.add(datasetsArray.getJSONObject(i))
                        }
                    }
                } else {
                    Log.e(TAG, "API Error: $responseCode - ${connection.responseMessage}")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting datasets", e)
            }

            datasets
        }
    }

    /**
     * Get dataset classes/categories
     */
    private suspend fun getDatasetClasses(apiKey: String, datasetId: String): List<DetectionClass> {
        return withContext(Dispatchers.IO) {
            val classes = mutableListOf<DetectionClass>()

            try {
                val url = URL("$FALCON_API_BASE$DATASET_ENDPOINT/$datasetId$CLASSES_ENDPOINT")
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.has("classes")) {
                        val classesArray = jsonResponse.getJSONArray("classes")
                        for (i in 0 until classesArray.length()) {
                            val classObj = classesArray.getJSONObject(i)
                            classes.add(
                                DetectionClass(
                                    id = classObj.getInt("id"),
                                    name = classObj.getString("name"),
                                    displayName = classObj.optString(
                                        "display_name",
                                        classObj.getString("name")
                                    ),
                                    color = classObj.optString("color", "#FF0000")
                                )
                            )
                        }
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting dataset classes", e)
            }

            classes
        }
    }

    /**
     * Get dataset images metadata
     */
    private suspend fun getDatasetImages(apiKey: String, datasetId: String): List<JSONObject> {
        return withContext(Dispatchers.IO) {
            val images = mutableListOf<JSONObject>()

            try {
                val url = URL("$FALCON_API_BASE$DATASET_ENDPOINT/$datasetId$IMAGES_ENDPOINT")
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.has("images")) {
                        val imagesArray = jsonResponse.getJSONArray("images")
                        for (i in 0 until imagesArray.length()) {
                            images.add(imagesArray.getJSONObject(i))
                        }
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting dataset images", e)
            }

            images
        }
    }

    /**
     * Download images from the dataset
     */
    private suspend fun downloadImages(apiKey: String, images: List<JSONObject>): Int {
        return withContext(Dispatchers.IO) {
            var downloadedCount = 0

            for (imageData in images) {
                try {
                    val imageId = imageData.getString("id")
                    val imageUrl = imageData.getString("url")
                    val filename = imageData.optString("filename", "$imageId.jpg")

                    val localFile = File(imagesDir, filename)
                    if (localFile.exists()) {
                        downloadedCount++
                        continue // Skip if already downloaded
                    }

                    // Download image
                    val connection = URL(imageUrl).openConnection() as HttpURLConnection
                    connection.setRequestProperty("Authorization", "Bearer $apiKey")
                    connection.connectTimeout = TIMEOUT_MS
                    connection.readTimeout = TIMEOUT_MS

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.use { input ->
                            FileOutputStream(localFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        downloadedCount++
                        Log.d(TAG, "Downloaded image: $filename")
                    }

                    connection.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Error downloading image", e)
                }
            }

            downloadedCount
        }
    }

    /**
     * Download annotations for the dataset
     */
    private suspend fun downloadAnnotations(apiKey: String, datasetId: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$FALCON_API_BASE$DATASET_ENDPOINT/$datasetId/annotations")
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val annotationsFile = File(annotationsDir, "annotations.json")
                    connection.inputStream.use { input ->
                        FileOutputStream(annotationsFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.i(TAG, "Downloaded annotations")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading annotations", e)
            }
        }
    }

    /**
     * Download trained model if available
     */
    private suspend fun downloadModel(apiKey: String, datasetId: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$FALCON_API_BASE$DATASET_ENDPOINT/$datasetId/model")
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val modelFile = File(modelDir, "falcon_model.tflite")
                    connection.inputStream.use { input ->
                        FileOutputStream(modelFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.i(TAG, "Downloaded trained model")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading model", e)
            }
        }
    }

    /**
     * Legacy URL sync method (enhanced)
     */
    suspend fun syncDataset(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.SYNCING)

                when {
                    url.endsWith(".tflite") -> downloadTensorFlowLiteModel(url)
                    url.endsWith(".zip") -> downloadAndExtractZip(url)
                    url.contains("/api/") -> {
                        // Try to extract API key from URL if present
                        val apiKey = extractApiKeyFromUrl(url)
                        if (apiKey != null) {
                            syncDatasetWithApiKey(apiKey)
                        } else {
                            downloadJson(url)
                        }
                    }
                    else -> downloadJson(url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing dataset from URL", e)
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                false
            }
        }
    }

    /**
     * Extract API key from URL if present
     */
    private fun extractApiKeyFromUrl(url: String): String? {
        return try {
            val uri = java.net.URI(url)
            val query = uri.query ?: return null
            val params = query.split("&").associate {
                val (key, value) = it.split("=", limit = 2)
                key to value
            }
            params["api_key"] ?: params["key"] ?: params["token"]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get synced dataset images for training/validation
     */
    fun getDatasetImages(): List<File> {
        return if (imagesDir.exists()) {
            imagesDir.listFiles()?.filter {
                it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png", "bmp")
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Get dataset image as bitmap
     */
    fun getImageBitmap(imageFile: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(imageFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image bitmap", e)
            null
        }
    }

    /**
     * Get detection classes from synced dataset
     */
    fun getDetectionClasses(): List<DetectionClass> {
        val classesFile = File(cacheDir, "classes.json")
        if (!classesFile.exists()) return emptyList()

        return try {
            val jsonContent = classesFile.readText()
            val jsonArray = JSONArray(jsonContent)
            val classes = mutableListOf<DetectionClass>()

            for (i in 0 until jsonArray.length()) {
                val classObj = jsonArray.getJSONObject(i)
                classes.add(
                    DetectionClass(
                        id = classObj.getInt("id"),
                        name = classObj.getString("name"),
                        displayName = classObj.optString(
                            "display_name",
                            classObj.getString("name")
                        ),
                        color = classObj.optString("color", "#FF0000")
                    )
                )
            }

            classes
        } catch (e: Exception) {
            Log.e(TAG, "Error loading detection classes", e)
            emptyList()
        }
    }

    /**
     * Save detection classes to local storage
     */
    private fun saveDetectionClasses(classes: List<DetectionClass>) {
        try {
            val classesFile = File(cacheDir, "classes.json")
            val jsonArray = JSONArray()

            classes.forEach { detectionClass ->
                val classObj = JSONObject().apply {
                    put("id", detectionClass.id)
                    put("name", detectionClass.name)
                    put("display_name", detectionClass.displayName)
                    put("color", detectionClass.color)
                }
                jsonArray.put(classObj)
            }

            classesFile.writeText(jsonArray.toString())
            Log.i(TAG, "Saved ${classes.size} detection classes")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving detection classes", e)
        }
    }

    /**
     * Get trained model file
     */
    fun getModelFile(): File? {
        val modelFile = File(modelDir, "falcon_model.tflite")
        return if (modelFile.exists()) modelFile else null
    }

    /**
     * Get annotations file
     */
    fun getAnnotationsFile(): File? {
        val annotationsFile = File(annotationsDir, "annotations.json")
        return if (annotationsFile.exists()) annotationsFile else null
    }

    /**
     * Clear cached dataset
     */
    fun clearDataset() {
        try {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            imagesDir.mkdirs()
            modelDir.mkdirs()
            annotationsDir.mkdirs()
            Log.i(TAG, "Dataset cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing dataset", e)
        }
    }

    /**
     * Get dataset sync status info
     */
    fun getDatasetInfo(): Map<String, Any> {
        val images = getDatasetImages()
        val classes = getDetectionClasses()
        val modelFile = getModelFile()

        return mapOf(
            "totalImages" to images.size,
            "totalClasses" to classes.size,
            "hasModel" to (modelFile != null),
            "datasetName" to (FalconPreferences.getDatasetName(context) ?: "Unknown"),
            "syncStatus" to FalconPreferences.getSyncStatus(context).name,
            "lastSync" to FalconPreferences.getLastSyncTime(context)
        )
    }

    // Legacy methods for backward compatibility
    private suspend fun downloadTensorFlowLiteModel(url: String): Boolean {
        // Implementation for downloading .tflite files
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val modelFile = File(modelDir, "model.tflite")
                connection.inputStream.use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.SYNCED)
                true
            } else {
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading TFLite model", e)
            FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
            false
        }
    }

    private suspend fun downloadAndExtractZip(url: String): Boolean {
        // Implementation for downloading and extracting ZIP files
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                ZipInputStream(connection.inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        val file = File(cacheDir, entry.name)
                        if (!entry.isDirectory) {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { output ->
                                zipStream.copyTo(output)
                            }
                        }
                        entry = zipStream.nextEntry
                    }
                }
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.SYNCED)
                true
            } else {
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading ZIP file", e)
            FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
            false
        }
    }

    private suspend fun downloadJson(url: String): Boolean {
        // Implementation for downloading JSON data
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val dataFile = File(cacheDir, "dataset.json")
                dataFile.writeText(response)
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.SYNCED)
                true
            } else {
                FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading JSON", e)
            FalconPreferences.setSyncStatus(context, FalconPreferences.SyncStatus.FAILED)
            false
        }
    }
}
