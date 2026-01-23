package org.khlug.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.khlug.util.NotificationPermissionUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAppFilter: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkNotificationPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.host,
                onValueChange = viewModel::updateHost,
                label = { Text("서버 주소") },
                placeholder = { Text("https://api.example.com") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::saveSettings,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "저장 중..." else "저장")
            }

            if (uiState.saveSuccess) {
                Text(
                    "설정이 저장되었습니다",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 백그라운드 배터리 전송
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "백그라운드 배터리 전송",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "30분마다 자동으로 배터리 상태 전송",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = uiState.isBackgroundSyncEnabled,
                    onCheckedChange = { viewModel.toggleBackgroundSync(context) },
                    enabled = uiState.isSettingsConfigured
                )
            }

            if (!uiState.isSettingsConfigured && uiState.isBackgroundSyncEnabled) {
                Text(
                    "설정 완료 후 백그라운드 동기화를 사용할 수 있습니다",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 알림 포워딩 섹션
            Text(
                "알림 포워딩",
                style = MaterialTheme.typography.titleMedium
            )

            // 알림 접근 권한 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isNotificationPermissionGranted)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (uiState.isNotificationPermissionGranted)
                            "알림 접근 권한 허용됨"
                        else
                            "알림 접근 권한 필요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isNotificationPermissionGranted)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (uiState.isNotificationPermissionGranted)
                            "앱이 알림을 읽을 수 있습니다"
                        else
                            "알림 포워딩을 사용하려면 권한을 허용해주세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.isNotificationPermissionGranted)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )

                    if (!uiState.isNotificationPermissionGranted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = NotificationPermissionUtil.createNotificationListenerSettingsIntent()
                                context.startActivity(intent)
                            }
                        ) {
                            Text("설정 열기")
                        }
                    }
                }
            }

            // 알림 포워딩 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "알림 포워딩 활성화",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "선택한 앱의 알림을 서버로 전송",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = uiState.isNotificationForwardingEnabled,
                    onCheckedChange = { viewModel.toggleNotificationForwarding() },
                    enabled = uiState.isSettingsConfigured && uiState.isNotificationPermissionGranted
                )
            }

            // 알림 전송 앱 선택 버튼
            OutlinedButton(
                onClick = onNavigateToAppFilter,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isNotificationPermissionGranted
            ) {
                Text("알림 전송 앱 선택")
            }
        }
    }
}
