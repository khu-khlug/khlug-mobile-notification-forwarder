package org.khlug.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.khlug.worker.BatterySyncWorker
import java.util.concurrent.TimeUnit

object WorkManagerUtil {
    private const val BATTERY_SYNC_WORK_NAME = "battery_sync_work"

    fun startBatterySync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Wi-Fi + 모바일 데이터 모두 허용
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BatterySyncWorker>(
            30, TimeUnit.MINUTES // 30분 주기
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BATTERY_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // 이미 실행 중이면 유지
            workRequest
        )
    }

    fun stopBatterySync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BATTERY_SYNC_WORK_NAME)
    }

    fun isBatterySyncScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(BATTERY_SYNC_WORK_NAME)
            .get()
        return workInfos.any { !it.state.isFinished }
    }
}
