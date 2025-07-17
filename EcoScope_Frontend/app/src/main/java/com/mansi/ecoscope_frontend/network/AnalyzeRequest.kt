package com.mansi.ecoscope_frontend.network



data class AnalyzeRequest(
    val lat: Double,
    val lon: Double,
    val before: String,
    val after: String
)