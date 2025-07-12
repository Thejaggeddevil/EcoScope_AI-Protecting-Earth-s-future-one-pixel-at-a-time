package com.himanshu.ecoscope.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himanshu.ecoscope.network.AnalyzeRequest
import com.himanshu.ecoscope.network.AnalyzeResponse
import com.himanshu.ecoscope.network.RetrofitClient
import kotlinx.coroutines.launch

class DrainageViewModel : ViewModel() {

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
