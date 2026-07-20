package com.atakolstudio.universalremote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class IrProtocol { NEC, NECX, RC5, RC6, SAMSUNG36, SONY12, SONY15, SONY20, PANASONIC, RAW }

/** Logical remote function shared across categories, mapped to concrete IR/WiFi commands. */
enum class RemoteFunction {
    POWER, VOLUME_UP, VOLUME_DOWN, MUTE,
    CHANNEL_UP, CHANNEL_DOWN, INPUT_SOURCE,
    NUMBER_0, NUMBER_1, NUMBER_2, NUMBER_3, NUMBER_4,
    NUMBER_5, NUMBER_6, NUMBER_7, NUMBER_8, NUMBER_9,
    DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, DPAD_OK,
    HOME, BACK, MENU,
    MODE, TEMP_UP, TEMP_DOWN, SWING, FAN_SPEED, TIMER
}

/**
 * A single IR code, either shipped as a preset (brand+category+function -> code)
 * or captured by the user via the learning flow (rawPattern populated instead of hexCode).
 */
@Entity(tableName = "ir_codes")
data class IrCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val category: DeviceCategory,
    val function: RemoteFunction,
    val protocol: IrProtocol,
    /** Hex-encoded command payload for protocol-based codes, e.g. "0x20DF10EF" for NEC. */
    val hexCode: String? = null,
    /** Comma-separated on/off microsecond durations for a raw learned pattern. */
    val rawPattern: String? = null,
    val carrierFrequencyHz: Int = 38000,
    val isUserLearned: Boolean = false,
    /** Non-null when this code was learned specifically for one device instance. */
    val deviceId: Long? = null
)
