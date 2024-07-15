package dev.ridill.rivo.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.isAnyOf
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.autofill
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.onboarding.domain.model.DataRestoreState

@Composable
fun DataRestorePage(
    restoreState: DataRestoreState,
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    showEncryptionPasswordInput: Boolean,
    onEncryptionPasswordInputDismiss: () -> Unit,
    onEncryptionPasswordSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRestoreInProgress by remember(restoreState) {
        derivedStateOf {
            restoreState.isAnyOf(
                DataRestoreState.CHECKING_FOR_BACKUP,
                DataRestoreState.DOWNLOADING_DATA,
                DataRestoreState.RESTORE_IN_PROGRESS
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MediumDisplayText(
            title = stringResource(R.string.onboarding_page_restore_data_title),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        )
        Text(
            text = stringResource(R.string.onboarding_page_restore_data_message),
            style = MaterialTheme.typography.titleLarge
        )

        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_cloud_download_filled),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight(DOWNLOAD_ICON_HEIGHT_FRACTION)
                .aspectRatio(Float.One)
        )

        Spacer(weight = Float.One)

        AnimatedVisibility(visible = isRestoreInProgress) {
            RestoreStatus(
                dataRestoreState = restoreState,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        RestoreBackupActions(
            onRestoreClick = onCheckForBackupClick,
            onSkipClick = onSkipClick,
            isRestoreInProgress = isRestoreInProgress
        )

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
                ),
                textFieldModifier = Modifier.autofill(
                    types = listOf(AutofillType.Password),
                    onFill = { passwordInput.value = it }
                ),
                label = stringResource(R.string.enter_password)
            )
        }
    }
}

@Composable
private fun RestoreStatus(
    dataRestoreState: DataRestoreState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = dataRestoreState,
            label = "RestoreWorkerStateText"
        ) { restoreState ->
            val uiText = when (restoreState) {
                DataRestoreState.IDLE -> null
                DataRestoreState.CHECKING_FOR_BACKUP -> R.string.checking_for_backups
                DataRestoreState.DOWNLOADING_DATA -> R.string.downloading_app_data
                DataRestoreState.RESTORE_IN_PROGRESS -> R.string.data_restore_in_progress
                DataRestoreState.COMPLETED -> R.string.restarting_app
                DataRestoreState.FAILED -> R.string.error_app_data_restore_failed
            }?.let { UiText.StringResource(it) }

            Text(
                text = uiText?.asString().orEmpty(),
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        SpacerSmall()

        Text(
            text = stringResource(R.string.data_restore_caution_message),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = SpacingLarge),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
    }
}

@Composable
private fun RestoreBackupActions(
    isRestoreInProgress: Boolean,
    onRestoreClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onRestoreClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            enabled = !isRestoreInProgress
        ) {
            Text(stringResource(R.string.check_for_backups))
        }

        TextButton(
            onClick = onSkipClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current
            ),
            enabled = !isRestoreInProgress
        ) {
            Text(stringResource(R.string.do_not_restore))
        }
    }
}

private const val DOWNLOAD_ICON_HEIGHT_FRACTION = 0.16f