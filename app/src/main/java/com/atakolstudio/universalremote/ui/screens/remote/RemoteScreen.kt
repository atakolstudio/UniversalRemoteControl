package com.atakolstudio.universalremote.ui.screens.remote

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atakolstudio.universalremote.R
import com.atakolstudio.universalremote.data.local.entity.IrProtocol
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import kotlinx.coroutines.launch

@Composable
fun RemoteScreen(
    onBack: () -> Unit,
    viewModel: RemoteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is RemoteEvent.Feedback) {
                val message = when {
                    event.messageResKey == "no_ir" -> "IR gönderici bulunamadı"
                    event.messageResKey.startsWith("error:") -> event.messageResKey.removePrefix("error:")
                    else -> "Komut gönderildi"
                }
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        }
    }

    val device = state.device
    var manualCodeTarget by remember { mutableStateOf<RemoteFunction?>(null) }

    manualCodeTarget?.let { function ->
        ManualIrCodeDialog(
            function = function,
            onDismiss = { manualCodeTarget = null },
            onSave = { protocol, hex ->
                viewModel.saveManualIrCode(function, protocol, hex)
                manualCodeTarget = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (device == null) return@Scaffold

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(device.id) {
                    // Swipe up/down anywhere on the remote body = volume up/down,
                    // matching the gesture convention of most universal remote apps.
                    var accumulated = 0f
                    detectVerticalDragGestures(
                        onDragStart = { accumulated = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            accumulated += dragAmount
                            val threshold = 60f
                            if (accumulated <= -threshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.send(RemoteFunction.VOLUME_UP)
                                accumulated = 0f
                            } else if (accumulated >= threshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.send(RemoteFunction.VOLUME_DOWN)
                                accumulated = 0f
                            }
                        }
                    )
                }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(RemoteLayouts.buttonsFor(device.category)) { function ->
                    RemoteButton(
                        function = function,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.send(function)
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            manualCodeTarget = function
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteButton(
    function: RemoteFunction,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(functionLabel(function), textAlign = TextAlign.Center)
    }
}

/**
 * Lets the user assign (or override) a raw hex IR command for one function on this
 * device - the manual-entry counterpart to the (platform-limited) learning flow.
 * See IrLearningHelper for why "point old remote at phone" learning isn't offered.
 */
@Composable
private fun ManualIrCodeDialog(
    function: RemoteFunction,
    onDismiss: () -> Unit,
    onSave: (IrProtocol, String) -> Unit
) {
    var hex by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf(IrProtocol.NEC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manual_ir_code)) },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("${function.name}", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = hex,
                    onValueChange = { hex = it },
                    label = { Text("Hex kod (ör. 0x20DF10EF)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (hex.isNotBlank()) onSave(protocol, hex.trim()) },
                enabled = hex.isNotBlank()
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun functionLabel(function: RemoteFunction): String = when (function) {
    RemoteFunction.POWER -> stringResource(R.string.power)
    RemoteFunction.VOLUME_UP -> "VOL +"
    RemoteFunction.VOLUME_DOWN -> "VOL -"
    RemoteFunction.MUTE -> stringResource(R.string.mute)
    RemoteFunction.CHANNEL_UP -> "CH +"
    RemoteFunction.CHANNEL_DOWN -> "CH -"
    RemoteFunction.INPUT_SOURCE -> "Kaynak"
    RemoteFunction.HOME -> stringResource(R.string.home)
    RemoteFunction.BACK -> stringResource(R.string.back)
    RemoteFunction.MENU -> "Menü"
    RemoteFunction.MODE -> stringResource(R.string.mode)
    RemoteFunction.TEMP_UP -> "Sıcaklık +"
    RemoteFunction.TEMP_DOWN -> "Sıcaklık -"
    RemoteFunction.SWING -> stringResource(R.string.swing)
    RemoteFunction.FAN_SPEED -> stringResource(R.string.fan_speed)
    RemoteFunction.TIMER -> "Zamanlayıcı"
    RemoteFunction.DPAD_UP -> "▲"
    RemoteFunction.DPAD_DOWN -> "▼"
    RemoteFunction.DPAD_LEFT -> "◀"
    RemoteFunction.DPAD_RIGHT -> "▶"
    RemoteFunction.DPAD_OK -> "OK"
    else -> function.name
}
