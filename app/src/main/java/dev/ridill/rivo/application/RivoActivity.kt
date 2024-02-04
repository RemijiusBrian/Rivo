package dev.ridill.rivo.application

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RivoActivity : FragmentActivity() {

    private val viewModel: RivoViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        checkAppPermissions()
    }

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
                        RivoViewModel.RivoEvent.EnableSecureFlags -> {
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE
                            )
                        }
                    }
                }
            }
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

            RivoTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicTheme
            ) {
                val navController = rememberNavController()
                RivoNavHost(
                    navController = navController,
                    showWelcomeFlow = showWelcomeFlow
                )
            }
        }
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