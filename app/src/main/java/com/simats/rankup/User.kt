package com.simats.rankup

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String, // "Student" or "Faculty"
    var status: String, // "Active" or "Blocked"
    val placementStatus: String? = null,
    val profileImage: Int,
    // New fields
    val password: String = "password123", // Default if not provided
    val age: String = "",
    val department: String = "",
    val gender: String = "",
    val registerNumber: String? = null
)
