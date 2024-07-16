package dev.ridill.rivo.settings.presentation.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.BuildConfig
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.core.ui.components.AmountVisualTransformation
import dev.ridill.rivo.core.ui.components.ArrangementTopWithFooter
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.Image
import dev.ridill.rivo.core.ui.components.LabelledRadioButton
import dev.ridill.rivo.core.ui.components.ListSearchSheet
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.components.PermissionRationaleDialog
import dev.ridill.rivo.core.ui.components.RadioOptionListDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.TitleMediumText
import dev.ridill.rivo.core.ui.components.icons.Message
import dev.ridill.rivo.core.ui.navigation.destinations.SettingsScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.presentation.components.PreferenceIcon
import dev.ridill.rivo.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.rivo.settings.presentation.components.SwitchPreference
import java.util.Currency

@Composable
fun SettingsScreen(
    appCurrencyPreference: Currency,
    snackbarController: SnackbarController,
    state: SettingsState,
    currencySearchQuery: () -> String,
    currenciesPagingData: LazyPagingItems<Currency>,
    actions: SettingsActions,
    navigateUp: () -> Unit,
    navigateToNotificationSettings: () -> Unit,
    navigateToBackupSettings: () -> Unit,
    navigateToSecuritySettings: () -> Unit,
    launchUriInBrowser: (Uri) -> Unit
) {
    val isAccountAuthenticated by remember(state.authState) {
        derivedStateOf {
            state.authState is AuthState.Authenticated
        }
    }
    val screenArrangement = remember(isAccountAuthenticated) {
        if (isAccountAuthenticated) ArrangementTopWithFooter()
        else Arrangement.Top
    }
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
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
                .padding(top = SpacingMedium)
                .padding(bottom = SpacingListEnd),
            verticalArrangement = screenArrangement
        ) {
            AnimatedVisibility(isAccountAuthenticated) {
                val accountInfo = remember(state.authState) {
                    tryOrNull { (state.authState as AuthState.Authenticated).account }
                }
                AccountInfo(
                    photoUrl = accountInfo?.photoUrl.orEmpty(),
                    email = accountInfo?.email.orEmpty(),
                    displayName = accountInfo?.displayName.orEmpty()
                )
            }
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
                    leadingIcon = { PreferenceIcon(imageVector = Icons.Rounded.Palette) }
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_notifications,
                summary = stringResource(R.string.preference_notification_summary),
                onClick = navigateToNotificationSettings,
                leadingIcon = Icons.Rounded.Notifications
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = PreferenceDividerVerticalPadding)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_budget,
                summary = state.currentMonthlyBudget.takeIf { it > Long.Zero }
                    ?.let {
                        stringResource(
                            R.string.preference_current_budget_summary,
                            TextFormat.currency(it, appCurrencyPreference)
                        )
                    }
                    ?: stringResource(R.string.preference_set_budget_summary),
                onClick = actions::onMonthlyBudgetPreferenceClick
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_currency,
                summary = appCurrencyPreference.currencyCode,
                onClick = actions::onCurrencyPreferenceClick
            )

            SwitchPreference(
                titleRes = R.string.preference_auto_detect_transactions,
                summary = stringResource(R.string.preference_auto_detect_transactions_summary),
                value = state.autoAddTransactionEnabled,
                onValueChange = actions::onToggleAutoAddTransactions
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_backup,
                summary = stringResource(R.string.preference_backup_summary),
                onClick = navigateToBackupSettings,
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_security,
                summary = stringResource(R.string.preference_security_summary),
                onClick = navigateToSecuritySettings
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = PreferenceDividerVerticalPadding)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_source_code,
                summary = stringResource(R.string.preference_source_code_summary),
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_filled_source_code),
                onClick = {
                    launchUriInBrowser(
                        BuildConfig.SOURCE_CODE_URL.toUri()
                    )
                }
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_version,
                leadingIcon = Icons.Rounded.Info,
                summary = BuildUtil.versionName
            )

            SimpleSettingsPreference(
                titleRes = if (isAccountAuthenticated) R.string.preference_logout
                else R.string.preference_login,
                summary = stringResource(
                    if (isAccountAuthenticated) R.string.preference_logout_summary
                    else R.string.preference_login_summary
                ),
                leadingIcon = if (isAccountAuthenticated) Icons.AutoMirrored.Rounded.Logout
                else Icons.AutoMirrored.Rounded.Login,
                onClick = actions::onLoginOrLogoutPreferenceClick
            )
        }

        if (state.showAppThemeSelection) {
            RadioOptionListDialog(
                titleRes = R.string.choose_theme,
                options = AppTheme.entries,
                currentOption = state.appTheme,
                onDismiss = actions::onAppThemeSelectionDismiss,
                onOptionSelect = actions::onAppThemeSelectionConfirm
            )
        }

        if (state.showBudgetInput) {
            BudgetInputSheet(
                currency = appCurrencyPreference,
                onConfirm = actions::onMonthlyBudgetInputConfirm,
                onDismiss = actions::onMonthlyBudgetInputDismiss,
                placeholder = TextFormat.number(state.currentMonthlyBudget),
                errorMessage = state.budgetInputError
            )
        }

        if (state.showCurrencySelection) {
            CurrencySelectionSheet(
                currentCurrency = appCurrencyPreference,
                onDismiss = actions::onCurrencySelectionDismiss,
                onConfirm = actions::onCurrencySelectionConfirm,
                searchQuery = currencySearchQuery,
                onSearchQueryChange = actions::onCurrencySearchQueryChange,
                currenciesPagingData = currenciesPagingData
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

        if (state.showLogoutConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.account_logout_confirmation_title,
                contentRes = R.string.account_logout_confirmation_message,
                onConfirm = actions::onLogoutConfirm,
                onDismiss = actions::onLogoutDismiss
            )
        }
    }
}

private val PreferenceDividerVerticalPadding = 12.dp

@Composable
private fun AccountInfo(
    photoUrl: String,
    email: String,
    displayName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = SpacingMedium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                url = photoUrl,
                contentDescription = displayName,
                size = ProfileImageSize,
                placeholderRes = R.drawable.ic_rounded_person,
                errorRes = R.drawable.ic_rounded_person
            )
            SpacerMedium()
            Column {
                TitleMediumText(title = displayName)
                BodyMediumText(
                    text = email,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
        }
    }
}

private val ProfileImageSize = 40.dp

@Composable
fun BudgetInputSheet(
    currency: Currency,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: UiText? = null,
    placeholder: String = ""
) {
    var input by remember { mutableStateOf("") }
    OutlinedTextFieldSheet(
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
        errorMessage = errorMessage,
        visualTransformation = remember { AmountVisualTransformation() },
        prefix = { Text(currency.symbol) }
    )
}

@Composable
private fun CurrencySelectionSheet(
    currentCurrency: Currency,
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    currenciesPagingData: LazyPagingItems<Currency>,
    onDismiss: () -> Unit,
    onConfirm: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    ListSearchSheet(
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onDismiss = onDismiss,
        placeholder = stringResource(R.string.search_currency),
        modifier = modifier
    ) {
        items(
            count = currenciesPagingData.itemCount,
            key = currenciesPagingData.itemKey { it.currencyCode },
            contentType = currenciesPagingData.itemContentType { "CurrencySelector" }
        ) { index ->
            currenciesPagingData[index]?.let { currency ->
                LabelledRadioButton(
                    label = "${currency.displayName} (${currency.currencyCode})",
                    selected = currency.currencyCode == currentCurrency.currencyCode,
                    onClick = { onConfirm(currency) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSimplePreference() {
    RivoTheme {
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