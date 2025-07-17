package com.mansi.ecoscope_frontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansi.ecoscope_frontend.network.AnalyzeRequest
import com.mansi.ecoscope_frontend.network.AnalyzeResponse
import com.mansi.ecoscope_frontend.network.RetrofitClient
import kotlinx.coroutines.launch

class AIViewModel : ViewModel() {
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
}

