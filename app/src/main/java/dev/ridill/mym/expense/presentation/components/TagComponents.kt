package dev.ridill.mym.expense.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall

@Composable
fun NewTagChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = false,
        onClick = onClick,
        label = { Text(stringResource(R.string.new_tag_chip_label)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_create_new_tag)
            )
        },
        modifier = modifier
    )
}

@Composable
fun NewTagDialog(
    nameInput: () -> String,
    onNameChange: (String) -> Unit,
    selectedColorCode: Int?,
    onColorSelect: (Color) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    tagColors: List<Color> = TagColors,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        title = { Text(stringResource(R.string.new_tag)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                OutlinedTextField(
                    value = nameInput(),
                    onValueChange = onNameChange,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )
                )

                LazyRow(
                    contentPadding = PaddingValues(
                        end = SpacingListEnd
                    ),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                ) {
                    items(items = tagColors, key = { it.toArgb() }) { color ->
                        ColorSelector(
                            color = color,
                            selected = color.toArgb() == selectedColorCode,
                            onClick = { onColorSelect(color) },
                            modifier = Modifier
                                .animateItemPlacement()
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ColorSelector(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Box(
            modifier = Modifier
                .size(ColorSelectorSize)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = LocalContentColor.current,
                    shape = CircleShape
                )
                .background(color)
                .clickable(
                    role = Role.Button,
                    onClick = onClick,
                    onClickLabel = stringResource(R.string.cd_select_tag_color)
                )
        )
        AnimatedVisibility(visible = selected) {
            Box(
                modifier = Modifier
                    .height(ColorSelctionIndicatorHeight)
                    .width(ColorSelectorSize)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private val ColorSelectorSize = 32.dp
private val ColorSelctionIndicatorHeight = 2.dp

val TagColors: List<Color>
    get() = listOf(
        Color.Red,
        Color.Green,
        Color.Blue
    )