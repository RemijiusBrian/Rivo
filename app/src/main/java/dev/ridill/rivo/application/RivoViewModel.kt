package dev.ridill.rivo.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class RivoViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiverService: ReceiverService
) : ViewModel() {
    private val preferences = preferencesManager.preferences
    val showWelcomeFlow = preferences.map { it.showAppWelcomeFlow }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, true)
    val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    val dynamicThemeEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    val showSplashScreen = MutableStateFlow(true)

    init {
        toggleSplashScreenVisibility()
        collectTransactionAutoAdd()
    }

    private fun toggleSplashScreenVisibility() = viewModelScope.launch {
        showWelcomeFlow.collect {
            delay(1.seconds)
            showSplashScreen.update { false }
        }
    }

    private fun collectTransactionAutoAdd() = viewModelScope.launch {
        preferences.map { it.autoAddTransactionEnabled }
            .collectLatest { receiverService.toggleSmsReceiver(it) }
    }

    fun onSmsPermissionCheck(granted: Boolean) = viewModelScope.launch {
        preferencesManager.updateAutoAddTransactionEnabled(granted)
    }

    fun onNotificationPermissionCheck(granted: Boolean) {
        receiverService.toggleNotificationActionReceivers(granted)
    }
}