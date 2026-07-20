package com.atakolstudio.universalremote.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val devices: List<DeviceEntity> = emptyList(),
    val hasIrBlaster: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RemoteRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeDevices()
        .let { devicesFlow ->
            kotlinx.coroutines.flow.combine(
                devicesFlow,
                kotlinx.coroutines.flow.flowOf(repository.hasIrBlaster())
            ) { devices, hasIr -> DashboardUiState(devices, hasIr) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch { repository.deleteDevice(device) }
    }
}
