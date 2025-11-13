package com.example.detectalchemy.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object FalconPreferences {
    private const val PREFS_NAME = "falcon_prefs"
    private const val KEY_FALCON_URL = "falcon_url"
    private const val KEY_FALCON_API_KEY = "falcon_api_key"
    private const val KEY_IS_CONNECTED = "is_connected"
    private const val KEY_LAST_SYNC = "last_sync_time"
    private const val KEY_CONNECTION_TYPE = "connection_type" // "URL" or "API_KEY"
    private const val KEY_DATASET_ID = "dataset_id"
    private const val KEY_DATASET_NAME = "dataset_name"
    private const val KEY_TOTAL_IMAGES = "total_images"
    private const val KEY_SYNC_STATUS = "sync_status"

    enum class ConnectionType {
        URL, API_KEY
    }

    enum class SyncStatus {
        NOT_SYNCED, SYNCING, SYNCED, FAILED
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save Falcon URL (existing functionality)
    fun saveFalconUrl(context: Context, url: String) {
        getPrefs(context).edit().apply {
            putString(KEY_FALCON_URL, url)
            putString(KEY_CONNECTION_TYPE, ConnectionType.URL.name)
            putBoolean(KEY_IS_CONNECTED, true)
            putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            putString(KEY_SYNC_STATUS, SyncStatus.NOT_SYNCED.name)
            apply()
        }
    }

    // Save Falcon API Key (new functionality)
    fun saveFalconApiKey(context: Context, apiKey: String, datasetId: String? = null) {
        getPrefs(context).edit().apply {
            // Store API key securely (simple encoding - for production use proper encryption)
            val encodedApiKey = Base64.encodeToString(apiKey.toByteArray(), Base64.DEFAULT)
            putString(KEY_FALCON_API_KEY, encodedApiKey)
            putString(KEY_CONNECTION_TYPE, ConnectionType.API_KEY.name)
            putBoolean(KEY_IS_CONNECTED, true)
            putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            putString(KEY_SYNC_STATUS, SyncStatus.NOT_SYNCED.name)

            // Store dataset ID if provided
            datasetId?.let { putString(KEY_DATASET_ID, it) }

            apply()
        }
    }

    fun getFalconUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_FALCON_URL, null)
    }

    fun getFalconApiKey(context: Context): String? {
        val encodedApiKey = getPrefs(context).getString(KEY_FALCON_API_KEY, null)
        return if (encodedApiKey != null) {
            try {
                String(Base64.decode(encodedApiKey, Base64.DEFAULT))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun getConnectionType(context: Context): ConnectionType {
        val typeString = getPrefs(context).getString(KEY_CONNECTION_TYPE, ConnectionType.URL.name)
        return try {
            ConnectionType.valueOf(typeString ?: ConnectionType.URL.name)
        } catch (e: Exception) {
            ConnectionType.URL
        }
    }

    fun isConnected(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_CONNECTED, false)
    }

    fun disconnect(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_FALCON_URL)
            remove(KEY_FALCON_API_KEY)
            remove(KEY_DATASET_ID)
            remove(KEY_DATASET_NAME)
            remove(KEY_TOTAL_IMAGES)
            putBoolean(KEY_IS_CONNECTED, false)
            putString(KEY_SYNC_STATUS, SyncStatus.NOT_SYNCED.name)
            apply()
        }
    }

    fun getLastSyncTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0L)
    }

    fun getDatasetId(context: Context): String? {
        return getPrefs(context).getString(KEY_DATASET_ID, null)
    }

    fun saveDatasetInfo(context: Context, datasetName: String, totalImages: Int) {
        getPrefs(context).edit().apply {
            putString(KEY_DATASET_NAME, datasetName)
            putInt(KEY_TOTAL_IMAGES, totalImages)
            apply()
        }
    }

    fun getDatasetName(context: Context): String? {
        return getPrefs(context).getString(KEY_DATASET_NAME, null)
    }

    fun getTotalImages(context: Context): Int {
        return getPrefs(context).getInt(KEY_TOTAL_IMAGES, 0)
    }

    fun setSyncStatus(context: Context, status: SyncStatus) {
        getPrefs(context).edit().apply {
            putString(KEY_SYNC_STATUS, status.name)
            if (status == SyncStatus.SYNCED) {
                putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            }
            apply()
        }
    }

    fun getSyncStatus(context: Context): SyncStatus {
        val statusString = getPrefs(context).getString(KEY_SYNC_STATUS, SyncStatus.NOT_SYNCED.name)
        return try {
            SyncStatus.valueOf(statusString ?: SyncStatus.NOT_SYNCED.name)
        } catch (e: Exception) {
            SyncStatus.NOT_SYNCED
        }
    }
}
