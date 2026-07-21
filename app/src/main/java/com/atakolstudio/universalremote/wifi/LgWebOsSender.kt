package com.atakolstudio.universalremote.wifi

import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response as OkResponse
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * LG webOS TVs speak the "SSAP" protocol over a plain WebSocket on port 3000/3001.
 * A handshake (register) request must succeed first; webOS shows an on-screen
 * pairing prompt and returns a client-key that should be cached in
 * DeviceEntity.authToken for silent reconnects afterward.
 */
@Singleton
class LgWebOsSender @Inject constructor(
    private val okHttpClient: OkHttpClient
) : WifiCommandSender {

    private val buttonMap = mapOf(
        RemoteFunction.POWER to "POWER",
        RemoteFunction.VOLUME_UP to "VOLUMEUP",
        RemoteFunction.VOLUME_DOWN to "VOLUMEDOWN",
        RemoteFunction.MUTE to "MUTE",
        RemoteFunction.CHANNEL_UP to "CHANNELUP",
        RemoteFunction.CHANNEL_DOWN to "CHANNELDOWN",
        RemoteFunction.HOME to "HOME",
        RemoteFunction.BACK to "BACK",
        RemoteFunction.DPAD_UP to "UP",
        RemoteFunction.DPAD_DOWN to "DOWN",
        RemoteFunction.DPAD_LEFT to "LEFT",
        RemoteFunction.DPAD_RIGHT to "RIGHT",
        RemoteFunction.DPAD_OK to "ENTER"
    )

    override suspend fun send(device: DeviceEntity, function: RemoteFunction): WifiCommandResult {
        val ip = device.ipAddress ?: return WifiCommandResult.Failed("IP adresi tanımlı değil")
        val button = buttonMap[function]
            ?: return WifiCommandResult.Failed("Bu fonksiyon LG WebOS için desteklenmiyor")

        val url = "ws://$ip:3000"

        return suspendCancellableCoroutine { cont ->
            val request = Request.Builder().url(url).build()
            okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: OkResponse) {
                    val register = JSONObject().apply {
                        put("type", "register")
                        put("id", "register_0")
                        put("payload", JSONObject().apply {
                            device.authToken?.let { put("client-key", it) }
                        })
                    }
                    webSocket.send(register.toString())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val response = runCatching { JSONObject(text) }.getOrNull()
                    when (response?.optString("type")) {
                        "registered" -> {
                            val newClientKey = response.optJSONObject("payload")
                                ?.optString("client-key")?.takeIf { it.isNotBlank() }

                            val direct = JSONObject().apply {
                                put("type", "request")
                                put("id", "btn_1")
                                put("uri", "ssap://com.webos.service.ime/sendEnterKey")
                                put("payload", JSONObject().apply { put("button", button) })
                            }
                            webSocket.send(direct.toString())
                            webSocket.close(1000, null)
                            if (cont.isActive) cont.resume(WifiCommandResult.Sent(newAuthToken = newClientKey))
                        }
                        "error" -> {
                            webSocket.close(1000, null)
                            if (cont.isActive) {
                                cont.resume(WifiCommandResult.Failed(response.optString("error")))
                            }
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: OkResponse?) {
                    if (cont.isActive) cont.resume(WifiCommandResult.Failed(t.message ?: "Bağlantı hatası"))
                }
            })
        }
    }
}
