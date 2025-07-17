package com.mansi.ecoscope_frontend





import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signup(
        email: String,
        password: String,
        context: Context,
        onResult: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                showToast(context, "Signup successful ✅. Verification email sent.")
                            } else {
                                showToast(context, "Signup done, but failed to send verification email.")
                            }
                        }
                    onResult(true)
                } else {
                    val error = task.exception?.message ?: "Unknown error"
                    showToast(context, "Signup failed: $error")
                    onResult(false)
                }
            }
    }

    fun login(
        email: String,
        password: String,
        context: Context,
        onResult: (Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        showToast(context, "Login successful 🎉")
                        onResult(true)
                    } else {
                        auth.signOut()
                        showToast(context, "Please verify your email before logging in.")
                        onResult(false)
                    }
                } else {
                    val error = task.exception?.message ?: "Unknown error"
                    showToast(context, "Login failed: $error")
                    onResult(false)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun currentUserId(): String? = auth.currentUser?.uid

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
