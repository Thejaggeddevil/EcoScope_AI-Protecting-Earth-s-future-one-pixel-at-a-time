package com.mansi.ecoscope_frontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansi.ecoscope_frontend.network.RetrofitClient
import com.mansi.ecoscope_frontend.network.SatelliteCompareResponse
import kotlinx.coroutines.launch

class SatelliteCompareViewModel : ViewModel() {
    var result by mutableStateOf<SatelliteCompareResponse?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun analyze(lat: Double, lon: Double, before: String, after: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val res = RetrofitClient.api.compareSatellite(lat, lon, before, after)
                result = res
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
} 