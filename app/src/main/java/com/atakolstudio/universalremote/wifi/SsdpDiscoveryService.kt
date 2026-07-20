package com.atakolstudio.universalremote.wifi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

data class SsdpDevice(
    val location: String,
    val server: String?,
    val usn: String?,
    val address: String
)

/**
 * Discovers WiFi-controllable devices (smart TVs, UPnP media renderers, smart plugs, etc.)
 * on the local network via SSDP M-SEARCH multicast, per UPnP device architecture.
 * Requires CHANGE_WIFI_MULTICAST_STATE + a held MulticastLock (acquired by the caller).
 */
@Singleton
class SsdpDiscoveryService @Inject constructor() {

    fun discover(timeoutMillis: Int = 4000): Flow<SsdpDevice> = callbackFlow {
        val socket = DatagramSocket()
        socket.soTimeout = timeoutMillis
        socket.broadcast = true

        val searchTarget = "ssdp:all"
        val message = buildString {
            append("M-SEARCH * HTTP/1.1\r\n")
            append("HOST: 239.255.255.250:1900\r\n")
            append("MAN: \"ssdp:discover\"\r\n")
            append("MX: 3\r\n")
            append("ST: $searchTarget\r\n\r\n")
        }
        val data = message.toByteArray()
        val multicastAddress = InetAddress.getByName("239.255.255.250")
        val sendPacket = DatagramPacket(data, data.size, InetSocketAddress(multicastAddress, 1900))

        try {
            socket.send(sendPacket)
            val buffer = ByteArray(2048)
            val deadline = System.currentTimeMillis() + timeoutMillis
            while (System.currentTimeMillis() < deadline) {
                try {
                    val receivePacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(receivePacket)
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    val headers = parseHeaders(response)
                    trySend(
                        SsdpDevice(
                            location = headers["location"] ?: continue,
                            server = headers["server"],
                            usn = headers["usn"],
                            address = receivePacket.address.hostAddress ?: ""
                        )
                    )
                } catch (timeout: Exception) {
                    break
                }
            }
        } finally {
            socket.close()
        }
        awaitClose { socket.close() }
    }.flowOn(Dispatchers.IO)

    private fun parseHeaders(raw: String): Map<String, String> =
        raw.lineSequence()
            .drop(1)
            .mapNotNull { line ->
                val idx = line.indexOf(':')
                if (idx <= 0) null else line.substring(0, idx).trim().lowercase() to line.substring(idx + 1).trim()
            }
            .toMap()
}
