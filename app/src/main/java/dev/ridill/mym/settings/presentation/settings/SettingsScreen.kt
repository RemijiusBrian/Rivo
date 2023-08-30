package dev.ridill.mym.settings.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.LabelledRadioButton
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.PermissionRationaleDialog
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.components.ValueInputSheet
import dev.ridill.mym.core.ui.components.icons.Message
import dev.ridill.mym.core.ui.navigation.destinations.SettingsScreenSpec
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.AppTheme
import dev.ridill.mym.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.mym.settings.presentation.components.SwitchPreference

@Composable
fun SettingsScreen(
    snackbarController: SnackbarController,
    state: SettingsState,
    actions: SettingsActions,
    navigateUp: () -> Unit,
    navigateToNotificationSettings: () -> Unit,
    navigateToBackupSettings: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    MYMScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SettingsScreenSpec.labelRes)) },
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
            SimpleSettingsPreference(
                titleRes = R.string.preference_app_theme,
                summary = stringResource(state.appTheme.labelRes),
                onClick = actions::onAppThemePreferenceClick,
                leadingIcon = Icons.Rounded.BrightnessMedium
            )

            if (BuildUtil.isDynamicColorsSupported()) {
                SwitchPreference(
                    titleRes = R.string.preference_dynamic_colors,
                    summary = stringResource(R.string.preference_dynamic_colors_summary),
                    value = state.dynamicColorsEnabled,
                    onValueChange = actions::onDynamicThemeEnabledChange,
                    leadingIcon = Icons.Rounded.Palette
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_notifications,
                summary = stringResource(R.string.preference_notification_summary),
                onClick = navigateToNotificationSettings,
                leadingIcon = Icons.Rounded.Notifications
            )

            Divider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_budget,
                summary = state.currentMonthlyBudget.takeIf { it.isNotEmpty() }
                    ?.let { stringResource(R.string.preference_current_budget_summary, it) }
                    ?: stringResource(R.string.preference_set_budget_summary),
                onClick = actions::onMonthlyBudgetPreferenceClick
            )

            SwitchPreference(
                titleRes = R.string.preference_auto_add_expenses,
                summary = stringResource(R.string.preference_auto_add_expense_summary),
                value = state.autoAddExpenseEnabled,
                onValueChange = actions::onToggleAutoAddExpense
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_backup,
                summary = stringResource(R.string.preference_backup_summary),
                onClick = navigateToBackupSettings,
            )

            Divider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_version,
                leadingIcon = Icons.Rounded.Info,
                summary = BuildUtil.versionName
            )
        }

        if (state.showAppThemeSelection) {
            AppThemeSelectionDialog(
                currentTheme = state.appTheme,
                onDismiss = actions::onAppThemeSelectionDismiss,
                onConfirm = actions::onAppThemeSelectionConfirm
            )
        }

        if (state.showBudgetInput) {
            BudgetInputSheet(
                onConfirm = actions::onMonthlyBudgetInputConfirm,
                onDismiss = actions::onMonthlyBudgetInputDismiss,
                placeholder = state.currentMonthlyBudget,
                errorMessage = state.budgetInputError
            )
        }

        if (state.showSmsPermissionRationale) {
            PermissionRationaleDialog(
                icon = Icons.Rounded.Message,
                rationaleText = stringResource(
                    R.string.permission_rationale_read_sms, stringResource(R.string.app_name)
                ),
                onDismiss = actions::onSmsPermissionRationaleDismiss,
                onSettingsClick = actions::onSmsPermissionRationaleSettingsClick
            )
        }
    }
}

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

@Composable
fun BudgetInputSheet(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: UiText? = null,
    placeholder: String = ""
) {
    var input by remember { mutableStateOf("") }
    ValueInputSheet(
        titleRes = R.string.monthly_budget_input_title,
        inputValue = { input },
        onValueChange = { input = it },
        onDismiss = onDismiss,
        onConfirm = { onConfirm(input) },
        placeholder = placeholder,
        modifier = modifier,
        text = stringResource(R.string.monthly_budget_input_text),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        errorMessage = errorMessage
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSimplePreference() {
    MYMTheme {
        Surface {
            SimpleSettingsPreference(
                titleRes = R.string.preference_app_theme,
                modifier = Modifier
                    .fillMaxWidth(),
                summary = stringResource(AppTheme.SYSTEM_DEFAULT.labelRes),
                leadingIcon = Icons.Rounded.BrightnessMedium
            )
        }
    }
}