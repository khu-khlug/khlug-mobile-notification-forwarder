package org.khlug.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.data.repository.BatteryRepository

class BatterySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting battery sync")

        return try {
            val settingsPreferences = SettingsPreferences(applicationContext)
            val repository = BatteryRepository(applicationContext, settingsPreferences)

            // 설정 확인
            val isConfigured = settingsPreferences.isConfigured().first()
            if (!isConfigured) {
                Log.d(TAG, "Settings not configured, skipping sync")
                return Result.success()
            }

            // 배터리 정보 수집 및 전송
            val batteryInfo = repository.getBatteryInfo()
            Log.d(TAG, "Battery info: ${batteryInfo.percent}%, charging: ${batteryInfo.isCharging}")

            val result = repository.sendBatteryStatus(batteryInfo)

            if (result.isSuccess) {
                Log.d(TAG, "Battery sync completed successfully")
            } else {
                Log.w(TAG, "Battery sync failed: ${result.exceptionOrNull()?.message}")
            }

            // 재시도 없음 - 실패해도 성공 처리
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Battery sync failed with exception", e)
            // 예외 발생 시에도 재시도 없이 성공 처리
            Result.success()
        }
    }

    companion object {
        private const val TAG = "BatterySyncWorker"
    }
}
