package dev.ridill.rivo.application

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.startClass2BiometricOrCredentialAuthentication
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.BiometricUtil
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.ui.components.circularReveal
import dev.ridill.rivo.core.ui.navigation.RivoNavHost
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.isPermissionGranted
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.presentation.security.AppLockScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RivoActivity : FragmentActivity() {

    private val viewModel: RivoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            RivoViewModel.RivoEvent.LaunchAppLockAuthentication -> {
                                checkAndLaunchBiometric()
                            }
                        }
                    }
                }
                
                launch {
                    viewModel.screenSecurityEnabled.collectLatest { enabled ->
                        if (enabled) {
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE
                            )
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
                }
            }
        }

        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle(AppTheme.SYSTEM_DEFAULT)
            val showWelcomeFlow by viewModel.showWelcomeFlow.collectAsStateWithLifecycle(false)
            val dynamicTheme by viewModel.dynamicThemeEnabled.collectAsStateWithLifecycle(false)
            val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle(false)
            val appLockErrorMessage by viewModel.appLockAuthErrorMessage.collectAsStateWithLifecycle()
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            val windowSizeClass = calculateWindowSizeClass(activity = this)

            ScreenContent(
                windowSizeClass = windowSizeClass,
                darkTheme = darkTheme,
                dynamicTheme = dynamicTheme,
                showWelcomeFlow = showWelcomeFlow,
                appLockErrorMessage = appLockErrorMessage,
                isAppLocked = isAppLocked,
                onUnlockClick = ::checkAndLaunchBiometric,
                closeApp = ::finish
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkAppPermissions()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppStop()
    }

    private fun checkAppPermissions() {
        if (viewModel.showWelcomeFlow.value) return
        val isSmsPermissionGranted = isPermissionGranted(Manifest.permission.RECEIVE_SMS)
        viewModel.onSmsPermissionCheck(isSmsPermissionGranted)

        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
            val isNotificationPermissionGranted =
                isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            viewModel.onNotificationPermissionCheck(isNotificationPermissionGranted)
        }
    }

    private fun checkAndLaunchBiometric() {
        val canAuthenticate = BiometricManager.from(this)
            .canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS
        if (!canAuthenticate) return

        val title = getString(R.string.biometric_prompt_title)
        val subtitle = getString(R.string.biometric_or_screen_lock_prompt_message)
        val authPromptCallback = object : AuthPromptCallback() {
            override fun onAuthenticationError(
                activity: FragmentActivity?,
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(activity, errorCode, errString)
                viewModel.updateAppLockErrorMessage(UiText.DynamicString(errString.toString()))
            }

            override fun onAuthenticationSucceeded(
                activity: FragmentActivity?,
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(activity, result)
                viewModel.onAppLockAuthSucceeded()
            }

            override fun onAuthenticationFailed(activity: FragmentActivity?) {
                super.onAuthenticationFailed(activity)
                viewModel.updateAppLockErrorMessage(UiText.StringResource(R.string.error_biometric_auth_failed))
            }
        }

        startClass2BiometricOrCredentialAuthentication(
            title = title,
            subtitle = subtitle,
            callback = authPromptCallback
        )
    }
}

@Composable
private fun ScreenContent(
    windowSizeClass: WindowSizeClass,
    darkTheme: Boolean,
    dynamicTheme: Boolean,
    showWelcomeFlow: Boolean,
    appLockErrorMessage: UiText?,
    isAppLocked: Boolean,
    onUnlockClick: () -> Unit,
    closeApp: () -> Unit
) {
    val unlockIconAnimProgress = remember { Animatable(0f) }
    val lockScreenVisibilityProgress = remember { Animatable(0f) }
    var showAppLock by rememberSaveable { mutableStateOf(isAppLocked) }

    LaunchedEffect(isAppLocked) {
        if (isAppLocked) {
            unlockIconAnimProgress.snapTo(0f)
            showAppLock = true
            lockScreenVisibilityProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = APP_SCREEN_VISIBILITY_ANIM_DURATION)
            )
        } else {
            unlockIconAnimProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = UNLOCK_ICON_ANIM_DURATION)
            )
            lockScreenVisibilityProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = APP_SCREEN_VISIBILITY_ANIM_DURATION)
            )
            showAppLock = false
        }
    }

    RivoTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicTheme
    ) {
        val navController = rememberNavController()
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            RivoNavHost(
                windowSizeClass = windowSizeClass,
                navController = navController,
                showWelcomeFlow = showWelcomeFlow
            )

            if (showAppLock) {
                AppLockScreen(
                    onBack = closeApp,
                    unlockAnimProgress = unlockIconAnimProgress.asState(),
                    onUnlockClick = onUnlockClick,
                    errorMessage = appLockErrorMessage,
                    modifier = Modifier
                        .circularReveal(
                            transitionProgress = lockScreenVisibilityProgress.asState(),
                            revealFrom = Offset(1f, 0f)
                        )
                )
            }
        }
    }
}

private const val UNLOCK_ICON_ANIM_DURATION = 1000
private const val APP_SCREEN_VISIBILITY_ANIM_DURATION = 500