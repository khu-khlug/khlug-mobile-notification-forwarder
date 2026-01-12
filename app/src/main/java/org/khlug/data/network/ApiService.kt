package org.khlug.data.network

import org.khlug.data.model.BatteryStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("/internal/khlug-phone/status")
    suspend fun sendBatteryStatus(
        @Header("x-api-key") apiKey: String,
        @Body request: BatteryStatusRequest
    ): Response<Unit>
}
