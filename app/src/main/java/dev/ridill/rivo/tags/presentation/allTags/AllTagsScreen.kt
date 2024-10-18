package dev.ridill.rivo.tags.presentation.allTags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SearchField
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.icons.Tags
import dev.ridill.rivo.core.ui.components.listEmptyIndicator
import dev.ridill.rivo.core.ui.navigation.destinations.AllTagsScreenSpec
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.presentation.components.TagListItem

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
    val isTagsListEmpty by remember(tagsLazyPagingItems) {
        derivedStateOf { tagsLazyPagingItems.isEmpty() }
    }
    RivoScaffold(
        snackbarController = snackbarController,
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(AllTagsScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddEditTag(null) }) {
                Icon(
                    imageVector = Icons.Rounded.Tags,
                    contentDescription = stringResource(R.string.cd_create_new_tag)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchField(
                query = searchQuery,
                onSearchQueryChange = actions::onSearchQueryChange,
                placeholder = stringResource(R.string.search_tags),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                )
            ) {
                listEmptyIndicator(
                    isListEmpty = isTagsListEmpty,
                    messageRes = R.string.all_tags_list_empty_message
                )

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