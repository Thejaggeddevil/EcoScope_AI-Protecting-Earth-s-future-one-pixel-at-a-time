package com.himanshu.ecoscope

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val preferredLanguage: String = "English",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "preferredLanguage" to preferredLanguage,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}

// Authentication states
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

// Sign up request data
data class SignUpRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val preferredLanguage: String,
    val password: String
)

// Login request data
data class LoginRequest(
    val email: String,
    val password: String
)
