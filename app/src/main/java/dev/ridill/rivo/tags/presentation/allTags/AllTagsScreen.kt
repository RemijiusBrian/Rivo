package dev.ridill.rivo.tags.presentation.allTags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.navigation.destinations.AllTagsScreenSpec
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.presentation.components.TagListItem
import dev.ridill.rivo.tags.presentation.components.TagSearchField

@Composable
fun AllTagsScreen(
    snackbarController: SnackbarController,
    tagsLazyPagingItems: LazyPagingItems<Tag>,
    searchQuery: () -> String,
    actions: AllTagsActions,
    navigateUp: () -> Unit,
    navigateToAddEditTag: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    RivoScaffold(
        snackbarController = snackbarController,
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(AllTagsScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TagSearchField(
                query = searchQuery,
                onSearchQueryChange = actions::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
            TextButton(
                onClick = { navigateToAddEditTag(null) },
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                )
                Spacer(spacing = ButtonDefaults.IconSpacing)
                Text(text = stringResource(R.string.create_new_tag))
            }
            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                )
            ) {
                items(
                    count = tagsLazyPagingItems.itemCount,
                    key = tagsLazyPagingItems.itemKey { it.id },
                    contentType = tagsLazyPagingItems.itemContentType { "TagListItem" }
                ) { index ->
                    tagsLazyPagingItems[index]?.let { tag ->
                        TagListItem(
                            name = tag.name,
                            color = tag.color,
                            excluded = tag.excluded,
                            createdTimestamp = tag.createdTimestamp.format(DateUtil.Formatters.localizedDateMedium),
                            modifier = Modifier
                                .clickable { navigateToAddEditTag(tag.id) }
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}