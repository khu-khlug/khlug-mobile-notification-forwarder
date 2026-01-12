package org.khlug.data.repository

import android.content.Context
import kotlinx.coroutines.flow.first
import org.khlug.data.model.BatteryInfo
import org.khlug.data.network.ApiClient
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.util.getBatteryInfo
import java.io.IOException

class BatteryRepository(
    private val context: Context,
    private val settingsPreferences: SettingsPreferences
) {
    fun getBatteryInfo(): BatteryInfo {
        return context.getBatteryInfo()
    }

    suspend fun sendBatteryStatus(batteryInfo: BatteryInfo): Result<Unit> {
        return try {
            val host = settingsPreferences.getHost().first()
            val apiKey = settingsPreferences.getApiKey().first()

            if (host.isBlank() || apiKey.isBlank()) {
                return Result.failure(Exception("설정을 먼저 구성해주세요"))
            }

            val apiService = ApiClient.createApiService(host)
            val response = apiService.sendBatteryStatus(apiKey, batteryInfo.toRequest())

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("서버 오류 (HTTP ${response.code()})"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("오류 발생: ${e.message}"))
        }
    }
}
