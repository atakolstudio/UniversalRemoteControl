package com.atakolstudio.universalremote

import android.app.Application
import com.atakolstudio.universalremote.data.local.PresetIrCodeSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point. Hilt generates the DI container here, and the bundled
 * preset IR code table is seeded into Room on first process start.
 */
@HiltAndroidApp
class UniversalRemoteApp : Application() {

    @Inject lateinit var presetIrCodeSeeder: PresetIrCodeSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { presetIrCodeSeeder.seedIfNeeded() }
    }
}
