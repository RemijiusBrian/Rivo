package dev.ridill.mym.settings.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.LabelledRadioButton
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.MonthlyLimitInputDialog
import dev.ridill.mym.core.ui.components.icons.Brightness
import dev.ridill.mym.core.ui.components.icons.NotificationBell
import dev.ridill.mym.core.ui.components.icons.Palette
import dev.ridill.mym.core.ui.navigation.destinations.SettingsDestination
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.settings.domain.modal.AppTheme

@Composable
fun SettingsScreen(
    snackbarHostState: SnackbarHostState,
    state: SettingsState,
    actions: SettingsActions,
    navigateUp: () -> Unit,
    navigateToNotificationSettings: () -> Unit
) {
    MYMScaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SettingsDestination.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        },
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SimplePreference(
                titleRes = R.string.app_theme,
                summary = stringResource(state.appTheme.labelRes),
                onClick = actions::onAppThemePreferenceClick,
                leadingIcon = Icons.Outlined.Brightness
            )

            if (BuildUtil.isDynamicColorsSupported()) {
                SwitchPreference(
                    titleRes = R.string.dynamic_colors,
                    summary = stringResource(R.string.dynamic_colors_summary),
                    checked = state.dynamicColorsEnabled,
                    onCheckedChange = actions::onDynamicThemeEnabledChange,
                    leadingIcon = Icons.Outlined.Palette
                )
            }

            SimplePreference(
                titleRes = R.string.notifications,
                onClick = navigateToNotificationSettings,
                leadingIcon = Icons.Outlined.NotificationBell
            )

            PreferenceDivider()

            SimplePreference(
                titleRes = R.string.monthly_limit,
                summary = state.currentMonthlyLimit.takeIf { it.isNotEmpty() }
                    ?.let { stringResource(R.string.your_limit_is_set_to_value, it) }
                    ?: stringResource(R.string.click_to_set_monthly_limit),
                onClick = actions::onMonthlyLimitPreferenceClick
            )
        }

        if (state.showAppThemeSelection) {
            AppThemeSelectionDialog(
                currentTheme = state.appTheme,
                onDismiss = actions::onAppThemeSelectionDismiss,
                onConfirm = actions::onAppThemeSelectionConfirm
            )
        }

        if (state.showMonthlyLimitInput) {
            MonthlyLimitInputDialog(
                onConfirm = actions::onMonthlyLimitInputConfirm,
                onDismiss = actions::onMonthlyLimitInputDismiss,
                placeholderAmount = state.currentMonthlyLimit
            )
        }
    }
}

@Composable
fun SimplePreference(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier
                    .clickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                else Modifier
            )
            .padding(contentPadding)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PreferenceContentSpacing)
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier
                    .size(PreferenceIconSize)
            )
        }
        Column(
            modifier = Modifier
                .weight(Float.One)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge
            )
            summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = LocalContentColor.current
                        .copy(alpha = 0.64f)
                )
            }
        }
    }
}

@Composable
private fun SwitchPreference(
    @StringRes titleRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange
            )
            .padding(contentPadding)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PreferenceContentSpacing)
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier
                    .size(PreferenceIconSize)
            )
        }
        Column(
            modifier = Modifier
                .weight(Float.One)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium
            )
            summary?.let {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = LocalContentColor.current
                        .copy(alpha = 0.64f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .clearAndSetSemantics {}
        )
    }
}

private val PreferenceContentSpacing = 30.dp
private val PreferenceIconSize = 24.dp
private val PreferenceContentPadding = PaddingValues(
    horizontal = SpacingMedium,
    vertical = SpacingSmall
)

@Composable
private fun PreferenceDivider() = Divider(
    modifier = Modifier
        .padding(vertical = 12.dp)
)

@Composable
private fun AppThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onConfirm: (AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        title = { Text(stringResource(R.string.choose_theme)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
            ) {
                AppTheme.values().forEach { theme ->
                    LabelledRadioButton(
                        labelRes = theme.labelRes,
                        selected = theme == currentTheme,
                        onClick = { onConfirm(theme) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    )
}