package dev.ridill.rivo.expense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.ValueInputSheet
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.UiText

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
    tagColors: List<Color> = TagColors,
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

    ValueInputSheet(
        title = {
            Row(
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
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
        text = if (!isEditMode()) stringResource(R.string.new_tag_input_text) else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        label = stringResource(R.string.tag_name),
        errorMessage = errorMessage,
        focusRequester = focusRequester,
        contentAfterTextField = {
            LazyRow(
                contentPadding = PaddingValues(
                    start = SpacingMedium,
                    end = SpacingListEnd
                ),
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                items(items = tagColors, key = { it.toArgb() }) { color ->
                    ColorSelector(
                        color = color,
                        selected = color.toArgb() == selectedColorCode(),
                        onClick = { onColorSelect(color) },
                        modifier = Modifier
                            .animateItemPlacement()
                    )
                }
            }

            LabelledSwitch(
                labelRes = R.string.action_mark_excluded,
                checked = excluded() == true,
                onCheckedChange = onExclusionToggle,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
                    .align(Alignment.End)
            )
        },
        modifier = modifier,
        actionButton = {
            Column {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingMedium)
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            }
        }
    )
}

@Composable
private fun ColorSelector(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(ColorSelectorSize)
            .clip(CircleShape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectedColor
                else LocalContentColor.current,
                shape = CircleShape
            )
            .background(color)
            .clickable(
                role = Role.Button,
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_select_tag_color)
            )
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.cd_tag_color_selected),
                tint = selectedColor
            )
        }
    }
}

private val ColorSelectorSize = 32.dp

val TagColors: List<Color>
    get() = listOf(
        Color(0xFF77172E),
        Color(0xFF692C18),
        Color(0xFF7C4A03),
        Color(0xFF274D3B),
        Color(0xFF0D635D),
        Color(0xFF246377),
        Color(0xFF284255),
        Color(0xFF472E5B),
        Color(0xFF6C3A4F),
        Color(0xFF4B443A),
        Color(0xFF232427)
    ).map { it.copy(alpha = TAG_COLOR_ALPHA) }

private const val TAG_COLOR_ALPHA = 0.64f