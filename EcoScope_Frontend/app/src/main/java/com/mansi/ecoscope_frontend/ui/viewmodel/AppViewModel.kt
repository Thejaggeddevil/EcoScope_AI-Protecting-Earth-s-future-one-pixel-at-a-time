package com.mansi.ecoscope_frontend.ui.viewmodel



import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansi.ecoscope_frontend.network.AnalyzeRequest
import com.mansi.ecoscope_frontend.network.RetrofitClient
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mansi.ecoscope_frontend.FirebaseHelper
import com.mansi.ecoscope_frontend.FirebaseService
import com.mansi.ecoscope_frontend.MainActivity
import com.mansi.ecoscope_frontend.network.AnalyzeResponse
import com.mansi.ecoscope_frontend.utils.LocaleHelper
import com.mansi.ecoscope_frontend.utils.LocationUtils
import com.mansi.ecoscope_frontend.utils.PrefsManager
import java.text.SimpleDateFormat
import java.util.*
import com.mansi.ecoscope_frontend.network.WeatherResponse
import com.mansi.ecoscope_frontend.network.WeatherRetrofitClient


class AppViewModel : ViewModel() {

    var result by mutableStateOf<AnalyzeResponse?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun analyze(lat: Double, lon: Double, before: String, after: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val req = AnalyzeRequest(lat, lon, before, after)
                val res = RetrofitClient.api.analyze(req)
                result = res
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
    // 👇 Add this inside AppViewModel class
    val chatbotMessages = mutableStateListOf<NotificationCompat.MessagingStyle.Message>()
    var isTyping by mutableStateOf(false)
    var animatedReply by mutableStateOf("")

    // Weather state
    var weather by mutableStateOf<WeatherResponse?>(null)
    var weatherLoading by mutableStateOf(false)
    var weatherError by mutableStateOf<String?>(null)

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            weatherLoading = true
            weatherError = null
            try {
                val res = WeatherRetrofitClient.api.getCurrentWeather(lat, lon, apiKey)
                weather = res
            } catch (e: Exception) {
                weatherError = e.message
            } finally {
                weatherLoading = false
            }
        }
    }


    private val TAG = "AppViewModel"

    var isDarkMode = mutableStateOf(false)
        private set

    var isHindi = mutableStateOf(false)
        private set

    private val _username = mutableStateOf("User")
    val username: State<String> get() = _username



    // ✅ Save username locally + set
    fun setUsername(name: String, context: Context? = null) {
        _username.value = name
        context?.let {
            PrefsManager.setUsername(it, name)
            Log.d(TAG, "✅ Username saved locally: $name")
        }
    }

    // ✅ Load username from Firebase Firestore (or fallback to prefs)
    fun loadUsername(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _username.value = PrefsManager.getUsername(context)
            Log.w(TAG, "⚠️ UID is null. Loaded from local prefs: ${_username.value}")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                if (!name.isNullOrBlank()) {
                    _username.value = name
                    PrefsManager.setUsername(context, name)
                    Log.d(TAG, "✅ Username loaded from Firestore: $name")
                } else {
                    _username.value = PrefsManager.getUsername(context)
                    Log.w(TAG, "⚠️ Firestore name is blank. Used local fallback: ${_username.value}")
                }
            }
            .addOnFailureListener {
                _username.value = PrefsManager.getUsername(context)
                Log.e(TAG, "❌ Firestore error: ${it.message}")
            }
    }

    // ✅ Save SOS


    // ✅ Remove SOS


    // ✅ Load all SOS from Firebase

    fun clearChatbotMessages() {
        chatbotMessages.clear()
    }


    fun logout() {
        FirebaseService.logout()
        _username.value = "User"
    }

    // 🔁 Toggle local dark mode setting
    fun toggleTheme(context: Context) {
        val newDark = !isDarkMode.value
        isDarkMode.value = newDark
        PrefsManager.setDarkMode(context, newDark)
    }

    fun toggleLanguage(context: Context, isHindiSelected: Boolean) {
        isHindi.value = isHindiSelected
        PrefsManager.setLangPref(context, isHindiSelected)
    }

    fun restartAppWithLocale(context: Context, lang: String) {
        LocaleHelper.setLocale(context, lang)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    fun sendSOS(context: Context, contacts: List<String>) {
        LocationUtils.getLastLocation(context) { location ->
            val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            val message = if (location != null) {
                "🚨 SOS! I need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "🚨 SOS! I need help. Location unavailable."
            }

            contacts.forEach { phoneNumber ->
                if (phoneNumber.isNotBlank()) {
                    try {
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("smsto:$phoneNumber")
                            putExtra("sms_body", message)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(smsIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "❌ Failed to send SMS to $phoneNumber", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }
    }

    fun ensureLocationOn(context: Context) {
        if (!LocationUtils.isLocationEnabled(context)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    // 🧠 Load AI chat history from Firebase

}
