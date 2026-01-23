package org.khlug.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val HOST = stringPreferencesKey("host")
        val API_KEY = stringPreferencesKey("api_key")
        val BACKGROUND_SYNC_ENABLED = booleanPreferencesKey("background_sync_enabled")
        val NOTIFICATION_FORWARDING_ENABLED = booleanPreferencesKey("notification_forwarding_enabled")
        val ALLOWED_NOTIFICATION_APPS = stringSetPreferencesKey("allowed_notification_apps")
    }

    suspend fun saveHost(host: String) {
        context.dataStore.edit { it[Keys.HOST] = host }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { it[Keys.API_KEY] = apiKey }
    }

    fun getHost(): Flow<String> =
        context.dataStore.data.map { it[Keys.HOST] ?: "" }

    fun getApiKey(): Flow<String> =
        context.dataStore.data.map { it[Keys.API_KEY] ?: "" }

    fun isConfigured(): Flow<Boolean> =
        context.dataStore.data.map {
            val host = it[Keys.HOST] ?: ""
            val apiKey = it[Keys.API_KEY] ?: ""
            host.isNotBlank() && apiKey.isNotBlank()
        }

    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BACKGROUND_SYNC_ENABLED] = enabled }
    }

    fun isBackgroundSyncEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[Keys.BACKGROUND_SYNC_ENABLED] ?: false }

    suspend fun setNotificationForwardingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_FORWARDING_ENABLED] = enabled }
    }

    fun isNotificationForwardingEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[Keys.NOTIFICATION_FORWARDING_ENABLED] ?: false }

    suspend fun setAllowedNotificationApps(packageNames: Set<String>) {
        context.dataStore.edit { it[Keys.ALLOWED_NOTIFICATION_APPS] = packageNames }
    }

    fun getAllowedNotificationApps(): Flow<Set<String>> =
        context.dataStore.data.map { it[Keys.ALLOWED_NOTIFICATION_APPS] ?: emptySet() }
}
