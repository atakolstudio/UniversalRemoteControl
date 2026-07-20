package com.atakolstudio.universalremote.wifi

import android.util.Base64
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response as OkResponse
import okio.ByteString
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Samsung "SmartView" remote control protocol used by modern Tizen TVs:
 * a WebSocket connection to ws(s)://<ip>:8001/api/v2/channels/samsung.remote.control
 * that accepts JSON-RPC-like key press events. The first connection triggers an
 * on-screen pairing prompt on the TV; the returned token should be persisted
 * (DeviceEntity.authToken) and reused on subsequent connections.
 */
@Singleton
class SamsungSmartViewSender @Inject constructor(
    private val okHttpClient: OkHttpClient
) : WifiCommandSender {

    private val keyMap = mapOf(
        RemoteFunction.POWER to "KEY_POWER",
        RemoteFunction.VOLUME_UP to "KEY_VOLUP",
        RemoteFunction.VOLUME_DOWN to "KEY_VOLDOWN",
        RemoteFunction.MUTE to "KEY_MUTE",
        RemoteFunction.CHANNEL_UP to "KEY_CHUP",
        RemoteFunction.CHANNEL_DOWN to "KEY_CHDOWN",
        RemoteFunction.HOME to "KEY_HOME",
        RemoteFunction.BACK to "KEY_RETURN",
        RemoteFunction.MENU to "KEY_MENU",
        RemoteFunction.DPAD_UP to "KEY_UP",
        RemoteFunction.DPAD_DOWN to "KEY_DOWN",
        RemoteFunction.DPAD_LEFT to "KEY_LEFT",
        RemoteFunction.DPAD_RIGHT to "KEY_RIGHT",
        RemoteFunction.DPAD_OK to "KEY_ENTER",
        RemoteFunction.INPUT_SOURCE to "KEY_SOURCE"
    )

    override suspend fun send(device: DeviceEntity, function: RemoteFunction): WifiCommandResult {
        val ip = device.ipAddress ?: return WifiCommandResult.Failed("IP adresi tanımlı değil")
        val keyCode = keyMap[function]
            ?: return WifiCommandResult.Failed("Bu fonksiyon Samsung SmartView için desteklenmiyor")

        val appNameB64 = Base64.encodeToString(
            "UniversalRemoteControl".toByteArray(), Base64.NO_WRAP
        )
        val tokenParam = device.authToken?.let { "&token=$it" } ?: ""
        val url = "wss://$ip:8002/api/v2/channels/samsung.remote.control?name=$appNameB64$tokenParam"

        return suspendCancellableCoroutine { cont ->
            val request = Request.Builder().url(url).build()
            var socket: WebSocket? = null
            socket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: OkResponse) {
                    val payload = JSONObject().apply {
                        put("method", "ms.remote.control")
                        put("params", JSONObject().apply {
                            put("Cmd", "Click")
                            put("DataOfCmd", keyCode)
                            put("Option", "false")
                            put("TypeOfRemote", "SendRemoteKey")
                        })
                    }
                    webSocket.send(payload.toString())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    // First message typically carries the pairing token; persisting it
                    // is left to the calling repository layer via the returned result.
                    webSocket.close(1000, null)
                    if (cont.isActive) cont.resume(WifiCommandResult.Sent)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    webSocket.close(1000, null)
                    if (cont.isActive) cont.resume(WifiCommandResult.Sent)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: OkResponse?) {
                    if (cont.isActive) cont.resume(WifiCommandResult.Failed(t.message ?: "Bağlantı hatası"))
                }
            })
            cont.invokeOnCancellation { socket?.cancel() }
        }
    }
}
