package com.atakolstudio.universalremote.wifi

import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction

sealed class WifiCommandResult {
    data class Sent(val newAuthToken: String? = null) : WifiCommandResult()
    data class Failed(val reason: String) : WifiCommandResult()
}

interface WifiCommandSender {
    suspend fun send(device: DeviceEntity, function: RemoteFunction): WifiCommandResult
}
