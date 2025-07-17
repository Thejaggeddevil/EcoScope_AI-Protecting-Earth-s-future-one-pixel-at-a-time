package com.mansi.ecoscope_frontend.network



import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface AnalyzeApi {
    @Headers("Content-Type: application/json")
    @POST("/analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalyzeResponse

    @FormUrlEncoded
    @POST("/satellite/compare")
    suspend fun compareSatellite(
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("start_date") startDate: String,
        @Field("end_date") endDate: String
    ): SatelliteCompareResponse
}

interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
