package dev.ridill.mym.expense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.ui.components.HorizontalSpacer
import dev.ridill.mym.core.ui.components.icons.Tags
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.expense.domain.model.ExpenseTag
import java.time.LocalDate

@Composable
fun ExpenseListItem(
    note: String,
    amount: String,
    date: LocalDate,
    tag: ExpenseTag?,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) {
    ListItem(
        headlineContent = {
            Text(
                text = note,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            TransactionDate(
                date = date,
            )
        },
        trailingContent = {
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            tag?.let { TagIndicator(it.name, it.color) }
        },
        overlineContent = overlineContent,
        modifier = modifier,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}

@Composable
fun TransactionDate(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val dateFormatted = remember(date) {
        date.format(DateUtil.Formatters.ddth_EEE_spaceSep)
            .replace(" ", "\n")
    }
    Box(
        modifier = Modifier
            .widthIn(min = DateContainerMinWidth)
            .clip(MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.primary
                    .copy(alpha = 0.20f)
            )
            .padding(SpacingSmall)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateFormatted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

private val DateContainerMinWidth: Dp = 56.dp

@Composable
private fun TagIndicator(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Tags,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(12.dp)
        )
        HorizontalSpacer(spacing = SpacingSmall)
        Text(text = name)
    }
}