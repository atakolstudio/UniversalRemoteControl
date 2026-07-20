package com.atakolstudio.universalremote.ui.screens.macro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.MacroEntity
import com.atakolstudio.universalremote.data.local.entity.MacroStepEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingStep(val deviceId: Long, val deviceName: String, val function: RemoteFunction)

data class MacroUiState(
    val macros: List<MacroEntity> = emptyList(),
    val devices: List<DeviceEntity> = emptyList(),
    val newMacroName: String = "",
    val pendingSteps: List<PendingStep> = emptyList()
)

@HiltViewModel
class MacroViewModel @Inject constructor(
    private val repository: RemoteRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(Pair("", emptyList<PendingStep>()))

    val uiState: StateFlow<MacroUiState> = combine(
        repository.observeMacros(),
        repository.observeDevices(),
        _formState
    ) { macros, devices, form ->
        MacroUiState(macros, devices, form.first, form.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MacroUiState())

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(first = name)
    }

    fun addStep(device: DeviceEntity, function: RemoteFunction) {
        val steps = _formState.value.second + PendingStep(device.id, device.name, function)
        _formState.value = _formState.value.copy(second = steps)
    }

    fun removeStep(index: Int) {
        val steps = _formState.value.second.toMutableList().also { it.removeAt(index) }
        _formState.value = _formState.value.copy(second = steps)
    }

    fun saveMacro() {
        val (name, steps) = _formState.value
        if (name.isBlank() || steps.isEmpty()) return
        viewModelScope.launch {
            val entities = steps.mapIndexed { index, step ->
                MacroStepEntity(
                    macroId = 0,
                    deviceId = step.deviceId,
                    function = step.function,
                    orderIndex = index
                )
            }
            repository.createMacro(name.trim(), "movie", entities)
            _formState.value = "" to emptyList()
        }
    }

    fun runMacro(macroId: Long) {
        viewModelScope.launch { repository.runMacro(macroId) }
    }
}
