package dev.ridill.mym.application

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.ui.navigation.MYMNavHost
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.util.isPermissionGranted
import dev.ridill.mym.settings.domain.modal.AppTheme

@AndroidEntryPoint
class MYMActivity : ComponentActivity() {

    private val viewModel: MYMViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        checkAppPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        splashScreen.apply {
            setKeepOnScreenCondition { viewModel.showSplashScreen.value }
        }
        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle(AppTheme.SYSTEM_DEFAULT)
            val showWelcomeFlow by viewModel.showWelcomeFlow.collectAsStateWithLifecycle(false)
            val dynamicTheme by viewModel.dynamicThemeEnabled.collectAsStateWithLifecycle(false)
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            MYMTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicTheme
            ) {
                val navController = rememberNavController()
                MYMNavHost(
                    navController = navController,
                    showWelcomeFlow = showWelcomeFlow
                )
            }
        }
    }

    private fun checkAppPermissions() {
        val isSmsPermissionGranted = isPermissionGranted(Manifest.permission.RECEIVE_SMS)
        viewModel.onSmsPermissionCheck(isSmsPermissionGranted)

        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
            val isNotificationPermissionGranted =
                isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            viewModel.onNotificationPermissionCheck(isNotificationPermissionGranted)
        }
    }
}