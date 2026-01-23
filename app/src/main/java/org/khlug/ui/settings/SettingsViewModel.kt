package org.khlug.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.util.NotificationPermissionUtil
import org.khlug.util.WorkManagerUtil

data class SettingsUiState(
    val host: String = "",
    val apiKey: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isBackgroundSyncEnabled: Boolean = false,
    val isSettingsConfigured: Boolean = false,
    val isNotificationPermissionGranted: Boolean = false,
    val isNotificationForwardingEnabled: Boolean = false
)

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsPreferences.getHost(),
                settingsPreferences.getApiKey(),
                settingsPreferences.isBackgroundSyncEnabled(),
                settingsPreferences.isConfigured(),
                settingsPreferences.isNotificationForwardingEnabled()
            ) { host, apiKey, backgroundSyncEnabled, isConfigured, notificationForwardingEnabled ->
                _uiState.update {
                    it.copy(
                        host = host,
                        apiKey = apiKey,
                        isBackgroundSyncEnabled = backgroundSyncEnabled,
                        isSettingsConfigured = isConfigured,
                        isNotificationForwardingEnabled = notificationForwardingEnabled
                    )
                }
            }.collect {}
        }
    }

    fun updateHost(host: String) {
        _uiState.update { it.copy(host = host, saveSuccess = false) }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey, saveSuccess = false) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            settingsPreferences.saveHost(_uiState.value.host)
            settingsPreferences.saveApiKey(_uiState.value.apiKey)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun toggleBackgroundSync(context: Context) {
        viewModelScope.launch {
            val newValue = !_uiState.value.isBackgroundSyncEnabled
            settingsPreferences.setBackgroundSyncEnabled(newValue)

            if (newValue) {
                WorkManagerUtil.startBatterySync(context)
            } else {
                WorkManagerUtil.stopBatterySync(context)
            }
        }
    }

    fun checkNotificationPermission(context: Context) {
        val isGranted = NotificationPermissionUtil.isNotificationListenerEnabled(context)
        _uiState.update { it.copy(isNotificationPermissionGranted = isGranted) }
    }

    fun toggleNotificationForwarding() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isNotificationForwardingEnabled
            settingsPreferences.setNotificationForwardingEnabled(newValue)
        }
    }
}
