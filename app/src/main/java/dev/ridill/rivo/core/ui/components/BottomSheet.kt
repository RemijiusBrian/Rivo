package dev.ridill.rivo.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.UiText

@Composable
fun ValueInputSheet(
    @StringRes titleRes: Int,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    @StringRes actionLabel: Int = R.string.action_confirm,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) = ValueInputSheet(
    title = {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = SpacingMedium)
        )
    },
    inputValue = inputValue,
    onValueChange = onValueChange,
    onDismiss = onDismiss,
    actionButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingMedium)
        ) {
            Text(stringResource(actionLabel))
        }
    },
    modifier = modifier,
    text = text,
    focusRequester = focusRequester,
    errorMessage = errorMessage,
    singleLine = singleLine,
    placeholder = placeholder,
    label = label,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    contentAfterTextField = contentAfterTextField
)

@Composable
fun ValueInputSheet(
    title: @Composable () -> Unit,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    val isInputEmpty by remember {
        derivedStateOf { inputValue().isEmpty() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingMedium),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        ) {
            title()

            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = SpacingMedium)
                )
            }

            OutlinedTextField(
                value = inputValue(),
                onValueChange = onValueChange,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
                    .focusRequester(focusRequester),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                label = label?.let { { Text(it) } },
                supportingText = { errorMessage?.let { Text(it.asString()) } },
                isError = errorMessage != null,
                placeholder = placeholder?.let { { Text(it) } },
                singleLine = singleLine,
                trailingIcon = {
                    if (!isInputEmpty) {
                        IconButton(onClick = { onValueChange(String.Empty) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        }
                    }
                }
            )

            contentAfterTextField?.invoke(this)

            actionButton()
        }
    }
}