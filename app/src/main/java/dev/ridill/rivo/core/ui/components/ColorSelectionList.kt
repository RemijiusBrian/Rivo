package dev.ridill.rivo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.theme.RivoSelectableColorsList
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun HorizontalColorSelectionList(
    onColorSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
    colorsList: List<Color> = remember { RivoSelectableColorsList },
    selectedColorCode: () -> Int? = { null },
    contentPadding: PaddingValues = PaddingValues(
        start = MaterialTheme.spacing.medium,
        end = PaddingScrollEnd
    ),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(MaterialTheme.spacing.small)
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
        reverseLayout = reverseLayout
    ) {
        items(items = colorsList, key = { it.toArgb() }) { color ->
            val selected by remember {
                derivedStateOf { color.toArgb() == selectedColorCode() }
            }
            ColorSelector(
                color = color,
                selected = selected,
                onClick = { onColorSelect(color) },
                modifier = Modifier
                    .animateItem()
            )
        }
    }
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