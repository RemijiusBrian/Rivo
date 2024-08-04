package dev.ridill.rivo.tags.presentation.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer

/*@Composable
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
)*/

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