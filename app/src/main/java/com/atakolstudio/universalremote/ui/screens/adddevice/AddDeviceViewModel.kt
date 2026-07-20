package com.atakolstudio.universalremote.ui.screens.adddevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atakolstudio.universalremote.data.local.entity.ConnectionType
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.WifiProtocol
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val savedDeviceId: Long? = null
) {
    val canSave: Boolean
        get() = name.isNotBlank() && brand.isNotBlank() &&
            (connectionType == ConnectionType.IR || ipAddress.isNotBlank())
}

@HiltViewModel
class AddDeviceViewModel @Inject constructor(
    private val repository: RemoteRepository
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
