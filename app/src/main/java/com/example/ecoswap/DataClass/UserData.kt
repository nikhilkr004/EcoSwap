package com.example.ecoswap.DataClass

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class UserData(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? =null,
    val password: String? =null,
    val profileImageUrl: String? = null,
    val location: Map<String, Double>? = null,
    val joinedAt: Timestamp? =null,  // Store as timestamp in millis
)
