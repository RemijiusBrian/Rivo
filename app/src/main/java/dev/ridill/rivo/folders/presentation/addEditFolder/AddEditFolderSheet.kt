package dev.ridill.rivo.folders.presentation.addEditFolder

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.rivo.core.ui.components.MarkExcludedSwitch
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.UiText

@Composable
fun AddEditFolderSheet(
    isLoading: Boolean,
    name: () -> String,
    excluded: () -> Boolean?,
    errorMessage: UiText?,
    isEditMode: Boolean,
    actions: AddEditFolderActions,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit, isEditMode) {
        if (!isEditMode) {
            focusRequester.requestFocus()
        }
    }

    OutlinedTextFieldSheet(
        title = {
            Row(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(
                        id = if (!isEditMode) R.string.destination_new_folder
                        else R.string.destination_edit_folder
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .weight(Float.One)
                )

                if (isEditMode) {
                    IconButton(onClick = actions::onDeleteClick) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.cd_delete_tag)
                        )
                    }
                }
            }
        },
        inputValue = name,
        onValueChange = actions::onNameChange,
        onDismiss = onDismiss,
        text = {
            if (!isEditMode) {
                Text(
                    text = stringResource(R.string.new_tag_input_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        label = stringResource(R.string.tag_name),
        errorMessage = errorMessage,
        focusRequester = focusRequester,
        contentAfterTextField = {
            MarkExcludedSwitch(
                excluded = excluded() == true,
                onToggle = actions::onExclusionChange,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .align(Alignment.End)
            )
        },
        modifier = modifier
            .padding(bottom = PaddingScrollEnd),
        actionButton = {
            ButtonWithLoadingIndicator(
                onClick = actions::onConfirm,
                textRes = R.string.action_confirm,
                loading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    )
}