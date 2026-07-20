package com.atakolstudio.universalremote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceCategory {
    TV, AIR_CONDITIONER, SET_TOP_BOX, FAN, SOUNDBAR, PROJECTOR, LIGHT, OTHER
}

enum class ConnectionType { IR, WIFI, HYBRID }

/**
 * WiFi protocol used to talk to the device, when connectionType is WIFI or HYBRID.
 * GENERIC_HTTP / GENERIC_UDP cover devices without a dedicated integration.
 */
enum class WifiProtocol {
    NONE, SAMSUNG_SMARTVIEW, LG_WEBOS, XIAOMI_MIIO, GENERIC_HTTP, GENERIC_UDP, UPNP
}

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val model: String? = null,
    val category: DeviceCategory,
    val connectionType: ConnectionType,
    val wifiProtocol: WifiProtocol = WifiProtocol.NONE,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val port: Int? = null,
    /** Auth token / pairing key returned by TV pairing flows (e.g. Samsung SmartView token). */
    val authToken: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastUsedMillis: Long? = null,
    val isFavorite: Boolean = false
)
