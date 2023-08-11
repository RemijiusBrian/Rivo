package dev.ridill.mym.application

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.appdistribution.AppDistributionRelease
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.notification.AppUpdateNotificationHelper
import dev.ridill.mym.core.domain.service.AppDistributionService
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.domain.util.tryOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MYMViewModel @Inject constructor(
    preferencesManager: PreferencesManager,
    private val savedStateHandle: SavedStateHandle,
    private val appDistributionService: AppDistributionService,
    private val notificationHelper: AppUpdateNotificationHelper
) : ViewModel() {
    private val preferences = preferencesManager.preferences
    val showWelcomeFlow = preferences.map { it.showAppWelcomeFlow }
        .distinctUntilChanged()
    val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    val dynamicThemeEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    val showTesterSignInPrompt = savedStateHandle
        .getStateFlow(SHOW_TESTER_SIGN_IN_PROMPT, false)

    private val _newAvailableRelease = MutableStateFlow<AppDistributionRelease?>(null)
    val newAvailableRelease get() = _newAvailableRelease.asStateFlow()

    init {
        onInit()
    }

    private fun onInit() {
        if (!BuildUtil.isBuildFlavourInternal()) return

        viewModelScope.launch {
            if (showWelcomeFlow.first()) return@launch

            if (appDistributionService.isTesterSignedIn) {
                checkForNewRelease()
            } else {
                savedStateHandle[SHOW_TESTER_SIGN_IN_PROMPT] = true
            }
        }
    }

    private fun checkForNewRelease() = viewModelScope.launch {
        val release = tryOrNull { appDistributionService.checkForNewRelease() }
            ?: return@launch

        if (release.versionCode <= BuildUtil.versionCode) return@launch

        _newAvailableRelease.update { release }
    }

    fun onSignInPromptConfirm() = viewModelScope.launch {
        val response = appDistributionService.signInTester()
        if (response.isErrorText) return@launch

        savedStateHandle[SHOW_TESTER_SIGN_IN_PROMPT] = false
        checkForNewRelease()
    }

    fun onAppUpdateDismiss() {
        _newAvailableRelease.update { null }
    }

    fun onAppUpdateConfirm() = viewModelScope.launch {
        val notificationId = Random.nextInt()
        try {
            _newAvailableRelease.update { null }
            notificationHelper.postUpdateNotification(notificationId)
            appDistributionService.updateApp()
            notificationHelper.dismissNotification(notificationId)
        } catch (t: Throwable) {
            notificationHelper.postUpdateFailedNotification(notificationId)
            t.printStackTrace()
        }
    }
}

private const val SHOW_TESTER_SIGN_IN_PROMPT = "SHOW_TESTER_SIGN_IN_PROMPT"