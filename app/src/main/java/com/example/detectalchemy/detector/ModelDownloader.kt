package com.example.detectalchemy.detector

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloader(private val context: Context) {

    suspend fun downloadModel(url: String, onProgress: (Int) -> Unit = {}): File? =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream

                // Save to app's files directory
                val modelFile = File(context.filesDir, "yolo_model_falcon.tflite")
                val outputStream = FileOutputStream(modelFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (fileLength > 0) {
                        val progress = (totalBytesRead * 100 / fileLength).toInt()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }

                outputStream.close()
                inputStream.close()
                connection.disconnect()

                modelFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun getLocalModel(): File? {
        val modelFile = File(context.filesDir, "yolo_model_falcon.tflite")
        return if (modelFile.exists()) modelFile else null
    }

    fun deleteLocalModel() {
        val modelFile = File(context.filesDir, "yolo_model_falcon.tflite")
        if (modelFile.exists()) {
            modelFile.delete()
        }
    }
}
