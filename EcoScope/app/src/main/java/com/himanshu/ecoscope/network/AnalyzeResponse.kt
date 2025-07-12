package com.himanshu.ecoscope.network

data class AnalyzeResponse(
    val before_url: String,
    val after_url: String,
    val change_map_url: String,
    val area_type: String
)