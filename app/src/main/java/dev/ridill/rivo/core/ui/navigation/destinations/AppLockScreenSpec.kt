package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.findActivity
import dev.ridill.rivo.settings.presentation.security.AppLockScreen
import dev.ridill.rivo.settings.presentation.settings.rememberBiometricPrompt
import dev.ridill.rivo.settings.presentation.settings.rememberPromptInfo
import kotlinx.coroutines.launch

data object AppLockScreenSpec : ScreenSpec {
    override val route: String = "app_locked"

    override val labelRes: Int = R.string.destination_app_lock

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val progressAnimatable = remember {
            Animatable(Float.Zero)
        }
        val coroutineScope = rememberCoroutineScope()
        val biometricPrompt = rememberBiometricPrompt(
            onAuthSucceeded = {
                coroutineScope.launch {
                    progressAnimatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = ANIM_DURATION)
                    )
                    navController.navigateUp()
                }
            },
        )
        val promptInfo = rememberPromptInfo(
            title = stringResource(R.string.fingerprint_title),
            subTitle = stringResource(R.string.fingerprint_subtitle)
        )

        LaunchedEffect(Unit) {
            biometricPrompt.authenticate(promptInfo)
        }

        val activity = LocalContext.current.findActivity()

        AppLockScreen(
            onBack = activity::finish,
            launchAuthentication = { biometricPrompt.authenticate(promptInfo) },
            animProgress = { progressAnimatable.value }
        )
    }
}

private const val ANIM_DURATION = 2000