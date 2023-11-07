package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsScreen
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsViewModel

data object BackupSettingsScreenSpec : ScreenSpec {
    override val route: String = "backup_settings"

    override val labelRes: Int = R.string.destination_backup_settings

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: BackupSettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = viewModel::onSignInResult
        )

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is BackupSettingsViewModel.BackupEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                is BackupSettingsViewModel.BackupEvent.LaunchGoogleSignIn -> {
                    googleSignInLauncher.launch(event.intent)
                }
            }
        }

        BackupSettingsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}