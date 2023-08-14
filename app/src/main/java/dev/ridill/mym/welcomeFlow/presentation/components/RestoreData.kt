package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.work.WorkInfo
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.Empty
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.LargeTitle
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingSmall

@Composable
fun RestoreDataContent(
    restoreState: WorkInfo.State,
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRestoreInProgress by remember(restoreState) {
        derivedStateOf { restoreState == WorkInfo.State.RUNNING }
    }
    Column(
        modifier = modifier
    ) {
        VerticalSpacer(spacing = SpacingExtraLarge)
        LargeTitle(
            title = stringResource(R.string.restore_data),
            modifier = Modifier
                .fillMaxWidth(0.80f)
        )

        VerticalSpacer(spacing = SpacingExtraLarge)

        Text(
            text = stringResource(R.string.sign_in_and_restore_data_message),
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

        Text(
            text = stringResource(R.string.data_restore_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = SpacingLarge),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )

        Column {
            Button(
                onClick = {
                    if (!isRestoreInProgress)
                        onCheckForBackupClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = isRestoreInProgress,
                    label = "ButtonLoadingAnimation"
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(ButtonDefaults.IconSize),
                            color = LocalContentColor.current
                        )
                    } else {
                        Text(stringResource(R.string.check_for_backup))
                    }
                }
            }

            TextButton(
                onClick = onSkipClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_skip))
            }
        }
    }
}

@Preview
@Composable
fun PreviewRestoreDataContent() {
    MYMTheme {
        RestoreDataContent(
            onCheckForBackupClick = {},
            onSkipClick = {},
            modifier = Modifier
                .fillMaxSize(),
            restoreState = WorkInfo.State.SUCCEEDED,
        )
    }
}