package com.atakolstudio.universalremote.ui.screens.adddevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atakolstudio.universalremote.R
import com.atakolstudio.universalremote.data.local.entity.ConnectionType
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.WifiProtocol
import com.atakolstudio.universalremote.data.model.BrandCatalog

@Composable
fun AddDeviceScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: AddDeviceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.savedDeviceId) {
        state.savedDeviceId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_device_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.device_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.select_category), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DeviceCategory.entries) { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(category.name.replace('_', ' ')) }
                    )
                }
            }

            Text(stringResource(R.string.select_brand), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BrandCatalog.brandsFor(state.category)) { brand ->
                    FilterChip(
                        selected = state.brand == brand,
                        onClick = { viewModel.updateBrand(brand) },
                        label = { Text(brand) }
                    )
                }
            }

            OutlinedTextField(
                value = state.model,
                onValueChange = viewModel::updateModel,
                label = { Text(stringResource(R.string.select_model)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.connection_type), style = MaterialTheme.typography.titleMedium)
            if (!state.hasIrBlaster) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        stringResource(R.string.ir_no_blaster_warning),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    if (state.hasIrBlaster) ConnectionType.entries else listOf(ConnectionType.WIFI)
                ) { type ->
                    FilterChip(
                        selected = state.connectionType == type,
                        onClick = { viewModel.updateConnectionType(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            if (state.connectionType != ConnectionType.IR) {
                Text(stringResource(R.string.select_category), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        listOf(
                            WifiProtocol.SAMSUNG_SMARTVIEW,
                            WifiProtocol.LG_WEBOS,
                            WifiProtocol.XIAOMI_MIIO,
                            WifiProtocol.GENERIC_HTTP,
                            WifiProtocol.UPNP
                        )
                    ) { protocol ->
                        FilterChip(
                            selected = state.wifiProtocol == protocol,
                            onClick = { viewModel.updateWifiProtocol(protocol) },
                            label = { Text(protocol.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = state.ipAddress,
                    onValueChange = viewModel::updateIp,
                    label = { Text(stringResource(R.string.wifi_ip_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.macAddress,
                    onValueChange = viewModel::updateMac,
                    label = { Text(stringResource(R.string.wifi_mac_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
