package com.atakolstudio.universalremote.wifi

import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fallback sender for WiFi devices without a dedicated brand integration
 * (e.g. Xiaomi Mi Box via local HTTP bridge, ESP8266/Tasmota-based smart plugs
 * used for IR-less "power" toggling, generic smart soundbars, etc.).
 * Expects the device to expose `http://<ip>:<port>/<functionName>`.
 */
@Singleton
class GenericHttpCommandSender @Inject constructor(
    private val okHttpClient: OkHttpClient
) : WifiCommandSender {

    override suspend fun send(device: DeviceEntity, function: RemoteFunction): WifiCommandResult =
        withContext(Dispatchers.IO) {
            val ip = device.ipAddress ?: return@withContext WifiCommandResult.Failed("IP adresi tanımlı değil")
            val port = device.port ?: 80
            val url = "http://$ip:$port/command?action=${function.name.lowercase()}"

            try {
                val request = Request.Builder().url(url).get().build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        WifiCommandResult.Sent
                    } else {
                        WifiCommandResult.Failed("HTTP ${response.code}")
                    }
                }
            } catch (e: Exception) {
                WifiCommandResult.Failed(e.message ?: "Ağ hatası")
            }
        }
}
