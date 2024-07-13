package dev.ridill.rivo.core.ui.navigation.destinations

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.authentication.DefaultCredentialService
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.settings.presentation.backupEncryption.ACTION_ENCRYPTION_PASSWORD
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsScreen
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsViewModel
import java.util.Currency

data object BackupSettingsScreenSpec : ScreenSpec {
    override val route: String = "backup_settings"

    override val labelRes: Int = R.string.destination_backup_settings

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = VIEW_BACKUP_SETTINGS_DEEPLINK_URI_PATTERN }
    )

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    fun buildBackupSettingsDeeplinkUri(): Uri = VIEW_BACKUP_SETTINGS_DEEPLINK_URI_PATTERN.toUri()

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: BackupSettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = viewModel::onSignInResult
        )

        DestinationResultEffect(
            key = ACTION_ENCRYPTION_PASSWORD,
            navBackStackEntry = navBackStackEntry,
            onResult = viewModel::onDestinationResult
        )

        val defaultAuthenticationService = DefaultCredentialService(context)

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is BackupSettingsViewModel.BackupSettingsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                is BackupSettingsViewModel.BackupSettingsEvent.LaunchGoogleSignIn -> {
                }

                BackupSettingsViewModel.BackupSettingsEvent.NavigateToBackupEncryptionScreen -> {
                    navController.navigate(BackupEncryptionScreenSpec.route)
                }
            }
        }

        BackupSettingsScreen(
            context = context,
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}

private const val VIEW_BACKUP_SETTINGS_DEEPLINK_URI_PATTERN = "$DEEP_LINK_URI/view_backup_settings"