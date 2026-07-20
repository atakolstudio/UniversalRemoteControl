package com.atakolstudio.universalremote.ui.screens.remote

import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction

object RemoteLayouts {
    fun buttonsFor(category: DeviceCategory): List<RemoteFunction> = when (category) {
        DeviceCategory.TV -> listOf(
            RemoteFunction.POWER, RemoteFunction.INPUT_SOURCE,
            RemoteFunction.VOLUME_UP, RemoteFunction.VOLUME_DOWN, RemoteFunction.MUTE,
            RemoteFunction.CHANNEL_UP, RemoteFunction.CHANNEL_DOWN,
            RemoteFunction.DPAD_UP, RemoteFunction.DPAD_LEFT, RemoteFunction.DPAD_OK,
            RemoteFunction.DPAD_RIGHT, RemoteFunction.DPAD_DOWN,
            RemoteFunction.HOME, RemoteFunction.BACK, RemoteFunction.MENU
        )
        DeviceCategory.AIR_CONDITIONER -> listOf(
            RemoteFunction.POWER, RemoteFunction.MODE,
            RemoteFunction.TEMP_UP, RemoteFunction.TEMP_DOWN,
            RemoteFunction.FAN_SPEED, RemoteFunction.SWING, RemoteFunction.TIMER
        )
        DeviceCategory.SET_TOP_BOX -> listOf(
            RemoteFunction.POWER,
            RemoteFunction.VOLUME_UP, RemoteFunction.VOLUME_DOWN,
            RemoteFunction.CHANNEL_UP, RemoteFunction.CHANNEL_DOWN,
            RemoteFunction.DPAD_UP, RemoteFunction.DPAD_LEFT, RemoteFunction.DPAD_OK,
            RemoteFunction.DPAD_RIGHT, RemoteFunction.DPAD_DOWN,
            RemoteFunction.HOME, RemoteFunction.BACK, RemoteFunction.MENU
        )
        DeviceCategory.FAN -> listOf(
            RemoteFunction.POWER, RemoteFunction.FAN_SPEED, RemoteFunction.SWING, RemoteFunction.TIMER
        )
        DeviceCategory.SOUNDBAR -> listOf(
            RemoteFunction.POWER, RemoteFunction.VOLUME_UP, RemoteFunction.VOLUME_DOWN,
            RemoteFunction.MUTE, RemoteFunction.MODE, RemoteFunction.INPUT_SOURCE
        )
        DeviceCategory.PROJECTOR -> listOf(
            RemoteFunction.POWER, RemoteFunction.INPUT_SOURCE, RemoteFunction.MENU,
            RemoteFunction.DPAD_UP, RemoteFunction.DPAD_LEFT, RemoteFunction.DPAD_OK,
            RemoteFunction.DPAD_RIGHT, RemoteFunction.DPAD_DOWN
        )
        DeviceCategory.LIGHT -> listOf(RemoteFunction.POWER, RemoteFunction.MODE)
        DeviceCategory.OTHER -> listOf(RemoteFunction.POWER)
    }
}
