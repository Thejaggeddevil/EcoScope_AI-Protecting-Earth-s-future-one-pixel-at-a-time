package com.himanshu.ecoscope.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

import com.himanshu.ecoscope.AuthResult
import com.himanshu.ecoscope.SignUpRequest
import com.himanshu.ecoscope.LoginRequest
import com.himanshu.ecoscope.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Current user flow
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Sign up user
    suspend fun signUp(signUpRequest: SignUpRequest): AuthResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(
                signUpRequest.email,
                signUpRequest.password
            ).await()

            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    firstName = signUpRequest.firstName,
                    lastName = signUpRequest.lastName,
                    email = signUpRequest.email,
                    phoneNumber = signUpRequest.phoneNumber,
                    preferredLanguage = signUpRequest.preferredLanguage,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                usersCollection.document(firebaseUser.uid).set(user.toMap()).await()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("User creation failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown signup error")
        }
    }

    // Sign in user
    suspend fun signIn(loginRequest: LoginRequest): AuthResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(
                loginRequest.email,
                loginRequest.password
            ).await()

            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()

                if (userDoc.exists()) {
                    val user = User(
                        uid = userDoc.getString("uid") ?: "",
                        firstName = userDoc.getString("firstName") ?: "",
                        lastName = userDoc.getString("lastName") ?: "",
                        email = userDoc.getString("email") ?: "",
                        phoneNumber = userDoc.getString("phoneNumber") ?: "",
                        preferredLanguage = userDoc.getString("preferredLanguage") ?: "English",
                        createdAt = userDoc.getDate("createdAt") ?: Date(),
                        updatedAt = userDoc.getDate("updatedAt") ?: Date()
                    )
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("User data not found in Firestore")
                }
            } else {
                AuthResult.Error("Firebase auth failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login error")
        }
    }

    // Sign out user
    suspend fun signOut(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get current user data
    suspend fun getCurrentUserData(): User? {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                if (userDoc.exists()) {
                    User(
                        uid = userDoc.getString("uid") ?: "",
                        firstName = userDoc.getString("firstName") ?: "",
                        lastName = userDoc.getString("lastName") ?: "",
                        email = userDoc.getString("email") ?: "",
                        phoneNumber = userDoc.getString("phoneNumber") ?: "",
                        preferredLanguage = userDoc.getString("preferredLanguage") ?: "English",
                        createdAt = userDoc.getDate("createdAt") ?: Date(),
                        updatedAt = userDoc.getDate("updatedAt") ?: Date()
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Update user data
    suspend fun updateUserData(user: User): Boolean {
        return try {
            val updatedUser = user.copy(updatedAt = Date())
            usersCollection.document(user.uid)
                .set(updatedUser.toMap())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Check if user is signed in
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}
