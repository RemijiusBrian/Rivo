package dev.ridill.rivo.welcomeFlow.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.work.WorkInfo
import com.google.android.gms.common.SignInButton
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.MYMTheme
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.settings.domain.modal.BackupDetails

@Composable
fun GoogleSignInPage(
    onSignInClick: () -> Unit,
    onSkipSignInClick: () -> Unit,
    restoreWorkerState: WorkInfo.State?,
    onSkipRestoreClick: () -> Unit,
    availableBackupDetails: BackupDetails?,
    onRestoreClick: () -> Unit,
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
                restoreWorkerState = restoreWorkerState,
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
                    onSkipClick = onSkipRestoreClick
                )
            } else {
                GoogleSignInActions(
                    onSignInClick = onSignInClick,
                    onSkipClick = onSkipSignInClick
                )
            }
        }
    }
}

@Composable
private fun RestoreBackupActions(
    onRestoreClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onRestoreClick) {
            Text(stringResource(R.string.restore))
        }

        TextButton(onClick = onSkipClick) {
            Text(stringResource(R.string.dont_restore))
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
    restoreWorkerState: WorkInfo.State?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = restoreWorkerState,
            label = "RestoreWorkerStateText",
            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
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

        SpacerSmall()

        if (restoreWorkerState == WorkInfo.State.RUNNING) {
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
    MYMTheme {
        GoogleSignInPage(
            onSignInClick = {},
            onSkipSignInClick = {},
            restoreWorkerState = null,
            onSkipRestoreClick = {},
            availableBackupDetails = BackupDetails("", "", ""),
            onRestoreClick = {}
        )
    }
}