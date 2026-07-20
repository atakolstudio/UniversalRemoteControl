package com.atakolstudio.universalremote.util

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Quick Settings tile that sends POWER to whichever device was used most recently,
 * so the most common single action (turning something on/off) is reachable without
 * opening the app. Hilt supports field injection into TileService via @AndroidEntryPoint.
 */
@AndroidEntryPoint
class QuickMacroTileService : TileService() {

    @Inject lateinit var repository: RemoteRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onClick() {
        super.onClick()
        scope.launch {
            val device = repository.observeDevices().first().firstOrNull()
            if (device != null) {
                repository.sendCommand(device, RemoteFunction.POWER)
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            val device = repository.observeDevices().first().firstOrNull()
            qsTile?.apply {
                label = device?.name ?: "Universal Remote"
                state = if (device != null) Tile.STATE_ACTIVE else Tile.STATE_UNAVAILABLE
                updateTile()
            }
        }
    }
}
