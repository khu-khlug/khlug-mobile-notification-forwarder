package org.khlug.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.data.repository.BatteryRepository

data class HomeUiState(
    val batteryInfoText: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSettingsConfigured: Boolean = false
)

class HomeViewModel(
    private val repository: BatteryRepository,
    settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsPreferences.isConfigured().collect { configured ->
                _uiState.update { it.copy(isSettingsConfigured = configured) }
            }
        }
    }

    fun checkBatteryStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 배터리 정보 가져오기
            val batteryInfo = repository.getBatteryInfo()
            _uiState.update { it.copy(batteryInfoText = batteryInfo.displayText) }

            // API 전송
            val result = repository.sendBatteryStatus(batteryInfo)
            result.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
