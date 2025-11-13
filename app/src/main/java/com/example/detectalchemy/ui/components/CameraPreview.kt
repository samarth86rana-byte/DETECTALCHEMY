package com.example.detectalchemy.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

@Composable
fun CameraPreview(
    onFrameAnalyzed: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Remember executor to ensure proper cleanup
    val executor = remember { Executors.newSingleThreadExecutor() }

    // Cleanup executor when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            try {
                executor.shutdown()
                Log.d("CameraPreview", "Executor shutdown")
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error shutting down executor", e)
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(
                            android.util.Size(
                                640,
                                480
                            )
                        ) // Limit resolution for memory
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(executor) { imageProxy ->
                                // Process in coroutine to avoid blocking
                                scope.launch(Dispatchers.Default) {
                                    processImageProxySafely(imageProxy, onFrameAnalyzed)
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        Log.d("CameraPreview", "Camera bound successfully")
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Camera binding failed", e)
                    }
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera provider setup failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

private fun processImageProxySafely(imageProxy: ImageProxy, onFrameAnalyzed: (Bitmap) -> Unit) {
    var bitmap: Bitmap? = null
    var rotatedBitmap: Bitmap? = null

    try {
        // Convert to bitmap with error handling
        bitmap = imageProxy.toBitmap()

        // Check if bitmap is valid
        if (bitmap.isRecycled) {
            Log.w("CameraPreview", "Bitmap is recycled, skipping frame")
            return
        }

        // Rotate bitmap if needed
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees.toFloat()
        rotatedBitmap = if (rotationDegrees != 0f) {
            rotateBitmapSafely(bitmap, rotationDegrees)
        } else {
            bitmap
        }

        // Only process if bitmap is valid and not too large
        if (rotatedBitmap != null && !rotatedBitmap.isRecycled) {
            // Check memory usage
            val bitmapSize = rotatedBitmap.allocationByteCount
            if (bitmapSize > 10 * 1024 * 1024) { // 10MB limit
                Log.w("CameraPreview", "Bitmap too large: ${bitmapSize / 1024 / 1024}MB, skipping")
                return
            }

            onFrameAnalyzed(rotatedBitmap)
        }

    } catch (e: OutOfMemoryError) {
        Log.e("CameraPreview", "Out of memory processing frame", e)
        System.gc() // Suggest garbage collection
    } catch (e: Exception) {
        Log.e("CameraPreview", "Error processing frame", e)
    } finally {
        // Always close the image proxy
        imageProxy.close()

        // Clean up bitmaps to prevent memory leaks
        try {
            if (rotatedBitmap != null && rotatedBitmap != bitmap && !rotatedBitmap.isRecycled) {
                rotatedBitmap.recycle()
            }
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error recycling bitmaps", e)
        }
    }
}

private fun rotateBitmapSafely(bitmap: Bitmap, rotationDegrees: Float): Bitmap? {
    if (rotationDegrees == 0f || bitmap.isRecycled) return bitmap

    var rotatedBitmap: Bitmap? = null

    try {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees)
        }

        rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        return rotatedBitmap

    } catch (e: OutOfMemoryError) {
        Log.e("CameraPreview", "Out of memory rotating bitmap", e)
        System.gc()
        return bitmap // Return original if rotation fails
    } catch (e: Exception) {
        Log.e("CameraPreview", "Error rotating bitmap", e)
        return bitmap // Return original if rotation fails
    }
}
