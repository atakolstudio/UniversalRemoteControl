package com.atakolstudio.universalremote.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atakolstudio.universalremote.R
import com.atakolstudio.universalremote.data.local.entity.ConnectionType
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity

@Composable
fun DashboardScreen(
    onOpenDevice: (Long) -> Unit,
    onAddDevice: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMacros: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResourceCompat(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = onOpenMacros) {
                        Icon(Icons.Filled.Router, contentDescription = stringResourceCompat(R.string.macros_title))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResourceCompat(R.string.settings_title))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddDevice,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResourceCompat(R.string.add_device_fab)) }
            )
        }
    ) { padding ->
        if (state.devices.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResourceCompat(R.string.dashboard_empty),
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(state.devices, key = { it.id }) { device ->
                    DeviceCard(device = device, onClick = { onOpenDevice(device.id) })
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(device: DeviceEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "${device.name}, ${device.brand}" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = categoryIcon(device.category),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(device.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                "${device.brand}${device.model?.let { " · $it" } ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                connectionLabel(device.connectionType),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun categoryIcon(category: DeviceCategory) = when (category) {
    DeviceCategory.TV -> Icons.Filled.Tv
    else -> Icons.Filled.DeviceUnknown
}

@Composable
private fun connectionLabel(type: ConnectionType): String = when (type) {
    ConnectionType.IR -> stringResourceCompat(R.string.device_type_ir)
    ConnectionType.WIFI -> stringResourceCompat(R.string.device_type_wifi)
    ConnectionType.HYBRID -> stringResourceCompat(R.string.device_type_hybrid)
}

/** Small wrapper so previews / non-Activity contexts don't crash on stringResource. */
@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)
