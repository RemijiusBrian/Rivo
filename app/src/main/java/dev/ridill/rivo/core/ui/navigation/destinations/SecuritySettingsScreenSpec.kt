package dev.ridill.rivo.core.ui.navigation.destinations

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.presentation.security.BiometricUtil
import dev.ridill.rivo.settings.presentation.security.SecuritySettingsScreen
import dev.ridill.rivo.settings.presentation.security.SecuritySettingsViewModel
import dev.ridill.rivo.settings.presentation.security.rememberBiometricManager
import dev.ridill.rivo.settings.presentation.security.rememberBiometricPrompt
import dev.ridill.rivo.settings.presentation.security.rememberPromptInfo

data object SecuritySettingsScreenSpec : ScreenSpec {
    override val route: String = "security_settings"

    override val labelRes: Int = R.string.destination_security_settings

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: SecuritySettingsViewModel = hiltViewModel(navBackStackEntry)
        val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle(false)
        val selectedInterval by viewModel.appAutoLockInterval
            .collectAsStateWithLifecycle(AppAutoLockInterval.ONE_MINUTE)
        val biometricManager = rememberBiometricManager()
        val biometricPrompt = rememberBiometricPrompt(
            onAuthSucceeded = { viewModel.onAuthenticationSuccess() }
        )
        val promptInfo = rememberPromptInfo(
            title = stringResource(R.string.fingerprint_title),
            subTitle = stringResource(R.string.fingerprint_subtitle)
        )

        val biometricEnrollLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                if (it.resultCode == Activity.RESULT_OK) {
                    if (biometricManager.canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
                        biometricPrompt.authenticate(promptInfo)
                    }
                }
            }
        )

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        LaunchedEffect(biometricPrompt, promptInfo, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    SecuritySettingsViewModel.SecuritySettingsEvent.LaunchAuthentication -> {
                        when (biometricManager.canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators)) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                biometricPrompt.authenticate(promptInfo)
                            }

                            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                                snackbarController.showSnackbar(
                                    context.getString(R.string.error_biometric_hw_unavailable)
                                )
                            }

                            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                if (BuildUtil.isApiLevelAtLeast30) {
                                    // Prompts the user to create credentials that your app accepts.
                                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                                        .apply {
                                            putExtra(
                                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                                BiometricUtil.DefaultBiometricAuthenticators
                                            )
                                        }
                                    biometricEnrollLauncher.launch(enrollIntent)
                                }
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }

        SecuritySettingsScreen(
            snackbarController = snackbarController,
            appLockEnabled = appLockEnabled,
            onAppLockToggle = viewModel::onAppLockToggle,
            autoLockInterval = selectedInterval,
            onIntervalSelect = viewModel::onAutoLockIntervalSelect,
            navigateUp = navController::navigateUp
        )
    }
}