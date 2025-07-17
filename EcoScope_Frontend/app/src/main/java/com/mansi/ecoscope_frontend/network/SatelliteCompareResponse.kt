package com.mansi.ecoscope_frontend.network

data class SatelliteCompareResponse(
    val before_image_url: String?,
    val after_image_url: String?,
    val impact_image_url: String?,
    val ai_analysis: String?, // or Map<String, Any> if needed
    val location: String?,
    val date_range: String?,
    val change_detection: String?,
    val timestamp: String?
) 