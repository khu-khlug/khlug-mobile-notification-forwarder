package org.khlug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.ui.navigation.AppNavigation
import org.khlug.ui.theme.MobileNotificationForwarderTheme
import org.khlug.util.WorkManagerUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WorkManager 상태 복원 체크
        lifecycleScope.launch {
            val settingsPreferences = SettingsPreferences(this@MainActivity)
            val isEnabled = settingsPreferences.isBackgroundSyncEnabled().first()

            if (isEnabled && !WorkManagerUtil.isBatterySyncScheduled(this@MainActivity)) {
                WorkManagerUtil.startBatterySync(this@MainActivity)
            }
        }

        enableEdgeToEdge()
        setContent {
            MobileNotificationForwarderTheme {
                AppNavigation(context = this)
            }
        }
    }
}