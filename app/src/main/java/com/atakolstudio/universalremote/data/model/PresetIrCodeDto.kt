package com.atakolstudio.universalremote.data.model

import com.squareup.moshi.JsonClass

/**
 * Mirrors the shape of assets/preset_ir_codes.json. Kept separate from the Room entity
 * so the on-disk JSON schema can evolve independently of the database schema.
 */
@JsonClass(generateAdapter = true)
data class PresetIrCodeDto(
    val brand: String,
    val category: String,
    val function: String,
    val protocol: String,
    val hexCode: String? = null,
    val rawPattern: String? = null,
    val carrierFrequencyHz: Int = 38000,
    val sourceModel: String? = null
)
