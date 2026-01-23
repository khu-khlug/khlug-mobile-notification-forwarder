package org.khlug.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val appName: String,
    val title: String,
    val content: String,
    val receivedAt: String // ISO 8601 format
)
