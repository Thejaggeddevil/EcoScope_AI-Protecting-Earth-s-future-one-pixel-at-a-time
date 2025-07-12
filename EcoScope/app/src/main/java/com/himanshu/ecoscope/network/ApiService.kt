package com.himanshu.ecoscope.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AnalyzeApi {
    @Headers("Content-Type: application/json")
    @POST("/analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalyzeResponse

}
