package dev.ridill.rivo.onboarding.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.common.SignInButton
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.BackupDetails

@Composable
fun GoogleSignInPage(
    onSignInClick: () -> Unit,
    onSkipSignInClick: () -> Unit,
    restoreStatus: UiText?,
    isRestoreRunning: Boolean,
    onSkipRestoreClick: () -> Unit,
    availableBackupDetails: BackupDetails?,
    onRestoreClick: () -> Unit,
    showEncryptionPasswordInput: Boolean,
    onEncryptionPasswordInputDismiss: () -> Unit,
    onEncryptionPasswordSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRestoreAvailable by remember(availableBackupDetails) {
        derivedStateOf { availableBackupDetails != null }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge)
    ) {
        MediumDisplayText(
            title = stringResource(
                id = if (isRestoreAvailable) R.string.welcome_flow_page_restore_data_title
                else R.string.welcome_flow_page_google_sign_in_title
            ),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        )
        Text(
            text = if (isRestoreAvailable) stringResource(
                R.string.welcome_flow_page_restore_data_message,
                availableBackupDetails?.getParsedDateTime()
                    ?.format(DateUtil.Formatters.localizedDateMedium)
                    .orEmpty()
            )
            else stringResource(R.string.welcome_flow_page_google_sign_in_message),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(weight = Float.One)

        if (isRestoreAvailable) {
            RestoreStatus(
                restoreStatus = restoreStatus,
                isRestoreRunning = isRestoreRunning,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        Crossfade(
            targetState = isRestoreAvailable,
            label = "SignInOrRestoreActions"
        ) { isRestoreAvailable ->
            if (isRestoreAvailable) {
                RestoreBackupActions(
                    onRestoreClick = onRestoreClick,
                    onSkipClick = onSkipRestoreClick,
                    isLoading = isRestoreRunning
                )
            } else {
                GoogleSignInActions(
                    onSignInClick = onSignInClick,
                    onSkipClick = onSkipSignInClick
                )
            }
        }

        if (showEncryptionPasswordInput) {
            val passwordInput = remember { mutableStateOf("") }
            OutlinedTextFieldSheet(
                titleRes = R.string.enter_password,
                inputValue = { passwordInput.value },
                onValueChange = { passwordInput.value = it },
                onDismiss = onEncryptionPasswordInputDismiss,
                onConfirm = {
                    onEncryptionPasswordSubmit(passwordInput.value)
                    passwordInput.value = ""
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@Composable
private fun RestoreBackupActions(
    isLoading: Boolean,
    onRestoreClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonWithLoadingIndicator(
            textRes = R.string.restore,
            loading = isLoading,
            onClick = onRestoreClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        TextButton(onClick = onSkipClick) {
            Text(stringResource(R.string.do_not_restore))
        }
    }
}

@Composable
private fun GoogleSignInActions(
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GoogleSignInButton(onClick = onSignInClick)

        TextButton(
            onClick = onSkipClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current
            )
        ) {
            Text(stringResource(R.string.action_skip))
        }
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            SignInButton(it).apply {
                setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO)

                setOnClickListener { onClick() }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun RestoreStatus(
    restoreStatus: UiText?,
    isRestoreRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = restoreStatus,
            label = "RestoreWorkerStateText",
            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
        ) { text ->
            Text(
                text = text?.asString().orEmpty(),
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        SpacerSmall()

        if (isRestoreRunning) {
            Text(
                text = stringResource(R.string.data_restore_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = SpacingLarge),
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRestoreScreen() {
    RivoTheme {
        GoogleSignInPage(
            onSignInClick = {},
            onSkipSignInClick = {},
            onSkipRestoreClick = {},
            availableBackupDetails = BackupDetails("", "", ""),
            onRestoreClick = {},
            onEncryptionPasswordSubmit = {},
            showEncryptionPasswordInput = false,
            onEncryptionPasswordInputDismiss = {},
            restoreStatus = null,
            isRestoreRunning = false
        )
    }
}