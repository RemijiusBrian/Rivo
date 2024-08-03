package dev.ridill.rivo.transactions.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.HorizontalColorSelectionList
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer

@Composable
fun NewTagChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = InputChip(
    selected = false,
    onClick = onClick,
    label = { Text(stringResource(R.string.new_tag_chip_label)) },
    trailingIcon = {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.create_new_tag)
        )
    },
    modifier = modifier,
    enabled = enabled
)

@Composable
fun TagChip(
    name: String,
    color: Color,
    excluded: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = FilterChip(
    selected = selected,
    onClick = onClick,
    label = {
        Text(
            text = name,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    },
    colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = color,
        selectedLabelColor = color.contentColor()
    ),
    modifier = Modifier
        .widthIn(max = TagChipMaxWidth)
        .then(modifier)
        .exclusionGraphicsLayer(excluded),
    enabled = enabled
)

private val TagChipMaxWidth = 150.dp

@Composable
fun TagInputSheet(
    nameInput: () -> String,
    onNameChange: (String) -> Unit,
    selectedColorCode: () -> Int?,
    onColorSelect: (Color) -> Unit,
    excluded: () -> Boolean?,
    onExclusionToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: UiText?,
    isEditMode: () -> Boolean,
    onDeleteClick: (() -> Unit)?
) {
    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit, isEditMode()) {
        if (!isEditMode()) {
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
                        id = if (isEditMode()) R.string.edit_tag_input_title
                        else R.string.new_tag_input_title
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .weight(Float.One)
                )

                if (isEditMode() && onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.cd_delete_tag)
                        )
                    }
                }
            }
        },
        inputValue = nameInput,
        onValueChange = onNameChange,
        onDismiss = onDismiss,
        text = {
            if (!isEditMode()) {
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
            HorizontalColorSelectionList(
                selectedColorCode = selectedColorCode,
                onColorSelect = onColorSelect
            )

            LabelledSwitch(
                labelRes = R.string.mark_excluded_question,
                checked = excluded() == true,
                onCheckedChange = onExclusionToggle,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .align(Alignment.End)
            )
        },
        modifier = modifier,
        actionButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        }
    )
}