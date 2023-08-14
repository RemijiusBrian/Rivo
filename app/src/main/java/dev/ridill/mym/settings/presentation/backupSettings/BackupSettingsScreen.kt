package dev.ridill.mym.settings.presentation.backupSettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.LabelledRadioButton
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.navigation.destinations.BackupSettingsDestination
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.settings.domain.modal.BackupInterval
import dev.ridill.mym.settings.presentation.components.BasicPreference
import dev.ridill.mym.settings.presentation.components.EmptyIconSpacer
import dev.ridill.mym.settings.presentation.components.PreferenceIconSize
import dev.ridill.mym.settings.presentation.components.SimplePreference
import dev.ridill.mym.settings.presentation.components.SimpleSettingsPreference

@Composable
fun BackupSettingsScreen(
    snackbarController: SnackbarController,
    state: BackupSettingsState,
    actions: BackupSettingsActions,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    MYMScaffold(
        snackbarController = snackbarController,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(BackupSettingsDestination.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            BackupDetailsText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingLarge)
            )

            Divider()

            SimpleSettingsPreference(
                titleRes = R.string.preference_google_account,
                leadingIcon = Icons.Default.CloudUpload,
                onClick = actions::onBackupAccountClick,
                summary = state.accountEmail
                    ?: stringResource(R.string.click_to_sign_in_to_google_account)
            )

            AnimatedVisibility(visible = state.isAccountAdded) {
                SimplePreference(
                    titleRes = R.string.preference_backup_interval,
                    summary = stringResource(state.interval.labelRes),
                    onClick = actions::onBackupIntervalPreferenceClick,
                    leadingIcon = { EmptyIconSpacer() }
                )
            }

            AnimatedVisibility(visible = state.isAccountAdded) {
                LastBackupDetails(
                    lastBackupDate = "",
                    lastBackupTime = "",
                    onBackupNowClick = actions::onBackupNowClick,
                    onRestoreClick = actions::onRestoreClick
                )
            }
        }

        if (state.isAccountAdded && state.showBackupIntervalSelection) {
            BackupIntervalSelection(
                currentInterval = state.interval,
                onDismiss = actions::onBackupIntervalSelectionDismiss,
                onConfirm = actions::onBackupIntervalSelected
            )
        }
    }
}

@Composable
private fun BackupDetailsText(
    modifier: Modifier = Modifier
) {
    BasicPreference(
        titleContent = { Text(stringResource(R.string.preference_title_google_drive)) },
        summaryContent = { Text(stringResource(R.string.preference_google_drive_backup_message)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Backup,
                contentDescription = null,
                modifier = Modifier
                    .size(PreferenceIconSize)
            )
        },
        titleTextStyle = MaterialTheme.typography.titleMedium,
        summaryTextStyle = MaterialTheme.typography.bodyLarge,
        modifier = modifier,
        verticalAlignment = Alignment.Top
    )
}

@Composable
private fun BackupIntervalSelection(
    currentInterval: BackupInterval,
    onDismiss: () -> Unit,
    onConfirm: (BackupInterval) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        title = { Text(stringResource(R.string.choose_backup_interval)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
            ) {
                BackupInterval.values().forEach { interval ->
                    LabelledRadioButton(
                        labelRes = interval.labelRes,
                        selected = interval == currentInterval,
                        onClick = { onConfirm(interval) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
fun LastBackupDetails(
    lastBackupDate: String,
    lastBackupTime: String,
    onBackupNowClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Button(onClick = onBackupNowClick) {
            Text(stringResource(R.string.backup_now))
        }

        Button(onClick = onRestoreClick) {
            Text("Restore")
        }
    }
}