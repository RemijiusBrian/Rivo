package dev.ridill.rivo.application

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.ui.navigation.RivoNavHost
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.util.isPermissionGranted
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.presentation.security.AppLockScreen
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RivoActivity : FragmentActivity() {

    private val viewModel: RivoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        splashScreen.apply {
            setKeepOnScreenCondition { viewModel.showSplashScreen.value }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is RivoViewModel.RivoEvent.EnableSecureFlags -> {
                            if (event.enabled) {
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
        }

        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle(AppTheme.SYSTEM_DEFAULT)
            val showWelcomeFlow by viewModel.showWelcomeFlow.collectAsStateWithLifecycle(false)
            val dynamicTheme by viewModel.dynamicThemeEnabled.collectAsStateWithLifecycle(false)
            val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle(false)
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            ScreenContent(
                darkTheme = darkTheme,
                dynamicTheme = dynamicTheme,
                showWelcomeFlow = showWelcomeFlow,
                isAppLocked = isAppLocked,
                onAuthSuccess = viewModel::onAppLockAuthSucceeded,
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
        viewModel.stopAppAutoLockTimer()
    }

    override fun onStop() {
        super.onStop()
        viewModel.startAppAutoLockTimer()
    }

    private fun checkAppPermissions() {
        val isSmsPermissionGranted = isPermissionGranted(Manifest.permission.RECEIVE_SMS)
        viewModel.onSmsPermissionCheck(isSmsPermissionGranted)

        if (BuildUtil.isNotificationRuntimePermissionNeeded() && !viewModel.showWelcomeFlow.value) {
            val isNotificationPermissionGranted =
                isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            viewModel.onNotificationPermissionCheck(isNotificationPermissionGranted)
        }
    }
}

@Composable
private fun ScreenContent(
    darkTheme: Boolean,
    dynamicTheme: Boolean,
    showWelcomeFlow: Boolean,
    isAppLocked: Boolean,
    onAuthSuccess: () -> Unit,
    closeApp: () -> Unit
) {
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
                navController = navController,
                showWelcomeFlow = showWelcomeFlow
            )

            AnimatedVisibility(
                visible = isAppLocked,
                enter = scaleIn(
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = tween(durationMillis = LOCK_ANIM_DURATION)
                ) + fadeIn(),
                exit = scaleOut(
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = tween(durationMillis = LOCK_ANIM_DURATION)
                ) + fadeOut()
            ) {
                AppLockScreen(
                    onBack = closeApp,
                    onAuthSucceeded = { onAuthSuccess() },
                )
            }
        }
    }
}

private const val LOCK_ANIM_DURATION = 500