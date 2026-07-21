package com.atakolstudio.universalremote.ui.screens.macro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atakolstudio.universalremote.R
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.ui.screens.remote.RemoteLayouts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroScreen(
    onBack: () -> Unit,
    viewModel: MacroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.macros_title)) },
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
            Text(stringResource(R.string.macros_title), style = MaterialTheme.typography.titleLarge)
            state.macros.forEach { macro ->
                ListItem(
                    headlineContent = { Text(macro.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.runMacro(macro.id) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.run_macro))
                        }
                    }
                )
                HorizontalDivider()
            }

            Text(stringResource(R.string.create_macro), style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = state.newMacroName,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.macro_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            state.pendingSteps.forEachIndexed { index, step ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("${index + 1}. ${step.deviceName} → ${step.function.name}", modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeStep(index) }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }
            }

            Text(stringResource(R.string.add_step), style = MaterialTheme.typography.titleMedium)
            state.devices.forEach { device ->
                Text(device.name, style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RemoteLayouts.buttonsFor(device.category)) { function: RemoteFunction ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.addStep(device, function) },
                            label = { Text(function.name) }
                        )
                    }
                }
            }

            Button(
                onClick = viewModel::saveMacro,
                enabled = state.newMacroName.isNotBlank() && state.pendingSteps.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
