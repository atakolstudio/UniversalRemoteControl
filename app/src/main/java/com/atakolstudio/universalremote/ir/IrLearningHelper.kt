package com.atakolstudio.universalremote.ir

/**
 * IMPORTANT PLATFORM LIMITATION:
 *
 * Android does not expose a public API to *receive* infrared signals - [android.hardware.ConsumerIrManager]
 * is transmit-only, and no AOSP-level IR receiver framework exists for third-party apps
 * (a handful of OEMs, e.g. some older Xiaomi/Redmi phones, exposed private/vendor-specific
 * IR-receive hooks, but there is nothing standard or reliably available across devices).
 *
 * Because of that, a genuine "point your old remote at the phone and press a button to learn it"
 * flow is not implementable in a portable way. This app instead offers:
 *   1. Preset codes from the bundled brand/category database (see PresetIrCodeSeeder).
 *   2. Manual code entry - the user types a known hex command (from a manufacturer's IR code
 *      sheet, a capture done with dedicated IR-receiver hardware, or a service like a Broadlink/
 *      IRDB export) which is stored as an IrCodeEntity with isUserLearned = true.
 *
 * If you have access to a specific handset's vendor IR-receive API, this is the seam to
 * implement it in: swap the body of [requestLearnedCode] for the vendor call and keep the
 * public suspend signature so the rest of the app (AddDeviceScreen / RemoteScreen) doesn't change.
 */
object IrLearningHelper {
    sealed class LearnResult {
        data class Captured(val rawPattern: IntArray) : LearnResult()
        data object UnsupportedOnThisDevice : LearnResult()
    }

    suspend fun requestLearnedCode(): LearnResult = LearnResult.UnsupportedOnThisDevice
}
