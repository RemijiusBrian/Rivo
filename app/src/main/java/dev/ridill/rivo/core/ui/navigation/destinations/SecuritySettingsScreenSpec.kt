package dev.ridill.rivo.core.ui.navigation.destinations

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.startClass2BiometricOrCredentialAuthentication
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.BiometricUtil
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.core.ui.util.findActivity
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.presentation.securitySettings.SecuritySettingsScreen
import dev.ridill.rivo.settings.presentation.securitySettings.SecuritySettingsViewModel

data object SecuritySettingsScreenSpec : ScreenSpec {
    override val route: String = "security_settings"

    override val labelRes: Int = R.string.destination_security_settings

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: SecuritySettingsViewModel = hiltViewModel(navBackStackEntry)
        val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle(false)
        val selectedInterval by viewModel.appAutoLockInterval
            .collectAsStateWithLifecycle(AppAutoLockInterval.ONE_MINUTE)
        val screenSecurityEnabled by viewModel.screenSecurityEnabled
            .collectAsStateWithLifecycle(false)

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val biometricManager = remember(context) { BiometricManager.from(context) }
        val biometricEnrollLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                if (it.resultCode == Activity.RESULT_OK) {
                    if (biometricManager.canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
                        startBiometricAuthentication(
                            context = context,
                            onAuthSuccess = viewModel::onAuthenticationSuccess
                        )
                    }
                }
            }
        )

        LaunchedEffect(snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    SecuritySettingsViewModel.SecuritySettingsEvent.LaunchBiometricAuthentication -> {
                        when (biometricManager.canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators)) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                startBiometricAuthentication(
                                    context = context,
                                    onAuthSuccess = viewModel::onAuthenticationSuccess
                                )
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
            screenSecurityEnabled = screenSecurityEnabled,
            onScreenSecurityToggle = viewModel::onScreenSecurityToggle,
            navigateUp = navController::navigateUp
        )
    }
}

private inline fun startBiometricAuthentication(
    context: Context,
    crossinline onAuthSuccess: () -> Unit
) {
    val activity = context.findActivity() as AppCompatActivity
    val title = context.getString(
        R.string.biometric_prompt_title_app_name,
        context.getString(R.string.app_name)
    )
    val subtitle = context.getString(R.string.biometric_or_screen_lock_prompt_message)
    val authPromptCallback = object : AuthPromptCallback() {
        override fun onAuthenticationSucceeded(
            activity: FragmentActivity?,
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(activity, result)
            onAuthSuccess()
        }
    }

    activity.startClass2BiometricOrCredentialAuthentication(
        title = title,
        subtitle = subtitle,
        callback = authPromptCallback
    )
}