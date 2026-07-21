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
 * Loads bundled IR code data into Room:
 *
 * 1. `assets/preset_ir_codes.json` - representative NEC/Sony/Samsung-*shaped* placeholder
 *    codes for 20+ brands across all device categories, seeded once (only if the table
 *    is empty). Generated for scaffolding purposes - NOT verified against real hardware.
 *
 * 2. `assets/lirc_ir_codes.json` - real IR signals for TVs from 11 major brands
 *    (Samsung, LG, Sony, Panasonic, JVC, Hitachi, Thomson, Sharp, Philips, Toshiba,
 *    Vestel), extracted from the LIRC remotes database (lirc-remotes / lirc.sourceforge.net,
 *    GPL-licensed project) and converted from their Pronto Hex (CCF) representation into
 *    the raw on/off microsecond pattern ConsumerIrManager.transmit() expects - see
 *    assets/lirc_sources/ for the original per-brand XML files this was derived from, and
 *    the conversion math in the project history. These override the matching
 *    (brand, category, function) placeholder rows every time the app starts, so shipping
 *    updated real-world data later doesn't require a reinstall. Coverage is partial: only
 *    common TV remote functions for one sampled model per brand - not exhaustive per model,
 *    and other categories (AC, STB, etc.) still fall back to the placeholder table.
 */
@Singleton
class PresetIrCodeSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val irCodeDao: IrCodeDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, PresetIrCodeDto::class.java)
    private val adapter = moshi.adapter<List<PresetIrCodeDto>>(listType)

    suspend fun seedIfNeeded() {
        if (irCodeDao.count() == 0) {
            seedFromAsset("preset_ir_codes.json", isRealWorld = false)
        }
        // Real-world codes are refreshed unconditionally (small dataset, cheap) so future
        // updates to the bundled LIRC data take effect without a fresh install.
        seedFromAsset("lirc_ir_codes.json", isRealWorld = true)
    }

    private suspend fun seedFromAsset(assetName: String, isRealWorld: Boolean) {
        val json = runCatching {
            context.assets.open(assetName).bufferedReader().use(BufferedReader::readText)
        }.getOrNull() ?: return

        val dtos = adapter.fromJson(json).orEmpty()

        val entities = dtos.mapNotNull { dto ->
            runCatching {
                IrCodeEntity(
                    brand = dto.brand,
                    category = DeviceCategory.valueOf(dto.category),
                    function = RemoteFunction.valueOf(dto.function),
                    protocol = IrProtocol.valueOf(dto.protocol),
                    hexCode = dto.hexCode,
                    rawPattern = dto.rawPattern,
                    carrierFrequencyHz = dto.carrierFrequencyHz,
                    isUserLearned = false,
                    deviceId = null
                )
            }.getOrNull()
        }

        if (isRealWorld) {
            // Delete-then-insert per (brand, category, function) so real data always wins
            // over a placeholder without needing a unique DB index (NULL deviceId columns
            // don't collide reliably in a plain SQLite UNIQUE constraint).
            entities.forEach { irCodeDao.deletePreset(it.brand, it.category, it.function) }
        }
        irCodeDao.insertAll(entities)
    }
}
