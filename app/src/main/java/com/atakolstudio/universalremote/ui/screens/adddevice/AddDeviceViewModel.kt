package com.atakolstudio.universalremote.ui.screens.adddevice

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atakolstudio.universalremote.data.local.entity.ConnectionType
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.WifiProtocol
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import com.atakolstudio.universalremote.wifi.SsdpDevice
import com.atakolstudio.universalremote.wifi.SsdpDiscoveryService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddDeviceUiState(
    val name: String = "",
    val category: DeviceCategory = DeviceCategory.TV,
    val brand: String = "",
    val model: String = "",
    val connectionType: ConnectionType = ConnectionType.IR,
    val wifiProtocol: WifiProtocol = WifiProtocol.NONE,
    val ipAddress: String = "",
    val macAddress: String = "",
    val hasIrBlaster: Boolean = true,
    val savedDeviceId: Long? = null,
    val isScanning: Boolean = false,
    val discoveredDevices: List<SsdpDevice> = emptyList()
) {
    val canSave: Boolean
        get() = name.isNotBlank() && brand.isNotBlank() &&
            (connectionType == ConnectionType.IR || ipAddress.isNotBlank())
}

@HiltViewModel
class AddDeviceViewModel @Inject constructor(
    private val repository: RemoteRepository,
    private val ssdpDiscoveryService: SsdpDiscoveryService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddDeviceUiState(hasIrBlaster = repository.hasIrBlaster())
    )
    val uiState: StateFlow<AddDeviceUiState> = _uiState.asStateFlow()

    init {
        if (!repository.hasIrBlaster()) {
            _uiState.update { it.copy(connectionType = ConnectionType.WIFI) }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateCategory(value: DeviceCategory) = _uiState.update { it.copy(category = value, brand = "") }
    fun updateBrand(value: String) = _uiState.update { it.copy(brand = value) }
    fun updateModel(value: String) = _uiState.update { it.copy(model = value) }
    fun updateConnectionType(value: ConnectionType) = _uiState.update { it.copy(connectionType = value) }
    fun updateWifiProtocol(value: WifiProtocol) = _uiState.update { it.copy(wifiProtocol = value) }
    fun updateIp(value: String) = _uiState.update { it.copy(ipAddress = value) }
    fun updateMac(value: String) = _uiState.update { it.copy(macAddress = value) }

    /**
     * Scans the local network for SSDP/UPnP-advertising devices (smart TVs, media renderers,
     * etc.) so the user can tap a result instead of typing an IP by hand. Requires
     * CHANGE_WIFI_MULTICAST_STATE, already declared in the manifest.
     */
    fun scanNetwork() {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("urc_ssdp_scan")?.apply { setReferenceCounted(true) }

        _uiState.update { it.copy(isScanning = true, discoveredDevices = emptyList()) }
        viewModelScope.launch {
            multicastLock?.acquire()
            ssdpDiscoveryService.discover(timeoutMillis = 4000)
                .catch { /* swallow: partial results are still useful, surfaced list stays as-is */ }
                .onCompletion {
                    multicastLock?.let { if (it.isHeld) it.release() }
                    _uiState.update { it.copy(isScanning = false) }
                }
                .collect { found ->
                    _uiState.update { state ->
                        if (state.discoveredDevices.any { it.address == found.address }) state
                        else state.copy(discoveredDevices = state.discoveredDevices + found)
                    }
                }
        }
    }

    /** Applies a scan result to the form: fills IP and takes a best-effort guess at the WiFi protocol. */
    fun applyDiscoveredDevice(device: SsdpDevice) {
        val guessedProtocol = when {
            device.server?.contains("Samsung", ignoreCase = true) == true ||
                device.usn?.contains("samsung", ignoreCase = true) == true -> WifiProtocol.SAMSUNG_SMARTVIEW
            device.server?.contains("WebOS", ignoreCase = true) == true ||
                device.server?.contains("LG", ignoreCase = true) == true -> WifiProtocol.LG_WEBOS
            else -> WifiProtocol.UPNP
        }
        _uiState.update {
            it.copy(
                ipAddress = device.address,
                connectionType = ConnectionType.WIFI,
                wifiProtocol = guessedProtocol
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            val id = repository.saveDevice(
                DeviceEntity(
                    name = state.name.trim(),
                    brand = state.brand,
                    model = state.model.ifBlank { null },
                    category = state.category,
                    connectionType = state.connectionType,
                    wifiProtocol = if (state.connectionType == ConnectionType.IR) WifiProtocol.NONE else state.wifiProtocol,
                    ipAddress = state.ipAddress.ifBlank { null },
                    macAddress = state.macAddress.ifBlank { null }
                )
            )
            _uiState.update { it.copy(savedDeviceId = id) }
        }
    }
}
