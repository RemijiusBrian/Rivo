package dev.ridill.mym.expense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.util.contentColor

@Composable
fun FilledTagChip(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = AssistChipDefaults.Height)
            .clip(AssistChipDefaults.shape)
            .background(color)
            .padding(horizontal = SpacingSmall)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = color.contentColor()
        )
    }
}

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