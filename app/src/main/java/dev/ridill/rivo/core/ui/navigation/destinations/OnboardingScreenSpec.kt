package dev.ridill.rivo.core.ui.navigation.destinations

import android.Manifest
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.application.EXTRA_RUN_CONFIG_RESTORE
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.ui.authentication.rememberAuthenticationService
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberMultiplePermissionsLauncher
import dev.ridill.rivo.core.ui.components.rememberMultiplePermissionsState
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.util.findActivity
import dev.ridill.rivo.core.ui.util.restartApplication
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.onboarding.presentation.OnboardingScreen
import dev.ridill.rivo.onboarding.presentation.OnboardingViewModel
import kotlinx.coroutines.launch
import java.util.Currency

data object OnboardingScreenSpec : ScreenSpec {
    override val route: String = "onboarding"
    override val labelRes: Int = R.string.destination_onboarding

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
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

        val permissionsState = rememberMultiplePermissionsState(
            permissions = if (BuildUtil.isNotificationRuntimePermissionNeeded())
                listOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECEIVE_SMS)
            else listOf(Manifest.permission.RECEIVE_SMS),
            launcher = rememberMultiplePermissionsLauncher(
                onResult = viewModel::onPermissionsRequestResult
            )
        )

        val credentialService = rememberAuthenticationService(context)
        val currentPage by remember(pagerState) {
            derivedStateOf { pagerState.currentPage }
        }

        LaunchedEffect(currentPage) {
            if (currentPage == OnboardingPage.GOOGLE_SIGN_IN.ordinal)
                viewModel.onAccountPageReached()
        }

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is OnboardingViewModel.OnboardingEvent.NavigateToPage -> {
                    if (!pagerState.isScrollInProgress)
                        pagerState.animateScrollToPage(event.page.ordinal)
                }

                OnboardingViewModel.OnboardingEvent.LaunchNotificationPermissionRequest -> {
                    permissionsState.launchRequest()
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

                OnboardingViewModel.OnboardingEvent.RestartApplication -> {
                    context.restartApplication(
                        editIntent = {
                            putExtra(EXTRA_RUN_CONFIG_RESTORE, true)
                        }
                    )
                }

                is OnboardingViewModel.OnboardingEvent.StartAutoSignInFlow -> {
                    val result = credentialService.startGetCredentialFlow(
                        filterByAuthorizedUsers = event.filterByAuthorizedAccounts,
                        activityContext = context.findActivity()
                    )
                    viewModel.onCredentialResult(result)
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()
        OnboardingScreen(
            snackbarController = snackbarController,
            pagerState = pagerState,
            permissionsState = permissionsState,
            restoreStatusText = restoreStatusText,
            isLoading = isLoading,
            availableBackup = availableBackup,
            showEncryptionPasswordInput = showEncryptionPasswordInput,
            currency = currency,
            budgetInput = { budgetInput.value },
            onSignInClick = {
                coroutineScope.launch {
                    credentialService.startManualGetCredentialFlow(
                        activityContext = context.findActivity()
                    )
                }
            },
            actions = viewModel
        )
    }
}