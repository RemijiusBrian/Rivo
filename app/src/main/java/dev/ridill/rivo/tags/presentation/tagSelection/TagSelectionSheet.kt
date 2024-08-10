package dev.ridill.rivo.tags.presentation.tagSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.ArrangementTopWithFooter
import dev.ridill.rivo.core.ui.components.RivoModalBottomSheet
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.TitleLargeText
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.presentation.components.TagChip

@Composable
fun TagSelectionSheet(
    multiSelection: Boolean,
    tagsLazyPagingItems: LazyPagingItems<Tag>,
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    selectedIds: Set<Long>,
    onItemClick: (Long) -> Unit,
    onConfirm: () -> Unit,
    navigateToAddEditTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    RivoModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = ArrangementTopWithFooter(MaterialTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleLargeText(
                    title = pluralStringResource(
                        id = R.plurals.select_tag,
                        count = if (multiSelection) 2 else 1
                    )
                )
            }
            TagSearchField(
                query = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
            )
            TextButton(onClick = navigateToAddEditTag) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                )
                Spacer(spacing = ButtonDefaults.IconSpacing)
                Text(text = stringResource(R.string.create_new_tag))
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = TagsFlowRowMinHeight)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(
                    space = MaterialTheme.spacing.small,
                    alignment = Alignment.CenterHorizontally
                )
            ) {
                repeat(tagsLazyPagingItems.itemCount) {
                    tagsLazyPagingItems[it]?.let { tag ->
                        TagChip(
                            name = tag.name,
                            color = Color(tag.colorCode),
                            excluded = tag.excluded,
                            selected = tag.id in selectedIds,
                            onClick = { onItemClick(tag.id) }
                        )
                    }
                }
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        }
    }
}

private val TagsFlowRowMinHeight = 200.dp

@Composable
private fun TagSearchField(
    query: () -> String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query(),
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        shape = CircleShape,
        placeholder = { Text(stringResource(R.string.search_tags)) },
        colors = TextFieldDefaults.colors(
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}