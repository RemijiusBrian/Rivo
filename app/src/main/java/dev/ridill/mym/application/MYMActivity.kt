package dev.ridill.mym.application

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.OnLifecycleStartEffect
import dev.ridill.mym.core.ui.components.PermissionRationaleDialog
import dev.ridill.mym.core.ui.components.rememberPermissionsState
import dev.ridill.mym.core.ui.navigation.MYMNavHost
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.settings.domain.modal.AppTheme

@AndroidEntryPoint
class MYMActivity : ComponentActivity() {

    private val viewModel: MYMViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle(AppTheme.SYSTEM_DEFAULT)
            val dynamicTheme by viewModel.dynamicThemeEnabled.collectAsStateWithLifecycle(false)
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                rememberPermissionsState(Manifest.permission.POST_NOTIFICATIONS)
            else null
            var showPermissionRationale by remember { mutableStateOf(false) }

            OnLifecycleStartEffect {
                showPermissionRationale = permissionsState?.shouldShowRationale == true
            }

            MYMTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicTheme
            ) {
                val navController = rememberNavController()
                MYMNavHost(navController = navController)

                if (showPermissionRationale) {
                    PermissionRationaleDialog(
                        icon = Icons.Rounded.Notifications,
                        textRes = R.string.permission_rationale_notification,
                        onDismiss = { showPermissionRationale = false },
                        onAgree = {
                            showPermissionRationale = false
                            permissionsState?.requestPermission()
                        }
                    )
                }
            }
        }
    }
}