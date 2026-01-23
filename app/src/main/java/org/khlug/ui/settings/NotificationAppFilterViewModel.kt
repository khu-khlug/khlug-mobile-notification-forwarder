package org.khlug.ui.settings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.khlug.data.model.InstalledAppInfo
import org.khlug.data.preferences.SettingsPreferences

data class NotificationAppFilterUiState(
    val isLoading: Boolean = true,
    val apps: List<InstalledAppInfo> = emptyList(),
    val selectedApps: Set<String> = emptySet(),
    val searchQuery: String = ""
)

class NotificationAppFilterViewModel(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationAppFilterUiState())
    val uiState: StateFlow<NotificationAppFilterUiState> = _uiState.asStateFlow()

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val selectedApps = settingsPreferences.getAllowedNotificationApps().first()

            val apps = withContext(Dispatchers.IO) {
                loadAppsFromPackageManager(context)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                apps = apps,
                selectedApps = selectedApps
            )
        }
    }

    private fun loadAppsFromPackageManager(context: Context): List<InstalledAppInfo> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return installedApps
            .filter { appInfo ->
                // 사용자 앱과 시스템 앱 중 런처가 있는 것만 포함
                val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                val hasLauncher = packageManager.getLaunchIntentForPackage(appInfo.packageName) != null
                isUserApp || hasLauncher
            }
            .map { appInfo ->
                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = try {
                        packageManager.getApplicationIcon(appInfo)
                    } catch (e: Exception) {
                        null
                    }
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleAppSelection(packageName: String) {
        val currentSelected = _uiState.value.selectedApps.toMutableSet()
        if (currentSelected.contains(packageName)) {
            currentSelected.remove(packageName)
        } else {
            currentSelected.add(packageName)
        }
        _uiState.value = _uiState.value.copy(selectedApps = currentSelected)

        viewModelScope.launch {
            settingsPreferences.setAllowedNotificationApps(currentSelected)
        }
    }

    fun getFilteredApps(): List<InstalledAppInfo> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) {
            return _uiState.value.apps
        }
        return _uiState.value.apps.filter {
            it.appName.lowercase().contains(query) ||
                    it.packageName.lowercase().contains(query)
        }
    }
}
