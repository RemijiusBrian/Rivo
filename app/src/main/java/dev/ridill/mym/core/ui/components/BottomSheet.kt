package dev.ridill.mym.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.util.UiText

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
    contentAfterTextField: @Composable (() -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )

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
                singleLine = singleLine
            )

            contentAfterTextField?.invoke()

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            ) {
                Text(stringResource(actionLabel))
            }
        }
    }
}