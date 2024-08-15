package dev.ridill.rivo.tags.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer
import dev.ridill.rivo.tags.domain.model.Tag

@Composable
fun TopTagsSelectorFlowRow(
    topTagsLazyPagingItems: LazyPagingItems<Tag>,
    selectedTagId: Long?,
    onTagClick: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        BodyMediumText(stringResource(R.string.tag_your_transaction))
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            repeat(topTagsLazyPagingItems.itemCount) { index ->
                topTagsLazyPagingItems[index]?.let { tag ->
                    TagChip(
                        name = tag.name,
                        color = tag.color,
                        excluded = tag.excluded,
                        selected = tag.id == selectedTagId,
                        onClick = { onTagClick(tag.id) }
                    )
                }
            }

            AssistChip(
                onClick = onViewAllClick,
                label = { Text(stringResource(R.string.view_all)) },
                border = null
            )
        }
    }
}

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