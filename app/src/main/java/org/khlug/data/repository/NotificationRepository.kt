package org.khlug.data.repository

import android.util.Log
import kotlinx.coroutines.flow.first
import org.khlug.data.model.NotificationRequest
import org.khlug.data.network.ApiClient
import org.khlug.data.preferences.SettingsPreferences

class NotificationRepository(
    private val settingsPreferences: SettingsPreferences
) {
    companion object {
        private const val TAG = "NotificationRepository"
    }

    suspend fun sendNotification(request: NotificationRequest): Result<Unit> {
        return try {
            val host = settingsPreferences.getHost().first()
            val apiKey = settingsPreferences.getApiKey().first()

            if (host.isBlank() || apiKey.isBlank()) {
                return Result.failure(Exception("설정이 구성되지 않았습니다"))
            }

            val apiService = ApiClient.createApiService(host)
            val response = apiService.sendNotification(apiKey, request)

            if (response.isSuccessful) {
                Log.d(TAG, "알림 전송 성공: ${request.appName}")
                Result.success(Unit)
            } else {
                Log.e(TAG, "알림 전송 실패: HTTP ${response.code()}")
                Result.failure(Exception("서버 오류 (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "알림 전송 중 오류 발생", e)
            Result.failure(e)
        }
    }

    suspend fun isForwardingEnabled(): Boolean {
        return settingsPreferences.isNotificationForwardingEnabled().first()
    }

    suspend fun isConfigured(): Boolean {
        return settingsPreferences.isConfigured().first()
    }

    suspend fun isAppAllowed(packageName: String): Boolean {
        val allowedApps = settingsPreferences.getAllowedNotificationApps().first()
        return allowedApps.contains(packageName)
    }

    suspend fun getAllowedApps(): Set<String> {
        return settingsPreferences.getAllowedNotificationApps().first()
    }
}
