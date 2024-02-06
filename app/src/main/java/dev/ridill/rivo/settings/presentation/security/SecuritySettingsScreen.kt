package dev.ridill.rivo.settings.presentation.security

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.LabelledRadioButton
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.navigation.destinations.SecuritySettingsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.rivo.settings.presentation.components.SwitchPreference

@Composable
fun SecuritySettingsScreen(
    appLockEnabled: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    autoLockInterval: AppAutoLockInterval,
    onIntervalSelect: (AppAutoLockInterval) -> Unit,
    navigateUp: () -> Unit
) {
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SecuritySettingsScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SpacingMedium)
        ) {
            SwitchPreference(
                titleRes = R.string.preference_app_lock,
                summary = stringResource(
                    R.string.preference_app_lock_summary,
                    stringResource(R.string.app_name)
                ),
                value = appLockEnabled,
                onValueChange = onAppLockToggle
            )

            Divider()

            AnimatedVisibility(visible = appLockEnabled) {
                AutoLockIntervalSelection(
                    selectedInterval = autoLockInterval,
                    onIntervalSelect = onIntervalSelect
                )
            }
        }
    }
}

@Composable
private fun AutoLockIntervalSelection(
    selectedInterval: AppAutoLockInterval,
    onIntervalSelect: (AppAutoLockInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        SimpleSettingsPreference(titleRes = R.string.auto_lock_after)
        AppAutoLockInterval.entries.forEach { interval ->
            LabelledRadioButton(
                label = stringResource(interval.labelRes),
                selected = interval == selectedInterval,
                onClick = { onIntervalSelect(interval) }
            )
        }
    }
}