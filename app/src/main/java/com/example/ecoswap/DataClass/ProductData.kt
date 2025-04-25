package com.example.ecoswap.DataClass

import com.google.firebase.firestore.GeoPoint

data class ProductData(
    val productId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrls: List<String> = emptyList(),
    val location: Map<String, Double>? = null,    // ✅ Change from String to GeoPoint
    val postedBy: String = "",
    val userName: String = "",
    val timestamp: Long = 0L,
    val availability: String = "Available",
    val swapType: String = "Free"
)
