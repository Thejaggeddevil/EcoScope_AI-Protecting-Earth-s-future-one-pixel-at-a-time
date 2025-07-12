package com.himanshu.ecoscope.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himanshu.ecoscope.AuthResult

import com.himanshu.ecoscope.SignUpRequest
import com.himanshu.ecoscope.LoginRequest
import com.himanshu.ecoscope.User
import com.himanshu.ecoscope.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // Auth result state (Success / Error / Loading)
    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState.asStateFlow()

    // Loading spinner state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Logged in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkAuthState()
    }

    // Check login status on app start
    private fun checkAuthState() {
        viewModelScope.launch {
            if (authRepository.isUserSignedIn()) {
                val user = authRepository.getCurrentUserData()
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthResult.Success(user)
                } else {
                    _authState.value = AuthResult.Error("User data not found")
                }
            } else {
                _authState.value = AuthResult.Error("User not signed in")
            }
        }
    }

    // Handle sign-up
    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        preferredLanguage: String,
        password: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val signUpRequest = SignUpRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phoneNumber = phoneNumber.trim(),
                preferredLanguage = preferredLanguage,
                password = password
            )

            val result = authRepository.signUp(signUpRequest)
            _authState.value = result

            if (result is AuthResult.Success) {
                _currentUser.value = result.user
            }

            _isLoading.value = false
        }
    }

    // Handle sign-in
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val loginRequest = LoginRequest(
                email = email.trim(),
                password = password
            )

            val result = authRepository.signIn(loginRequest)
            _authState.value = result

            if (result is AuthResult.Success) {
                _currentUser.value = result.user
            }

            _isLoading.value = false
        }
    }

    // Handle sign-out
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            val success = authRepository.signOut()
            if (success) {
                _currentUser.value = null
                _authState.value = AuthResult.Error("User signed out")
            }
            _isLoading.value = false
        }
    }

    // Reset auth state (for navigation cleanup)
    fun clearAuthState() {
        _authState.value = AuthResult.Loading
    }

    // Basic validations
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length >= 10
    }

    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}
