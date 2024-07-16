package dev.ridill.rivo.settings.presentation.backupSettings

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToDrive
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.RadioOptionListDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.icons.Google
import dev.ridill.rivo.core.ui.navigation.destinations.BackupSettingsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.FatalBackupError
import dev.ridill.rivo.settings.presentation.components.BasicPreference
import dev.ridill.rivo.settings.presentation.components.EmptyPreferenceIconSpacer
import dev.ridill.rivo.settings.presentation.components.PreferenceIcon
import dev.ridill.rivo.settings.presentation.components.PreferenceIconSize
import dev.ridill.rivo.settings.presentation.components.SimplePreference
import dev.ridill.rivo.settings.presentation.components.SimpleSettingsPreference

@Composable
fun BackupSettingsScreen(
    context: Context,
    snackbarController: SnackbarController,
    state: BackupSettingsState,
    actions: BackupSettingsActions,
    navigateUp: () -> Unit
) {
    val isAuthenticated by remember(state.authState) {
        derivedStateOf { state.authState is AuthState.Authenticated }
    }
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        snackbarController = snackbarController,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(BackupSettingsScreenSpec.labelRes)) },
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
            val showBackupError by remember(state.fatalBackupError) {
                derivedStateOf { state.fatalBackupError != null }
            }
            AnimatedVisibility(visible = showBackupError) {
                state.fatalBackupError?.let {
                    FatalBackupErrorMessage(
                        error = it,
                        modifier = Modifier
                            .padding(horizontal = SpacingMedium)
                    )
                }
            }

            val infoVerticalDp by animateDpAsState(
                targetValue = if (showBackupError) Dp.Zero
                else SpacingLarge,
                label = "VerticalSpacingDpForInfo"
            )
            BackupInfoText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = infoVerticalDp)
            )

            HorizontalDivider()

            SimpleSettingsPreference(
                titleRes = R.string.preference_google_account,
                leadingIcon = Icons.Default.Google,
                onClick = actions::onBackupAccountClick,
                summary = when (state.authState) {
                    is AuthState.Authenticated -> state.authState.account.email
                    AuthState.UnAuthenticated -> stringResource(R.string.click_to_sign_in_to_google_account)
                }
            )

            AnimatedVisibility(visible = isAuthenticated) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingSmall)
                ) {
                    SimplePreference(
                        titleRes = R.string.preference_backup_interval,
                        summary = stringResource(state.backupInterval.labelRes),
                        onClick = actions::onBackupIntervalPreferenceClick,
                        leadingIcon = { EmptyPreferenceIconSpacer() }
                    )

                    PreviousBackupDetails(
                        lastBackupDate = state.lastBackupDateFormatted?.asString(),
                        lastBackupTime = state.getBackupTimeFormatted(
                            is24HourFormat = DateFormat.is24HourFormat(context)
                        ),
                        onBackupNowClick = actions::onBackupNowClick,
                        isBackupRunning = state.isBackupRunning
                    )

                    SimplePreference(
                        titleRes = R.string.preference_backup_encryption_title,
                        summary = stringResource(R.string.preference_backup_encryption_summary),
                        onClick = actions::onEncryptionPreferenceClick,
                        leadingIcon = {
                            if (!state.isEncryptionPasswordAvailable) {
                                PreferenceIcon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            }
        }

        if (state.showBackupIntervalSelection && isAuthenticated) {
            RadioOptionListDialog(
                titleRes = R.string.choose_backup_interval,
                options = BackupInterval.entries,
                currentOption = state.backupInterval,
                onDismiss = actions::onBackupIntervalSelectionDismiss,
                onOptionSelect = actions::onBackupIntervalSelected
            )
        }

        if (state.showBackupRunningMessage) {
            BackupRunningMessageDialog(
                onAcknowledge = actions::onBackupRunningMessageAcknowledge
            )
        }
    }
}

@Composable
private fun BackupInfoText(
    modifier: Modifier = Modifier
) {
    BasicPreference(
        titleContent = { Text(stringResource(R.string.preference_title_google_drive)) },
        summaryContent = {
            Text(
                text = stringResource(
                    R.string.preference_google_drive_backup_message,
                    stringResource(R.string.app_name)
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AddToDrive,
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
fun PreviousBackupDetails(
    lastBackupDate: String?,
    lastBackupTime: String?,
    onBackupNowClick: () -> Unit,
    isBackupRunning: Boolean,
    modifier: Modifier = Modifier
) {
    BasicPreference(
        titleContent = {
            Text(
                text = stringResource(R.string.last_backup),
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingIcon = { EmptyPreferenceIconSpacer() },
        summaryContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingExtraSmall),
                modifier = Modifier
                    .padding(vertical = SpacingSmall)
            ) {
                lastBackupDate?.let { Text(stringResource(R.string.date_label, it)) }
                lastBackupTime?.let { Text(stringResource(R.string.time_label, it)) }

                Crossfade(
                    targetState = isBackupRunning,
                    label = "BackupProgressBarAnimation"
                ) { loading ->
                    if (loading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    } else {
                        Button(onClick = onBackupNowClick) {
                            Text(stringResource(R.string.backup_now))
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun FatalBackupErrorMessage(
    error: FatalBackupError,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null
            )

            Text(
                text = stringResource(
                    id = when (error) {
                        FatalBackupError.PASSWORD_CORRUPTED -> R.string.error_fatal_backup_failure_password_corruption
                        FatalBackupError.GOOGLE_AUTH_FAILURE -> R.string.error_fatal_backup_failure_auth_failure
                        FatalBackupError.STORAGE_QUOTA_EXCEEDED -> R.string.error_fatal_backup_failure_storage_quota_exceeded
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BackupRunningMessageDialog(
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onAcknowledge,
        confirmButton = {
            TextButton(onClick = onAcknowledge) {
                Text(stringResource(R.string.action_ok))
            }
        },
        text = {
            Text(stringResource(R.string.backup_running_message))
        },
        modifier = modifier
    )
}