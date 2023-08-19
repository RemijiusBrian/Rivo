package dev.ridill.mym.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.ReceiverService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MYMViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiverService: ReceiverService
) : ViewModel() {
    private val preferences = preferencesManager.preferences
    val showWelcomeFlow = preferences.map { it.showAppWelcomeFlow }
        .distinctUntilChanged()
    val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    val dynamicThemeEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    val showSplashScreen = MutableStateFlow(true)

    init {
        toggleSplashScreenVisibility()
        collectExpenseAutoAdd()
    }

    private fun toggleSplashScreenVisibility() = viewModelScope.launch {
        showWelcomeFlow.collect {
            delay(100.milliseconds)
            showSplashScreen.update { false }
        }
    }

    private fun collectExpenseAutoAdd() = viewModelScope.launch {
        preferences.map { it.autoAddExpenseEnabled }
            .collectLatest { receiverService.toggleSmsReceiver(it) }
    }

    fun onSmsPermissionCheck(granted: Boolean) = viewModelScope.launch {
        preferencesManager.updateAutoAddExpenseEnabled(granted)
    }
}