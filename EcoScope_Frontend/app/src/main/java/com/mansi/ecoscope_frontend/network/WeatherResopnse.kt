package com.mansi.ecoscope_frontend.network


data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
) {
    data class Main(
        val temp: Double,
        val humidity: Int
    )
    data class Weather(
        val main: String,
        val description: String
    )
    data class Wind(
        val speed: Double
    )
}