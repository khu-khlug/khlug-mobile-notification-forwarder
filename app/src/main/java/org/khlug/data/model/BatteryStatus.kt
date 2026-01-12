package org.khlug.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BatteryStatusRequest(
    val batteryPercent: Int,
    val batteryStatus: String // "CHARGING" or "NOT_CHARGING"
)

data class BatteryInfo(
    val percent: Int,
    val isCharging: Boolean
) {
    val displayText: String
        get() = "배터리: $percent%\n충전 중: ${if (isCharging) "예" else "아니오"}"

    fun toRequest() = BatteryStatusRequest(
        batteryPercent = percent,
        batteryStatus = if (isCharging) "CHARGING" else "NOT_CHARGING"
    )
}
