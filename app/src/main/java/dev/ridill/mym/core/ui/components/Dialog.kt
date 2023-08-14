package dev.ridill.mym.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.theme.SpacingMedium

@Composable
fun ConfirmationDialog(
    @StringRes titleRes: Int,
    @StringRes contentRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes confirmActionRes: Int = R.string.action_confirm,
    @StringRes dismissActionRes: Int = R.string.action_cancel,
    showDismissButton: Boolean = true,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(confirmActionRes))
            }
        },
        dismissButton = {
            if (showDismissButton) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(dismissActionRes))
                }
            }
        },
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(contentRes)) },
        modifier = modifier,
        properties = properties
    )
}

@Composable
fun ConfirmationDialog(
    @StringRes titleRes: Int,
    content: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes confirmActionRes: Int = R.string.action_confirm,
    @StringRes dismissActionRes: Int = R.string.action_cancel,
    showDismissButton: Boolean = true,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(confirmActionRes))
            }
        },
        dismissButton = {
            if (showDismissButton) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(dismissActionRes))
                }
            }
        },
        title = { Text(stringResource(titleRes)) },
        text = { Text(content) },
        modifier = modifier,
        properties = properties
    )
}

@Composable
fun TextInputDialog(
    @StringRes titleRes: Int,
    @StringRes contentRes: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isInputError: Boolean = false,
    @StringRes errorRes: Int? = null,
    @StringRes confirmActionRes: Int = R.string.action_confirm,
    @StringRes dismissActionRes: Int = R.string.action_cancel,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    var input by remember { mutableStateOf("") }
    val showSupportingText = remember(errorRes, isInputError) {
        errorRes != null && isInputError
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(input) }) {
                Text(stringResource(confirmActionRes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(dismissActionRes))
            }
        },
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                Text(stringResource(contentRes))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = true,
                    isError = isInputError,
                    shape = MaterialTheme.shapes.medium,
                    supportingText = {
                        errorRes?.let {
                            AnimatedVisibility(
                                visible = showSupportingText,
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                Text(stringResource(it))
                            }
                        }
                    },
                    placeholder = { placeholder?.let { Text(it) } }
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun BudgetInputDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isInputError: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    placeholderAmount: String? = null
) = TextInputDialog(
    titleRes = R.string.monthly_budget_input_dialog_title,
    contentRes = R.string.monthly_budget_input_dialog_content,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    isInputError = isInputError,
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
    ),
    errorRes = R.string.error_invalid_amount,
    placeholder = placeholderAmount ?: stringResource(R.string.enter_budget),
    modifier = modifier,
    focusRequester = focusRequester
)


@Composable
fun PermissionRationaleDialog(
    icon: ImageVector,
    @StringRes textRes: Int,
    onDismiss: () -> Unit,
    onAgree: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        modifier = modifier
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            contentColor = AlertDialogDefaults.titleContentColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(RationaleSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(PermissionIconSize)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(RationaleSpacing)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(textRes))

                    VerticalSpacer(RationaleSpacing)

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.action_not_now))
                        }
                        TextButton(onClick = onAgree) {
                            Text(stringResource(R.string.action_continue))
                        }
                    }
                }
            }
        }
    }
}

private val RationaleSpacing = 24.dp
private val PermissionIconSize = 40.dp