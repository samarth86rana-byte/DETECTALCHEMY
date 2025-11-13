package com.example.detectalchemy.data

/**
 * Represents a detection class/category from the Falcon dataset
 */
data class DetectionClass(
    val id: Int,
    val name: String,
    val displayName: String = name,
    val color: String = "#FF0000",
    val count: Int = 0,
    val confidence: Float = 0.0f,
    val sampleImages: List<String> = emptyList(),
    val description: String = "",
    val isActive: Boolean = true
)