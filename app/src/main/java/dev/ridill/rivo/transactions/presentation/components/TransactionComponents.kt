package dev.ridill.rivo.transactions.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.ui.components.icons.Tags
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel0
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer
import dev.ridill.rivo.folders.domain.model.Folder
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
    folder: Folder? = null,
    excluded: Boolean = false,
    overlineContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) {
    val isNoteEmpty = remember(note) { note.isEmpty() }
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

        tag?.let {
            append(String.WhiteSpace)
            append(stringResource(R.string.cd_transaction_list_item_tag_append, it.name))
        }

        folder?.let {
            append(String.WhiteSpace)
            append(stringResource(R.string.cd_transaction_list_item_folder_append, it.name))
        }
    }
    ListItem(
        headlineContent = {
            Text(
                text = note
                    .ifEmpty { stringResource(type.labelRes) },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth(),
                color = LocalContentColor.current.copy(
                    alpha = if (isNoteEmpty) ContentAlpha.SUB_CONTENT
                    else Float.One
                ),
                style = LocalTextStyle.current.copy(
                    fontStyle = if (note.isEmpty()) FontStyle.Italic
                    else null
                )
            )
            // FIXME: note text not visible if amount text is too long
        },
        leadingContent = { TransactionDate(date) },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(weight = Float.One, fill = false)
                )
                if (showTypeIndicator) {
                    Icon(
                        imageVector = ImageVector.vectorResource(type.iconRes),
                        contentDescription = stringResource(type.labelRes),
                        modifier = Modifier
                            .size(TypeIndicatorSize)
                    )
                }
            }
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                tag?.let {
                    TagIndicator(
                        name = it.name,
                        color = Color(it.colorCode),
                        modifier = Modifier
                            .weight(weight = Float.One, fill = false)
                    )
                }
                folder?.let {
                    FolderIndicator(
                        name = it.name,
                        modifier = Modifier
                            .weight(weight = Float.One, fill = false)
                    )
                }
            }
        },
        overlineContent = overlineContent,
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = transactionListItemContentDescription
            }
            .then(modifier)
            .exclusionGraphicsLayer(excluded),
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}

private val TypeIndicatorSize = 16.dp

@Composable
fun TransactionDate(
    date: LocalDate,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = ElevationLevel0,
    contentPadding: PaddingValues = PaddingValues(SpacingSmall)
) {
    val dateFormatted = remember(date) {
        date.format(DateUtil.Formatters.ddth_EEE_spaceSep)
            .replace(" ", "\n")
    }
    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = DateContainerMinWidth)
                .padding(contentPadding)
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

@Composable
fun NewTransactionFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation()
) {
    FloatingActionButton(
        onClick = onClick,
        elevation = elevation,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.cd_new_transaction_fab)
        )
    }
}