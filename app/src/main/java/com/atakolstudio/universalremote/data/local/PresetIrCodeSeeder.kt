package com.atakolstudio.universalremote.data.local

import android.content.Context
import com.atakolstudio.universalremote.data.local.dao.IrCodeDao
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.IrCodeEntity
import com.atakolstudio.universalremote.data.local.entity.IrProtocol
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.model.PresetIrCodeDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads app/src/main/assets/preset_ir_codes.json into Room the first time the app runs
 * (or whenever the table is empty), so brand presets are available offline.
 *
 * NOTE: the bundled hex codes are representative NEC/Sony/Samsung-shaped placeholders
 * generated for scaffolding purposes. Swap `assets/preset_ir_codes.json` for a verified
 * manufacturer code database before shipping to production - IR command tables are
 * device/model-specific and there is no universally "correct" code per brand.
 */
@Singleton
class PresetIrCodeSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val irCodeDao: IrCodeDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun seedIfNeeded() {
        if (irCodeDao.count() > 0) return
        val json = context.assets.open("preset_ir_codes.json").bufferedReader().use(BufferedReader::readText)
        val listType = Types.newParameterizedType(List::class.java, PresetIrCodeDto::class.java)
        val adapter = moshi.adapter<List<PresetIrCodeDto>>(listType)
        val dtos = adapter.fromJson(json).orEmpty()

        val entities = dtos.mapNotNull { dto ->
            runCatching {
                IrCodeEntity(
                    brand = dto.brand,
                    category = DeviceCategory.valueOf(dto.category),
                    function = RemoteFunction.valueOf(dto.function),
                    protocol = IrProtocol.valueOf(dto.protocol),
                    hexCode = dto.hexCode,
                    carrierFrequencyHz = dto.carrierFrequencyHz,
                    isUserLearned = false,
                    deviceId = null
                )
            }.getOrNull()
        }
        irCodeDao.insertAll(entities)
    }
}
