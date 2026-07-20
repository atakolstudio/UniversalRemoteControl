package com.atakolstudio.universalremote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import com.atakolstudio.universalremote.data.local.entity.IrCodeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class IrTransmitResult {
    data object Sent : IrTransmitResult()
    data object NoBlaster : IrTransmitResult()
    data class Failed(val reason: String) : IrTransmitResult()
}

/**
 * Thin wrapper around [ConsumerIrManager]. Handles the "no IR blaster on this device"
 * case explicitly, since most modern phones (post ~2018) no longer ship one.
 */
@Singleton
class IrController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val irManager: ConsumerIrManager? by lazy {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    }

    fun hasIrBlaster(): Boolean = irManager?.hasIrEmitter() == true

    fun getSupportedCarrierFrequencies(): IntArray {
        val manager = irManager ?: return IntArray(0)
        return manager.carrierFrequencies?.map { it.minFrequency }?.toIntArray() ?: IntArray(0)
    }

    /** Transmits a code stored as hex (protocol-encoded) by converting it to an NEC-style pulse pattern. */
    fun transmit(code: IrCodeEntity): IrTransmitResult {
        val manager = irManager ?: return IrTransmitResult.NoBlaster
        if (!manager.hasIrEmitter()) return IrTransmitResult.NoBlaster

        return try {
            val pattern = code.rawPattern?.let(::parseRawPattern)
                ?: code.hexCode?.let { IrProtocolEncoder.encode(code.protocol, it) }
                ?: return IrTransmitResult.Failed("Kod bulunamadı")
            manager.transmit(code.carrierFrequencyHz, pattern)
            IrTransmitResult.Sent
        } catch (e: Exception) {
            IrTransmitResult.Failed(e.message ?: "Bilinmeyen hata")
        }
    }

    private fun parseRawPattern(raw: String): IntArray =
        raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toIntArray()
}
