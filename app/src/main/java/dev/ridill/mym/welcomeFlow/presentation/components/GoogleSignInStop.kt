package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.work.WorkInfo
import com.google.android.gms.common.SignInButton
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Empty
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.ConfirmationDialog
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.settings.domain.modal.BackupDetails

@Composable
fun GoogleSignInStop(
    restoreState: WorkInfo.State?,
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
    availableBackup: BackupDetails?,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRestoreInProgress by remember(restoreState) {
        derivedStateOf { restoreState == WorkInfo.State.RUNNING }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge)
    ) {
        Text(
            text = stringResource(R.string.welcome_flow_stop_google_sign_in_message),
            style = MaterialTheme.typography.headlineSmall
        )

        VerticalSpacer(weight = Float.One)

        Crossfade(
            targetState = restoreState,
            label = "RestoreStateText",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) { state ->
            val stateText = when (state) {
                WorkInfo.State.RUNNING -> stringResource(R.string.data_restore_in_progress)
                WorkInfo.State.SUCCEEDED -> stringResource(R.string.restarting_app)
                else -> String.Empty
            }

            Text(
                text = stateText,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        VerticalSpacer(spacing = SpacingSmall)

        if (restoreState == WorkInfo.State.RUNNING) {
            Text(
                text = stringResource(R.string.data_restore_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = SpacingLarge),
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GoogleSignInButton(
                onClick = {
                    if (!isRestoreInProgress) onSignInClick()
                }
            )

            TextButton(onClick = onSkipClick) {
                Text(stringResource(R.string.action_skip))
            }
        }
    }

    if (availableBackup != null) {
        BackupFoundDialog(
            backupDetails = availableBackup,
            onRestoreClick = onRestoreClick,
            onSkipClick = onSkipClick
        )
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
private fun BackupFoundDialog(
    backupDetails: BackupDetails,
    onRestoreClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        titleRes = R.string.backup_found_dialog_title,
        content = stringResource(
            R.string.backup_found_dialog_content,
            backupDetails.getParsedDateTime()?.format(DateUtil.Formatters.localizedDateMedium)
                .orEmpty()
        ),
        onConfirm = onRestoreClick,
        onDismiss = onSkipClick,
        confirmActionRes = R.string.restore,
        dismissActionRes = R.string.action_skip,
        modifier = modifier
    )
}