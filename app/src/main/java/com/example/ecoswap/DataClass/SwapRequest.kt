package com.example.ecoswap.DataClass

data class SwapRequest(
    val requestId: String = "",              // Unique ID for each request
    val senderId: String = "",
    val receiverId: String = "",
    val productId: String = "",
    val status: String = "pending",          // pending, accepted, rejected
    val timestamp: com.google.firebase.Timestamp? = null
)