package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.LargeTitle
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge

@Composable
fun RestoreDataContent(
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        Column {
            Button(
                onClick = onCheckForBackupClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(R.string.check_for_backup))
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
                .fillMaxSize()
        )
    }
}