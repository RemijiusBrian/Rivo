package dev.ridill.rivo.core.ui.navigation.destinations

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.pager.rememberPagerState
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
import dev.ridill.rivo.welcomeFlow.domain.model.WelcomeFlowPage
import dev.ridill.rivo.welcomeFlow.presentation.WelcomeFlowScreen
import dev.ridill.rivo.welcomeFlow.presentation.WelcomeFlowViewModel

data object WelcomeFlowScreenSpec : ScreenSpec {
    override val route: String = "welcome_flow"
    override val labelRes: Int = R.string.destination_welcome_flow

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: WelcomeFlowViewModel = hiltViewModel(navBackStackEntry)
        val pagerState = rememberPagerState(
            pageCount = { WelcomeFlowPage.entries.size }
        )
        val availableBackup by viewModel.availableBackup.collectAsStateWithLifecycle()
        val restoreState by viewModel.restoreState.collectAsStateWithLifecycle()
        val currency by viewModel.currency.collectAsStateWithLifecycle(initialValue = LocaleUtil.defaultCurrency)
        val budgetInput = viewModel.budgetInput.collectAsStateWithLifecycle()

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
                is WelcomeFlowViewModel.WelcomeFlowEvent.NavigateToPage -> {
                    if (!pagerState.isScrollInProgress)
                        pagerState.animateScrollToPage(event.page.ordinal)
                }

                WelcomeFlowViewModel.WelcomeFlowEvent.LaunchNotificationPermissionRequest -> {
                    notificationPermissionLauncher?.launchRequest()
                }

                is WelcomeFlowViewModel.WelcomeFlowEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                WelcomeFlowViewModel.WelcomeFlowEvent.WelcomeFlowConcluded -> {
                    navController.navigate(DashboardScreenSpec.route) {
                        popUpTo(route) {
                            inclusive = true
                        }
                    }
                }

                is WelcomeFlowViewModel.WelcomeFlowEvent.LaunchGoogleSignIn -> {
                    signInLauncher.launch(event.intent)
                }

                WelcomeFlowViewModel.WelcomeFlowEvent.RestartApplication -> {
                    context.restartApplication()
                }
            }
        }

        WelcomeFlowScreen(
            snackbarController = snackbarController,
            pagerState = pagerState,
            restoreState = restoreState,
            availableBackup = availableBackup,
            currency = currency,
            budgetInput = { budgetInput.value },
            actions = viewModel
        )
    }
}