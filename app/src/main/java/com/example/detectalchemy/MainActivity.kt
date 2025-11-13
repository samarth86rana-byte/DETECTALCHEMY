package com.example.detectalchemy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.detectalchemy.ui.screens.DetectionScreen
import com.example.detectalchemy.ui.screens.HomeScreen
import com.example.detectalchemy.ui.screens.InventoryScreen
import com.example.detectalchemy.ui.screens.SafetyReportsScreen
import com.example.detectalchemy.ui.screens.AnalyticsScreen
import com.example.detectalchemy.ui.screens.SettingsScreen
import com.example.detectalchemy.ui.screens.AboutScreen
import com.example.detectalchemy.ui.theme.DETECTALCHEMYTheme
import com.example.detectalchemy.viewmodel.DetectionViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var viewModel: DetectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "Creating MainActivity")

            enableEdgeToEdge()

            // Initialize ViewModel with proper lifecycle management
            viewModel = ViewModelProvider(this)[DetectionViewModel::class.java]

            setContent {
                DETECTALCHEMYTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(viewModel)
                    }
                }
            }

            Log.d(TAG, "MainActivity created successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error creating MainActivity", e)
            // Try to continue with basic setup
            try {
                setContent {
                    DETECTALCHEMYTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error initializing app. Please restart.",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Critical error in MainActivity", fallbackError)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")

        // Monitor memory usage
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryPercent = (usedMemory * 100 / maxMemory).toInt()

        Log.d(
            TAG,
            "Memory usage: ${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB ($memoryPercent%)"
        )

        if (memoryPercent > 80) {
            Log.w(TAG, "High memory usage detected, suggesting GC")
            System.gc()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity destroyed")

        // Additional cleanup if needed
        try {
            if (::viewModel.isInitialized) {
                // ViewModel will be automatically cleared by ViewModelStore
                Log.d(TAG, "ViewModel cleanup handled by lifecycle")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during MainActivity cleanup", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning received")
        System.gc()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.w(TAG, "Trim memory called with level: $level")

        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Memory pressure detected, clearing caches")
                System.gc()
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: DetectionViewModel) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> {
            HomeScreen(
                onNavigateToFeature = { featureId ->
                    currentScreen = featureId
                }
            )
        }

        "detection" -> {
            DetectionScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = "home" }
            )
        }

        "inventory" -> {
            InventoryScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }

        "reports" -> {
            SafetyReportsScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }

        "analytics" -> {
            AnalyticsScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }

        "settings" -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }

        "about" -> {
            AboutScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }

        else -> {
            PlaceholderScreen(
                title = when (currentScreen) {
                    "analytics" -> "Analytics Dashboard"
                    "reports" -> "Safety Reports"
                    "settings" -> "Settings"
                    "about" -> "About & Help"
                    else -> "Feature"
                },
                description = "Coming soon!",
                onNavigateBack = { currentScreen = "home" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "🚧",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}