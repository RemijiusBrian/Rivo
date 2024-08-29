package dev.ridill.rivo.settings.presentation.budgetUpdate

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.AmountVisualTransformation
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.navigation.destinations.UpdateBudgetSheetSpec
import dev.ridill.rivo.core.ui.util.LocalCurrencyPreference
import dev.ridill.rivo.core.ui.util.UiText

@Composable
fun UpdateBudgetSheet(
    placeholder: String,
    budgetInput: () -> String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    errorMessage: UiText?,
    modifier: Modifier = Modifier
) {
    OutlinedTextFieldSheet(
        titleRes = UpdateBudgetSheetSpec.labelRes,
        inputValue = budgetInput,
        onValueChange = onInputChange,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        placeholder = placeholder,
        modifier = modifier,
        text = stringResource(R.string.monthly_budget_input_text),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        errorMessage = errorMessage,
        visualTransformation = remember { AmountVisualTransformation() },
        prefix = { Text(LocalCurrencyPreference.current.symbol) }
    )
}