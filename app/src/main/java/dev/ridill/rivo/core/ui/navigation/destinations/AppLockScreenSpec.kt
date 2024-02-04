package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.settings.presentation.security.AppLockScreen
import dev.ridill.rivo.settings.presentation.settings.rememberBiometricPrompt
import dev.ridill.rivo.settings.presentation.settings.rememberPromptInfo

data object AppLockScreenSpec : ScreenSpec {
    override val route: String = "app_locked"

    override val labelRes: Int = R.string.destination_app_lock

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val biometricPrompt = rememberBiometricPrompt(
            onAuthSucceeded = {
                navController.navigate(DashboardScreenSpec.route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            },

        )
        val promptInfo = rememberPromptInfo(
            title = stringResource(R.string.fingerprint_title),
            subTitle = stringResource(R.string.fingerprint_subtitle)
        )

        LaunchedEffect(biometricPrompt, promptInfo) {
            biometricPrompt.authenticate(promptInfo)
        }

        AppLockScreen(
            launchAuthentication = { biometricPrompt.authenticate(promptInfo) }
        )
    }
}