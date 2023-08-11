package dev.ridill.mym.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.core.ui.components.ConfirmationDialog
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
            val showWelcomeFlow by viewModel.showWelcomeFlow.collectAsStateWithLifecycle(false)
            val dynamicTheme by viewModel.dynamicThemeEnabled.collectAsStateWithLifecycle(false)
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            val showSignInPrompt by viewModel.showTesterSignInPrompt.collectAsStateWithLifecycle()
            val newAvailableRelease by viewModel.newAvailableRelease.collectAsStateWithLifecycle()

            MYMTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicTheme
            ) {
                val navController = rememberNavController()
                MYMNavHost(
                    navController = navController,
                    showWelcomeFlow = showWelcomeFlow
                )

                if (showSignInPrompt) {
                    ConfirmationDialog(
                        titleRes = com.google.firebase.appdistribution.impl.R.string.signin_dialog_title,
                        contentRes = com.google.firebase.appdistribution.impl.R.string.singin_dialog_message,
                        onConfirm = viewModel::onSignInPromptConfirm,
                        onDismiss = {},
                        showDismissButton = false,
                        confirmActionRes = com.google.firebase.appdistribution.impl.R.string.singin_yes_button,
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    )
                }

                newAvailableRelease?.let { release ->
                    ConfirmationDialog(
                        titleRes = com.google.firebase.appdistribution.impl.R.string.update_dialog_title,
                        content = stringResource(
                            com.google.firebase.appdistribution.impl.R.string.update_version_available,
                            release.displayVersion,
                            release.versionCode
                        ),
                        onConfirm = viewModel::onAppUpdateConfirm,
                        onDismiss = viewModel::onAppUpdateDismiss,
                        confirmActionRes = com.google.firebase.appdistribution.impl.R.string.update_yes_button,
                        dismissActionRes = com.google.firebase.appdistribution.impl.R.string.update_no_button
                    )
                }
            }
        }
    }
}