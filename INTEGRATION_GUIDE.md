# 🔧 Integration Guide for DETECTALCHEMY

This guide will help you integrate your trained YOLO model and connect to Falcon API for model
updates.

---

## 📦 Part 1: Integrating Your YOLO Model

### Step 1: Prepare Your Model

1. **Export your YOLO model to TensorFlow Lite format**
   ```bash
   # Using YOLOv5 as example
   python export.py --weights your_model.pt --include tflite
   
   # Or for YOLOv8
   yolo export model=your_model.pt format=tflite
   ```

2. **Optimize the model (optional but recommended)**
   ```bash
   # Apply quantization for faster inference
   python -m tensorflow.lite.python.optimize \
     --input_model=model.tflite \
     --output_model=model_optimized.tflite \
     --quantize
   ```

### Step 2: Add Model to Project

1. Create assets folder if it doesn't exist:
   ```
   app/src/main/assets/
   ```

2. Copy your `.tflite` model file:
   ```
   app/src/main/assets/safety_detector.tflite
   ```

3. Also include the labels file:
   ```
   app/src/main/assets/labels.txt
   ```

   Example labels.txt:
   ```
   Oxygen Tank
   Fire Extinguisher
   Fire Alarm
   First Aid Kit
   Emergency Light
   Safety Helmet
   Communication Device
   ```

### Step 3: Update ObjectDetector.kt

Replace the mock implementation in
`app/src/main/java/com/example/detectalchemy/detector/ObjectDetector.kt`:

```kotlin
package com.example.detectalchemy.detector

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import com.example.detectalchemy.data.BoundingBox
import com.example.detectalchemy.data.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class ObjectDetector(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private val detectionThreshold = 0.5f
    private val inputSize = 640 // Adjust based on your model
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Load model
            val modelBuffer = FileUtil.loadMappedFile(context, "safety_detector.tflite")
            interpreter = Interpreter(modelBuffer)
            
            // Load labels
            labels = FileUtil.loadLabels(context, "labels.txt")
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun detectObjects(bitmap: Bitmap): List<DetectionResult> = withContext(Dispatchers.Default) {
        val interpreter = interpreter ?: return@withContext emptyList()
        
        try {
            // Preprocess image
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
                .build()
            
            var tensorImage = TensorImage.fromBitmap(bitmap)
            tensorImage = imageProcessor.process(tensorImage)
            
            // Prepare output buffer based on your model's output shape
            // Adjust these based on your YOLO model
            val outputLocations = Array(1) { Array(MAX_DETECTIONS) { FloatArray(4) } }
            val outputClasses = Array(1) { FloatArray(MAX_DETECTIONS) }
            val outputScores = Array(1) { FloatArray(MAX_DETECTIONS) }
            val numDetections = FloatArray(1)
            
            // Run inference
            val outputs = mapOf(
                0 to outputLocations,
                1 to outputClasses,
                2 to outputScores,
                3 to numDetections
            )
            
            interpreter.runForMultipleInputsOutputs(arrayOf(tensorImage.buffer), outputs)
            
            // Parse results
            val detections = mutableListOf<DetectionResult>()
            val numDet = numDetections[0].toInt().coerceAtMost(MAX_DETECTIONS)
            
            for (i in 0 until numDet) {
                val score = outputScores[0][i]
                if (score >= detectionThreshold) {
                    val classIndex = outputClasses[0][i].toInt()
                    val label = if (classIndex < labels.size) labels[classIndex] else "Unknown"
                    
                    val location = outputLocations[0][i]
                    val boundingBox = BoundingBox(
                        top = location[0],
                        left = location[1],
                        bottom = location[2],
                        right = location[3]
                    )
                    
                    detections.add(
                        DetectionResult(
                            id = "det_${System.currentTimeMillis()}_$i",
                            label = label,
                            confidence = score,
                            boundingBox = boundingBox
                        )
                    )
                }
            }
            
            detections
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun release() {
        interpreter?.close()
        interpreter = null
    }
    
    companion object {
        private const val MAX_DETECTIONS = 10
    }
}
```

### Step 4: Update ViewModel Initialization

In `DetectionViewModel.kt`, update the detector initialization:

```kotlin
class DetectionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val objectDetector = ObjectDetector(application.applicationContext)
    
    // ... rest of the code
}
```

And update MainActivity to pass application context:

```kotlin
val viewModel: DetectionViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetectionViewModel(application) as T
        }
    }
)
```

---

## 🔄 Part 2: Connecting to Falcon API

### Step 1: Add Networking Dependencies

Update `gradle/libs.versions.toml`:

```toml
[versions]
retrofit = "2.9.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.6.0"

[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
```

Update `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}
```

### Step 2: Add Internet Permission

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 3: Create Falcon API Service

Create `app/src/main/java/com/example/detectalchemy/network/FalconApiService.kt`:

```kotlin
package com.example.detectalchemy.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class ModelVersion(
    val version: String,
    val download_url: String,
    val created_at: String,
    val accuracy: Float,
    val map_50: Float
)

data class ModelListResponse(
    val models: List<ModelVersion>,
    val latest: ModelVersion
)

interface FalconApiService {
    
    @GET("models/safety-detector/versions")
    suspend fun getModelVersions(): Response<ModelListResponse>
    
    @GET
    @Streaming
    suspend fun downloadModel(@Url url: String): Response<ResponseBody>
    
    @POST("models/safety-detector/feedback")
    suspend fun sendFeedback(
        @Body feedback: DetectionFeedback
    ): Response<Unit>
    
    companion object {
        private const val BASE_URL = "https://your-falcon-api.com/api/v1/"
        
        fun create(): FalconApiService {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FalconApiService::class.java)
        }
    }
}

data class DetectionFeedback(
    val image_id: String,
    val detections: List<DetectionResult>,
    val user_corrections: List<UserCorrection>
)

data class UserCorrection(
    val label: String,
    val bounding_box: BoundingBox,
    val action: String // "add", "remove", "modify"
)
```

### Step 4: Create Model Manager

Create `app/src/main/java/com/example/detectalchemy/model/ModelManager.kt`:

```kotlin
package com.example.detectalchemy.model

import android.content.Context
import com.example.detectalchemy.network.FalconApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ModelManager(
    private val context: Context,
    private val apiService: FalconApiService
) {
    
    private val _updateStatus = MutableStateFlow<ModelUpdateStatus>(ModelUpdateStatus.Idle)
    val updateStatus: StateFlow<ModelUpdateStatus> = _updateStatus
    
    private val _currentVersion = MutableStateFlow("1.0.0")
    val currentVersion: StateFlow<String> = _currentVersion
    
    suspend fun checkForUpdates(): Boolean = withContext(Dispatchers.IO) {
        try {
            _updateStatus.value = ModelUpdateStatus.Checking
            
            val response = apiService.getModelVersions()
            if (response.isSuccessful) {
                val latest = response.body()?.latest
                if (latest != null && latest.version != _currentVersion.value) {
                    _updateStatus.value = ModelUpdateStatus.UpdateAvailable(latest.version)
                    return@withContext true
                }
            }
            
            _updateStatus.value = ModelUpdateStatus.UpToDate
            false
        } catch (e: Exception) {
            _updateStatus.value = ModelUpdateStatus.Error(e.message ?: "Unknown error")
            false
        }
    }
    
    suspend fun downloadAndInstallUpdate(downloadUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _updateStatus.value = ModelUpdateStatus.Downloading(0)
            
            val response = apiService.downloadModel(downloadUrl)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val modelFile = File(context.filesDir, "safety_detector_new.tflite")
                    
                    body.byteStream().use { input ->
                        FileOutputStream(modelFile).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            val contentLength = body.contentLength()
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                
                                val progress = (totalBytesRead * 100 / contentLength).toInt()
                                _updateStatus.value = ModelUpdateStatus.Downloading(progress)
                            }
                        }
                    }
                    
                    // Move new model to active location
                    val activeModel = File(context.getExternalFilesDir(null), "safety_detector.tflite")
                    modelFile.copyTo(activeModel, overwrite = true)
                    modelFile.delete()
                    
                    _updateStatus.value = ModelUpdateStatus.Installed
                    return@withContext true
                }
            }
            
            _updateStatus.value = ModelUpdateStatus.Error("Download failed")
            false
        } catch (e: Exception) {
            _updateStatus.value = ModelUpdateStatus.Error(e.message ?: "Unknown error")
            false
        }
    }
}

sealed class ModelUpdateStatus {
    object Idle : ModelUpdateStatus()
    object Checking : ModelUpdateStatus()
    object UpToDate : ModelUpdateStatus()
    data class UpdateAvailable(val version: String) : ModelUpdateStatus()
    data class Downloading(val progress: Int) : ModelUpdateStatus()
    object Installed : ModelUpdateStatus()
    data class Error(val message: String) : ModelUpdateStatus()
}
```

### Step 5: Add Update UI to DetectionScreen

Add a floating action button for model updates:

```kotlin
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    modelManager: ModelManager,
    modifier: Modifier = Modifier
) {
    // ... existing code
    
    val updateStatus by modelManager.updateStatus.collectAsStateWithLifecycle()
    
    Box(modifier = modifier.fillMaxSize()) {
        // ... existing UI layers
        
        // Model Update FAB
        FloatingActionButton(
            onClick = {
                viewModel.viewModelScope.launch {
                    if (modelManager.checkForUpdates()) {
                        // Show update dialog
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            when (updateStatus) {
                is ModelUpdateStatus.Checking -> CircularProgressIndicator()
                is ModelUpdateStatus.Downloading -> {
                    val progress = (updateStatus as ModelUpdateStatus.Downloading).progress
                    Text("$progress%")
                }
                else -> Icon(Icons.Default.CloudDownload, "Update Model")
            }
        }
    }
}
```

---

## 🧪 Part 3: Testing Your Integration

### Test Checklist

- [ ] Model loads successfully on app startup
- [ ] Real-time detection works smoothly (30+ FPS)
- [ ] Bounding boxes align correctly with objects
- [ ] Confidence scores are reasonable (>70% for clear objects)
- [ ] All safety object types are detected
- [ ] Falcon API connection succeeds
- [ ] Model update downloads and installs correctly
- [ ] App handles offline mode gracefully

### Debugging Tips

1. **Enable TensorFlow Lite logging**:
   ```kotlin
   Interpreter.Options().apply {
       setNumThreads(4)
       setUseNNAPI(true) // For hardware acceleration
   }
   ```

2. **Log inference times**:
   ```kotlin
   val startTime = System.currentTimeMillis()
   val results = detectObjects(bitmap)
   val inferenceTime = System.currentTimeMillis() - startTime
   Log.d("ObjectDetector", "Inference time: ${inferenceTime}ms")
   ```

3. **Visualize intermediate outputs**:
    - Save preprocessed images to debug preprocessing
    - Log raw model outputs to verify parsing

---

## 📈 Part 4: Performance Optimization

### Inference Optimization

1. **Use GPU Acceleration**:
   ```kotlin
   implementation(libs.tensorflow.lite.gpu)
   
   val options = Interpreter.Options().apply {
       addDelegate(GpuDelegate())
   }
   ```

2. **Quantization**:
    - Use INT8 quantized models for 4x faster inference
    - Trade-off: ~1-2% accuracy loss

3. **Batch Processing**:
    - Process multiple frames in parallel if needed
    - Use coroutines for async processing

### Network Optimization

1. **Cache Model Files**:
   ```kotlin
   val cache = File(context.cacheDir, "model_cache")
   OkHttpClient.Builder()
       .cache(Cache(cache, 50 * 1024 * 1024)) // 50 MB cache
   ```

2. **Compress API Responses**:
   ```kotlin
   .addInterceptor { chain ->
       chain.proceed(chain.request().newBuilder()
           .header("Accept-Encoding", "gzip")
           .build())
   }
   ```

---

## 🎯 Next Steps

1. **Collect Real Data**: Use the app to collect edge case detections
2. **Fine-tune Model**: Retrain on Falcon with collected data
3. **A/B Testing**: Compare multiple model versions
4. **Analytics**: Track detection accuracy in production
5. **Continuous Integration**: Automate model deployment pipeline

---

## 📞 Support

If you encounter issues:

- Check the Android Logcat for errors
- Verify model input/output shapes match your implementation
- Test with a known-working TFLite model first
- Reach out to Falcon support for API issues

Good luck with your integration! 🚀
