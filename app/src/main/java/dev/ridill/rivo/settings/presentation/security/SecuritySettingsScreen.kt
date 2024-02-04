package dev.ridill.rivo.settings.presentation.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.navigation.destinations.SecuritySettingsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.settings.presentation.components.SwitchPreference

@Composable
fun SecuritySettingsScreen(
    appLockEnabled: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
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
        }
    }
}