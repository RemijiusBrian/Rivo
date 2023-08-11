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
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ridill.mym.BuildConfig
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.IncomeInputDialog
import dev.ridill.mym.core.ui.components.LabelledRadioButton
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.PermissionRationaleDialog
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.components.icons.Message
import dev.ridill.mym.core.ui.navigation.destinations.SettingsDestination
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.settings.domain.modal.AppTheme

@Composable
fun SettingsScreen(
    snackbarController: SnackbarController,
    state: SettingsState,
    actions: SettingsActions,
    navigateUp: () -> Unit,
    navigateToNotificationSettings: () -> Unit,
    navigateToSourceCode: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    MYMScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SettingsDestination.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SimplePreference(
                titleRes = R.string.preference_app_theme,
                summary = stringResource(state.appTheme.labelRes),
                onClick = actions::onAppThemePreferenceClick,
                leadingIcon = Icons.Rounded.BrightnessMedium
            )

            if (BuildUtil.isDynamicColorsSupported()) {
                SwitchPreference(
                    titleRes = R.string.preference_dynamic_colors,
                    summary = stringResource(R.string.preference_dynamic_colors_summary),
                    checked = state.dynamicColorsEnabled,
                    onCheckedChange = actions::onDynamicThemeEnabledChange,
                    leadingIcon = Icons.Rounded.Palette
                )
            }

            SimplePreference(
                titleRes = R.string.preference_notifications,
                summary = stringResource(R.string.preference_notification_summary),
                onClick = navigateToNotificationSettings,
                leadingIcon = Icons.Rounded.Notifications
            )

            PreferenceDivider()

            SimplePreference(
                titleRes = R.string.income,
                summary = state.currentMonthlyLimit.takeIf { it.isNotEmpty() }
                    ?.let { stringResource(R.string.preference_current_income_summary, it) }
                    ?: stringResource(R.string.preference_set_income_summary),
                onClick = actions::onMonthlyLimitPreferenceClick
            )

            SimplePreference(
                titleRes = R.string.preference_auto_add_expenses,
                summary = stringResource(R.string.preference_auto_add_expense_summary),
                onClick = actions::onAutoAddExpensePreferenceClick
            )

            PreferenceDivider()

            SimplePreference(
                titleRes = R.string.preference_feedback,
                summary = stringResource(R.string.preference_feedback_summary),
                leadingIcon = Icons.Rounded.RateReview,
                onClick = actions::onFeedbackPreferenceClick
            )

            SimplePreference(
                titleRes = R.string.preference_source_code,
                leadingIcon = Icons.Rounded.Code,
                onClick = navigateToSourceCode
            )

            SimplePreference(
                titleRes = R.string.preference_version,
                leadingIcon = Icons.Rounded.Info,
                summary = BuildConfig.VERSION_NAME
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
            IncomeInputDialog(
                onConfirm = actions::onMonthlyLimitInputConfirm,
                onDismiss = actions::onMonthlyLimitInputDismiss,
                placeholderAmount = state.currentMonthlyLimit
            )
        }

        if (state.showSmsPermissionRationale) {
            PermissionRationaleDialog(
                icon = Icons.Rounded.Message,
                textRes = R.string.permission_rationale_sms_for_expense,
                onDismiss = actions::onSmsPermissionRationaleDismiss,
                onAgree = actions::onSmsPermissionRationaleAgree
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
        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
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
        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
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

@Preview(showBackground = true)
@Composable
fun PreviewSimplePreference() {
    MYMTheme {
        Surface {
            SimplePreference(
                titleRes = R.string.preference_app_theme,
                modifier = Modifier
                    .fillMaxWidth(),
                summary = stringResource(AppTheme.SYSTEM_DEFAULT.labelRes),
                leadingIcon = Icons.Rounded.BrightnessMedium
            )
        }
    }
}