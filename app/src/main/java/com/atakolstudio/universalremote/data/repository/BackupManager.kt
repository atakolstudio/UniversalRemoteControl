package com.atakolstudio.universalremote.data.repository

import com.atakolstudio.universalremote.data.local.dao.DeviceDao
import com.atakolstudio.universalremote.data.local.dao.MacroDao
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.MacroEntity
import com.atakolstudio.universalremote.data.local.entity.MacroStepEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val version: Int = 1,
    val devices: List<DeviceEntity>,
    val macros: List<MacroEntity>,
    val macroSteps: List<MacroStepEntity>
)

/**
 * Exports/imports the user's devices and macros as a single JSON document, so people
 * can move their setup between phones or keep a manual backup (Settings > Backup).
 */
@Singleton
class BackupManager @Inject constructor(
    private val deviceDao: DeviceDao,
    private val macroDao: MacroDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BackupPayload::class.java).indent("  ")

    suspend fun exportToJson(): String {
        val devices = deviceDao.observeAll().first()
        val macros = macroDao.observeAll().first()
        val steps = macros.flatMap { macroDao.getSteps(it.id) }
        return adapter.toJson(BackupPayload(devices = devices, macros = macros, macroSteps = steps))
    }

    /** Restores a previously exported backup. Existing data is not cleared automatically. */
    suspend fun importFromJson(json: String): Result<Unit> = runCatching {
        val payload = adapter.fromJson(json) ?: error("Geçersiz yedek dosyası")
        val idRemap = mutableMapOf<Long, Long>()
        payload.devices.forEach { device ->
            val newId = deviceDao.upsert(device.copy(id = 0))
            idRemap[device.id] = newId
        }
        payload.macros.forEach { macro ->
            val steps = payload.macroSteps
                .filter { it.macroId == macro.id }
                .mapNotNull { step ->
                    val remappedDeviceId = idRemap[step.deviceId] ?: return@mapNotNull null
                    step.copy(id = 0, macroId = 0, deviceId = remappedDeviceId)
                }
            macroDao.createWithSteps(macro.copy(id = 0), steps)
        }
    }
}
