package com.himanshu.ecoscope.network

data class AnalyzeRequest(
    val lat: Double,
    val lon: Double,
    val before: String,
    val after: String
)