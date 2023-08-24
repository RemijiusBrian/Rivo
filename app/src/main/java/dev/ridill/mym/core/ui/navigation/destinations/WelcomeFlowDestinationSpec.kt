package dev.ridill.mym.core.ui.navigation.destinations

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.ui.components.rememberMultiplePermissionsLauncher
import dev.ridill.mym.core.ui.components.rememberMultiplePermissionsState
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.core.ui.util.restartApplication
import dev.ridill.mym.welcomeFlow.presentation.WelcomeFlowScreen
import dev.ridill.mym.welcomeFlow.presentation.WelcomeFlowViewModel

object WelcomeFlowDestinationSpec : ChildDestinationSpec {
    override val route: String = "welcome_flow"
    override val labelRes: Int = R.string.destination_welcome_flow

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: WelcomeFlowViewModel = hiltViewModel(navBackStackEntry)
        val flowStop by viewModel.currentFlowStop.collectAsStateWithLifecycle()
        val budgetInput = viewModel.budgetInput.collectAsStateWithLifecycle()
        val availableBackup by viewModel.availableBackup.collectAsStateWithLifecycle()
        val restoreState by viewModel.restoreState.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val permissionsList = if (BuildUtil.isNotificationRuntimePermissionNeeded()) listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_SMS
        ) else listOf(Manifest.permission.RECEIVE_SMS)
        val permissionsLauncher = rememberMultiplePermissionsLauncher(
            onResult = { viewModel.onPermissionResponse() }
        )
        val multiplePermissionsState = rememberMultiplePermissionsState(
            permissions = permissionsList,
            launcher = permissionsLauncher
        )

        val signInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.onSignInResult(result.data)
                }
            }
        )

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    WelcomeFlowViewModel.WelcomeFlowEvent.LaunchPermissionRequests -> {
                        multiplePermissionsState.launchRequest()
                    }

                    is WelcomeFlowViewModel.WelcomeFlowEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    WelcomeFlowViewModel.WelcomeFlowEvent.WelcomeFlowConcluded -> {
                        navController.navigate(DashboardDestinationSpec.route) {
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
        }

        WelcomeFlowScreen(
            snackbarController = snackbarController,
            flowStop = flowStop,
            availableBackup = availableBackup,
            budgetInput = { budgetInput.value },
            restoreState = restoreState,
            actions = viewModel
        )
    }
}