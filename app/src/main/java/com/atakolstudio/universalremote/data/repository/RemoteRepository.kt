package com.atakolstudio.universalremote.data.repository

import com.atakolstudio.universalremote.data.local.dao.DeviceDao
import com.atakolstudio.universalremote.data.local.dao.FavoriteDao
import com.atakolstudio.universalremote.data.local.dao.IrCodeDao
import com.atakolstudio.universalremote.data.local.dao.MacroDao
import com.atakolstudio.universalremote.data.local.dao.MacroWithSteps
import com.atakolstudio.universalremote.data.local.entity.ConnectionType
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.FavoriteEntity
import com.atakolstudio.universalremote.data.local.entity.MacroEntity
import com.atakolstudio.universalremote.data.local.entity.MacroStepEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.ir.IrController
import com.atakolstudio.universalremote.ir.IrTransmitResult
import com.atakolstudio.universalremote.wifi.WifiCommandResult
import com.atakolstudio.universalremote.wifi.WifiCommandRouter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class CommandOutcome {
    data object Success : CommandOutcome()
    data object NoIrHardware : CommandOutcome()
    data class Error(val message: String) : CommandOutcome()
}

@Singleton
class RemoteRepository @Inject constructor(
    private val deviceDao: DeviceDao,
    private val irCodeDao: IrCodeDao,
    private val macroDao: MacroDao,
    private val favoriteDao: FavoriteDao,
    private val irController: IrController,
    private val wifiCommandRouter: WifiCommandRouter
) {
    fun observeDevices(): Flow<List<DeviceEntity>> = deviceDao.observeAll()
    fun observeDevice(id: Long): Flow<DeviceEntity?> = deviceDao.observeById(id)
    fun observeFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.observeAll()
    fun observeMacros(): Flow<List<MacroEntity>> = macroDao.observeAll()

    fun hasIrBlaster(): Boolean = irController.hasIrBlaster()

    suspend fun saveDevice(device: DeviceEntity): Long = deviceDao.upsert(device)
    suspend fun deleteDevice(device: DeviceEntity) = deviceDao.delete(device)

    suspend fun toggleFavorite(deviceId: Long, function: RemoteFunction, favorite: FavoriteEntity?) {
        if (favorite != null) {
            favoriteDao.delete(favorite)
        } else {
            favoriteDao.insert(FavoriteEntity(deviceId = deviceId, function = function))
        }
    }

    suspend fun saveManualIrCode(
        deviceId: Long,
        function: RemoteFunction,
        protocol: com.atakolstudio.universalremote.data.local.entity.IrProtocol,
        hexCode: String
    ) {
        val device = deviceDao.getById(deviceId) ?: return
        irCodeDao.insert(
            com.atakolstudio.universalremote.data.local.entity.IrCodeEntity(
                brand = device.brand,
                category = device.category,
                function = function,
                protocol = protocol,
                hexCode = hexCode,
                isUserLearned = true,
                deviceId = deviceId
            )
        )
    }

    suspend fun createMacro(name: String, iconKey: String, steps: List<MacroStepEntity>): Long =
        macroDao.createWithSteps(MacroEntity(name = name, iconKey = iconKey), steps)

    suspend fun runMacro(macroId: Long) {
        val steps = macroDao.getSteps(macroId)
        for (step in steps) {
            val device = deviceDao.getById(step.deviceId) ?: continue
            sendCommand(device, step.function)
            delay(step.delayAfterMillis)
        }
    }

    /**
     * Sends [function] to [device], choosing IR or WiFi transport based on the
     * device's connectionType. HYBRID devices prefer WiFi when configured, falling
     * back to IR (useful for e.g. power-on, which some TVs only accept via IR/CEC).
     */
    suspend fun sendCommand(device: DeviceEntity, function: RemoteFunction): CommandOutcome {
        deviceDao.touchLastUsed(device.id)

        val useWifi = device.connectionType == ConnectionType.WIFI ||
            (device.connectionType == ConnectionType.HYBRID && device.ipAddress != null)

        return if (useWifi) {
            when (val result = wifiCommandRouter.send(device, function)) {
                is WifiCommandResult.Sent -> CommandOutcome.Success
                is WifiCommandResult.Failed ->
                    if (device.connectionType == ConnectionType.HYBRID) {
                        sendIr(device, function)
                    } else {
                        CommandOutcome.Error(result.reason)
                    }
            }
        } else {
            sendIr(device, function)
        }
    }

    private suspend fun sendIr(device: DeviceEntity, function: RemoteFunction): CommandOutcome {
        val learned = irCodeDao.getLearnedCode(device.id, function)
        val preset = learned ?: irCodeDao.getPresetCode(device.brand, device.category, function)
        val code = preset ?: return CommandOutcome.Error("Bu fonksiyon için kayıtlı IR kodu yok")

        return when (val result = irController.transmit(code)) {
            is IrTransmitResult.Sent -> CommandOutcome.Success
            is IrTransmitResult.NoBlaster -> CommandOutcome.NoIrHardware
            is IrTransmitResult.Failed -> CommandOutcome.Error(result.reason)
        }
    }
}
