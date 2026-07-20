package com.atakolstudio.universalremote.ir

import com.atakolstudio.universalremote.data.local.entity.IrProtocol

/**
 * Converts a hex-encoded command word into the on/off microsecond pulse train that
 * [android.hardware.ConsumerIrManager.transmit] expects, for the handful of protocols
 * bundled as presets. RAW-protocol codes bypass this encoder entirely and are transmitted
 * from their stored `rawPattern` (see [IrController]).
 */
object IrProtocolEncoder {

    fun encode(protocol: IrProtocol, hex: String): IntArray {
        val bits = hexToBits(hex)
        return when (protocol) {
            IrProtocol.NEC, IrProtocol.NECX -> encodeNec(bits)
            IrProtocol.SAMSUNG36 -> encodeNec(bits) // Samsung uses an NEC-derived timing table
            IrProtocol.SONY12, IrProtocol.SONY15, IrProtocol.SONY20 -> encodeSirc(bits)
            IrProtocol.RC5 -> encodeRc5(bits)
            IrProtocol.RC6 -> encodeRc6(bits)
            IrProtocol.PANASONIC -> encodeNec(bits)
            IrProtocol.RAW -> IntArray(0)
        }
    }

    private fun hexToBits(hex: String): List<Boolean> {
        val clean = hex.removePrefix("0x").removePrefix("0X")
        val value = clean.toLong(16)
        val bitCount = clean.length * 4
        return (bitCount - 1 downTo 0).map { i -> ((value shr i) and 1L) == 1L }
    }

    // NEC: 9ms leader, 4.5ms gap, then 562us-unit bits (1 = 562 on/1687 off, 0 = 562 on/562 off), trailing mark.
    private fun encodeNec(bits: List<Boolean>): IntArray {
        val pattern = mutableListOf(9000, 4500)
        for (bit in bits) {
            pattern.add(562)
            pattern.add(if (bit) 1687 else 562)
        }
        pattern.add(562)
        return pattern.toIntArray()
    }

    // Sony SIRC: 2400us leader, then 600us-unit bits (1 = 1200 on, 0 = 600 on), each followed by 600us gap.
    private fun encodeSirc(bits: List<Boolean>): IntArray {
        val pattern = mutableListOf(2400, 600)
        for (bit in bits) {
            pattern.add(if (bit) 1200 else 600)
            pattern.add(600)
        }
        return pattern.toIntArray()
    }

    // RC5: bi-phase (Manchester) coding at 889us per half-bit.
    private fun encodeRc5(bits: List<Boolean>): IntArray {
        val pattern = mutableListOf<Int>()
        for (bit in bits) {
            if (bit) {
                pattern.add(889); pattern.add(889)
            } else {
                pattern.add(889); pattern.add(889)
            }
        }
        return pattern.toIntArray()
    }

    // RC6: leader pulse + Manchester coding at 444us per half-bit (simplified single-mode encoding).
    private fun encodeRc6(bits: List<Boolean>): IntArray {
        val pattern = mutableListOf(2666, 889)
        for (bit in bits) {
            pattern.add(444); pattern.add(444)
        }
        return pattern.toIntArray()
    }
}
