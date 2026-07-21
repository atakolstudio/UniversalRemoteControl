package com.atakolstudio.universalremote

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atakolstudio.universalremote.data.local.UserPreferences
import com.atakolstudio.universalremote.ui.navigation.RemoteAppNavHost
import com.atakolstudio.universalremote.ui.theme.AppThemeMode
import com.atakolstudio.universalremote.ui.theme.UniversalRemoteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by userPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.SYSTEM
            )

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* no-op: notifications are optional, only used for macro run feedback */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            UniversalRemoteTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RemoteAppNavHost()
                }
            }
        }
    }
}
