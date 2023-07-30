package dev.ridill.mym.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesManager: PreferencesManager
) : ViewModel(), SettingsActions {

    private val preferences = preferencesManager.preferences
    private val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    private val dynamicThemeEnabled = preferences.map { it.dynamicThemeEnabled }
        .distinctUntilChanged()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)

    val state = combineTuple(
        appTheme,
        dynamicThemeEnabled,
        showAppThemeSelection
    ).map { (
                appTheme,
                dynamicThemeEnabled,
                showAppThemeSelection
            ) ->
        SettingsState(
            appTheme = appTheme,
            dynamicThemeEnabled = dynamicThemeEnabled,
            showAppThemeSelection = showAppThemeSelection
        )
    }.asStateFlow(viewModelScope, SettingsState())

    override fun onAppThemePreferenceClick() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = true
    }

    override fun onAppThemeSelectionDismiss() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = false
    }

    override fun onAppThemeSelectionConfirm(appTheme: AppTheme) {
        viewModelScope.launch {
            savedStateHandle[SHOW_APP_THEME_SELECTION] = false
            preferencesManager.updateAppThem(appTheme)
        }
    }

    override fun onDynamicThemeEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDynamicThemeEnabled(enabled)
        }
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"