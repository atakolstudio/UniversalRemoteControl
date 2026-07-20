package com.atakolstudio.universalremote.wifi

import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.local.entity.WifiProtocol
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiCommandRouter @Inject constructor(
    private val samsungSmartViewSender: SamsungSmartViewSender,
    private val lgWebOsSender: LgWebOsSender,
    private val genericHttpCommandSender: GenericHttpCommandSender
) {
    suspend fun send(device: DeviceEntity, function: RemoteFunction): WifiCommandResult {
        val sender: WifiCommandSender = when (device.wifiProtocol) {
            WifiProtocol.SAMSUNG_SMARTVIEW -> samsungSmartViewSender
            WifiProtocol.LG_WEBOS -> lgWebOsSender
            WifiProtocol.XIAOMI_MIIO,
            WifiProtocol.GENERIC_HTTP,
            WifiProtocol.GENERIC_UDP,
            WifiProtocol.UPNP -> genericHttpCommandSender
            WifiProtocol.NONE -> return WifiCommandResult.Failed("WiFi protokolü tanımlı değil")
        }
        return sender.send(device, function)
    }
}
