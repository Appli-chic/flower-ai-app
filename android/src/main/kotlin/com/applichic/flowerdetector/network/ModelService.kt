package com.applichic.flowerdetector.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ModelService {
    @POST("predict")
    suspend fun getPrediction(@Body body: RequestBody): Response<PredictionResponse>
}

data class PredictionResponse(
    val prediction: String
)