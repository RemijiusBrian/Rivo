package dev.ridill.mym.expense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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