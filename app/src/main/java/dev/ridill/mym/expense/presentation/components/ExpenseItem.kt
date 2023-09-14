package dev.ridill.mym.expense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.ui.components.SpacerSmall
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
    shadowElevation: Dp = ListItemDefaults.Elevation,
    excluded: Boolean = false
) {
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (excluded) {
                    ExcludedIndicator()
                    SpacerSmall()
                }
                Text(
                    text = note,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
        supportingContent = { tag?.let { TagIndicator(it.name, it.color) } },
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
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = contentColorFor(containerColor)
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
                color = containerColor
            )
            .padding(SpacingSmall)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = LocalContentColor.current
            )
        }
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
            imageVector = Icons.Rounded.Tags,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(IndicatorSize)
        )
        SpacerSmall()
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExcludedIndicator() = Icon(
    imageVector = Icons.Rounded.VisibilityOff,
    contentDescription = stringResource(R.string.cd_excluded_expense),
    modifier = Modifier
        .size(IndicatorSize)
)

private val IndicatorSize = 12.dp