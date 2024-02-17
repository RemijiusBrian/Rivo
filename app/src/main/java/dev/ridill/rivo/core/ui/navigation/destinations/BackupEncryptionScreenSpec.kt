package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.settings.presentation.backupEncryption.ACTION_ENCRYPTION_PASSWORD
import dev.ridill.rivo.settings.presentation.backupEncryption.BackupEncryptionScreen
import dev.ridill.rivo.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.rivo.settings.presentation.backupEncryption.ENCRYPTION_PASSWORD_UPDATED
import dev.ridill.rivo.settings.presentation.security.rememberBiometricPrompt
import dev.ridill.rivo.settings.presentation.security.rememberPromptInfo

data object BackupEncryptionScreenSpec : ScreenSpec {
    override val route: String = "backup_encryption"

    override val labelRes: Int = R.string.destination_backup_encryption

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: BackupEncryptionViewModel = hiltViewModel(navBackStackEntry)
        val currentPassword = viewModel.currentPassword.collectAsStateWithLifecycle()
        val newPassword = viewModel.newPassword.collectAsStateWithLifecycle()
        val confirmNewPassword = viewModel.confirmNewPassword.collectAsStateWithLifecycle()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val biometricPrompt = rememberBiometricPrompt(
            onAuthSucceeded = { viewModel.onBiometricAuthSucceeded() }
        )
        val promptInfo = rememberPromptInfo()

        LaunchedEffect(viewModel, context, snackbarController) {
            viewModel.events.collect { event ->
                when (event) {
                    is BackupEncryptionViewModel.BackupEncryptionEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            message = event.message.asString(context),
                            isError = event.message.isErrorText
                        )
                    }

                    BackupEncryptionViewModel.BackupEncryptionEvent.PasswordUpdated -> {
                        navController.navigateUpWithResult(
                            ACTION_ENCRYPTION_PASSWORD,
                            ENCRYPTION_PASSWORD_UPDATED
                        )
                    }

                    BackupEncryptionViewModel.BackupEncryptionEvent.LaunchBiometricAuthentication -> {
                        biometricPrompt.authenticate(promptInfo)
                    }
                }
            }
        }

        BackupEncryptionScreen(
            snackbarController = snackbarController,
            currentPassword = { currentPassword.value },
            newPassword = { newPassword.value },
            confirmNewPassword = { confirmNewPassword.value },
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )

    }
}