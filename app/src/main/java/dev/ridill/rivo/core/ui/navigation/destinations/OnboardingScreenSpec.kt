package dev.ridill.rivo.core.ui.navigation.destinations

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberPermissionState
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.util.restartApplication
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.onboarding.presentation.OnboardingScreen
import dev.ridill.rivo.onboarding.presentation.OnboardingViewModel

data object OnboardingScreenSpec : ScreenSpec {
    override val route: String = "welcome_flow"
    override val labelRes: Int = R.string.destination_welcome_flow

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: OnboardingViewModel = hiltViewModel(navBackStackEntry)
        val pagerState = rememberPagerState(
            pageCount = { OnboardingPage.entries.size }
        )
        val availableBackup by viewModel.availableBackup.collectAsStateWithLifecycle()
        val restoreStatusText by viewModel.restoreStatusText.collectAsStateWithLifecycle(null)
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val currency by viewModel.currency.collectAsStateWithLifecycle(initialValue = LocaleUtil.defaultCurrency)
        val budgetInput = viewModel.budgetInput.collectAsStateWithLifecycle()
        val showEncryptionPasswordInput by viewModel.showEncryptionPasswordInput.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val notificationPermissionLauncher = if (BuildUtil.isNotificationRuntimePermissionNeeded())
            rememberPermissionState(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onPermissionResult = viewModel::onNotificationPermissionResponse
            )
        else null

        val signInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = viewModel::onSignInResult
        )

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is OnboardingViewModel.OnboardingEvent.NavigateToPage -> {
                    if (!pagerState.isScrollInProgress)
                        pagerState.animateScrollToPage(event.page.ordinal)
                }

                OnboardingViewModel.OnboardingEvent.LaunchNotificationPermissionRequest -> {
                    notificationPermissionLauncher?.launchRequest()
                }

                is OnboardingViewModel.OnboardingEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                OnboardingViewModel.OnboardingEvent.OnboardingConcluded -> {
                    navController.navigate(DashboardScreenSpec.route) {
                        popUpTo(route) {
                            inclusive = true
                        }
                    }
                }

                is OnboardingViewModel.OnboardingEvent.LaunchGoogleSignIn -> {
                    signInLauncher.launch(event.intent)
                }

                OnboardingViewModel.OnboardingEvent.RestartApplication -> {
                    context.restartApplication()
                }
            }
        }

        OnboardingScreen(
            snackbarController = snackbarController,
            pagerState = pagerState,
            restoreStatusText = restoreStatusText,
            isLoading = isLoading,
            availableBackup = availableBackup,
            showEncryptionPasswordInput = showEncryptionPasswordInput,
            currency = currency,
            budgetInput = { budgetInput.value },
            actions = viewModel
        )
    }
}