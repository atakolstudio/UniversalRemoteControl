package com.atakolstudio.universalremote.ui.screens.remote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.FavoriteEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.repository.CommandOutcome
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RemoteEvent {
    data class Feedback(val messageResKey: String) : RemoteEvent()
}

data class RemoteUiState(
    val device: DeviceEntity? = null,
    val favoriteFunctions: Set<RemoteFunction> = emptySet()
)

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val repository: RemoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deviceId: Long = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RemoteEvent>()
    val events: SharedFlow<RemoteEvent> = _events

    init {
        combine(
            repository.observeDevice(deviceId),
            repository.observeFavorites()
        ) { device, favorites ->
            RemoteUiState(
                device = device,
                favoriteFunctions = favorites.filter { it.deviceId == deviceId }.map { it.function }.toSet()
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun send(function: RemoteFunction) {
        val device = _uiState.value.device ?: return
        viewModelScope.launch {
            when (val outcome = repository.sendCommand(device, function)) {
                is CommandOutcome.Success -> _events.emit(RemoteEvent.Feedback("sent:${function.name}"))
                is CommandOutcome.NoIrHardware -> _events.emit(RemoteEvent.Feedback("no_ir"))
                is CommandOutcome.Error -> _events.emit(RemoteEvent.Feedback("error:${outcome.message}"))
            }
        }
    }

    fun toggleFavorite(function: RemoteFunction, existing: FavoriteEntity?) {
        viewModelScope.launch {
            repository.toggleFavorite(deviceId, function, existing)
        }
    }

    fun saveManualIrCode(function: RemoteFunction, protocol: com.atakolstudio.universalremote.data.local.entity.IrProtocol, hex: String) {
        viewModelScope.launch {
            repository.saveManualIrCode(deviceId, function, protocol, hex)
            _events.emit(RemoteEvent.Feedback("sent:code_saved"))
        }
    }
}
