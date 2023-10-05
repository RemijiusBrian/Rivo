package dev.ridill.rivo.transactions.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.ui.components.icons.Tags
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.folders.domain.model.TransactionFolder
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate

@Composable
fun TransactionListItem(
    note: String,
    amount: String,
    date: LocalDate,
    type: TransactionType,
    modifier: Modifier = Modifier,
    showTypeIndicator: Boolean = false,
    tag: Tag? = null,
    folder: TransactionFolder? = null,
    overlineContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    excluded: Boolean = false
) {
    val transactionListItemContentDescription = buildString {
        append(
            stringResource(
                when (type) {
                    TransactionType.CREDIT -> R.string.cd_transaction_list_item_credit
                    TransactionType.DEBIT -> R.string.cd_transaction_list_item_debit
                },
                amount,
                note,
                date.format(DateUtil.Formatters.localizedDateLong)
            )
        )

        folder?.let {
            append(String.WhiteSpace)
            append(stringResource(R.string.cd_transaction_list_item_folder_append, it.name))
        }

        tag?.let {
            append(String.WhiteSpace)
            append(stringResource(R.string.cd_transaction_list_item_tag_append, it.name))
        }
    }
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (excluded) TextDecoration.LineThrough
                    else null
                )
            }
        },
        leadingContent = { TransactionDate(date) },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (showTypeIndicator) {
                    Icon(
                        imageVector = type.directionIcon,
                        contentDescription = stringResource(type.labelRes),
                    )
                }
            }
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall)
            ) {
                folder?.let { FolderIndicator(name = it.name) }
                tag?.let { TagIndicator(it.name, it.color) }
            }
        },
        overlineContent = overlineContent,
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = transactionListItemContentDescription
            },
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
            .background(containerColor)
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Icon(
            imageVector = Icons.Rounded.Tags,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(SmallIndicatorSize)
        )
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderIndicator(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_filled_folder),
            contentDescription = null,
            modifier = Modifier
                .size(SmallIndicatorSize)
        )
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


private val SmallIndicatorSize = 12.dp