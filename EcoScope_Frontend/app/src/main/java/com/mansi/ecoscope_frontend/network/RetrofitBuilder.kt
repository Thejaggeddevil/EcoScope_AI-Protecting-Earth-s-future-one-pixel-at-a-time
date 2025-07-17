package com.mansi.ecoscope_frontend.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val api: AnalyzeApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.31.201:5000/") // your local Flask server
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnalyzeApi::class.java)
    }
}

object WeatherRetrofitClient {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}
