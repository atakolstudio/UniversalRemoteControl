package com.atakolstudio.universalremote.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atakolstudio.universalremote.data.local.UserPreferences
import com.atakolstudio.universalremote.data.repository.BackupManager
import com.atakolstudio.universalremote.ui.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: String = "tr",
    val debugMode: Boolean = false
)

sealed class SettingsEvent {
    data class ExportReady(val json: String) : SettingsEvent()
    data class ImportResult(val success: Boolean, val message: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val backupManager: BackupManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.themeMode, preferences.language, preferences.debugMode
    ) { theme, lang, debug -> SettingsUiState(theme, lang, debug) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch { preferences.setThemeMode(mode) }
    fun setLanguage(code: String) = viewModelScope.launch {
        preferences.setLanguage(code)
        // Per-app language switch (AndroidX AppCompat); on API 33+ this is backed by the
        // system LocaleManager, on older versions AppCompat recreates activities itself.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }
    fun setDebugMode(enabled: Boolean) = viewModelScope.launch { preferences.setDebugMode(enabled) }

    fun exportBackup() = viewModelScope.launch {
        val json = backupManager.exportToJson()
        _events.emit(SettingsEvent.ExportReady(json))
    }

    fun importBackup(json: String) = viewModelScope.launch {
        val result = backupManager.importFromJson(json)
        _events.emit(
            SettingsEvent.ImportResult(
                success = result.isSuccess,
                message = result.exceptionOrNull()?.message ?: "Geri yükleme tamamlandı"
            )
        )
    }
}
