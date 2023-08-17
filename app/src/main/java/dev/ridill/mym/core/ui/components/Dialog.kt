package dev.ridill.mym.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.ridill.mym.R

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
    @StringRes contentRes: Int,
    @StringRes tertiaryActionRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onTertiaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes confirmActionRes: Int = R.string.action_confirm,
    @StringRes dismissActionRes: Int = R.string.action_cancel,
    showDismissButton: Boolean = true,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(ButtonsMainAxisSpacing),
                verticalArrangement = Arrangement.spacedBy(ButtonsCrossAxisSpacing)
            ) {
                /* OutlinedButton(onClick = onTertiaryActionClick) {
                     Text(stringResource(tertiaryActionRes))
                 }*/
                Button(onClick = onConfirm) {
                    Text(stringResource(confirmActionRes))
                }
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

private val ButtonsMainAxisSpacing = 8.dp
private val ButtonsCrossAxisSpacing = 12.dp

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
fun PermissionRationaleDialog(
    icon: ImageVector,
    rationaleText: String,
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
                    Text(rationaleText)

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