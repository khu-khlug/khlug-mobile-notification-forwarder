package org.khlug.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.khlug.data.model.NotificationRequest
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.data.repository.NotificationRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationForwarderService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationForwarder"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var settingsPreferences: SettingsPreferences
    private lateinit var notificationRepository: NotificationRepository

    override fun onCreate() {
        super.onCreate()
        settingsPreferences = SettingsPreferences(applicationContext)
        notificationRepository = NotificationRepository(settingsPreferences)
        Log.d(TAG, "NotificationForwarderService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "NotificationForwarderService destroyed")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        serviceScope.launch {
            try {
                processNotification(sbn)
            } catch (e: Exception) {
                Log.e(TAG, "알림 처리 중 오류 발생", e)
            }
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
        // 1. 포워딩 활성화 확인
        if (!notificationRepository.isForwardingEnabled()) {
            Log.d(TAG, "알림 포워딩이 비활성화됨")
            return
        }

        // 2. 설정 구성 확인
        if (!notificationRepository.isConfigured()) {
            Log.d(TAG, "설정이 구성되지 않음")
            return
        }

        // 3. 허용된 앱인지 확인
        val packageName = sbn.packageName
        if (!notificationRepository.isAppAllowed(packageName)) {
            Log.d(TAG, "허용되지 않은 앱: $packageName")
            return
        }

        // 4. 알림 정보 추출
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // 빈 알림 무시
        if (title.isBlank() && content.isBlank()) {
            Log.d(TAG, "빈 알림 무시: $packageName")
            return
        }

        // 5. 앱 이름 가져오기
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        // 6. ISO 8601 형식 시간 생성
        val receivedAt = DateTimeFormatter.ISO_INSTANT
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())

        // 7. API 호출
        val request = NotificationRequest(
            appName = appName,
            title = title,
            content = content,
            receivedAt = receivedAt
        )

        Log.d(TAG, "알림 전송 시도: $appName - $title")
        notificationRepository.sendNotification(request)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // 필요시 구현
    }
}
