package dev.ridill.rivo.folders.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.ListSearchSheet
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.folders.domain.model.Folder

@Composable
fun FolderListSearchSheet(
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    foldersListLazyPagingItems: LazyPagingItems<Folder>,
    onFolderClick: (Folder) -> Unit,
    onCreateNewClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListSearchSheet(
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onDismiss = onDismiss,
        placeholder = stringResource(R.string.search_folder),
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = SpacingListEnd
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        item(
            key = "CreateNewFolderItem",
            contentType = "CreateNewFolderItem"
        ) {
            NewFolderItem(
                onClick = onCreateNewClick,
                modifier = Modifier
                    .animateItemPlacement()
            )
        }
        items(
            count = foldersListLazyPagingItems.itemCount,
            key = foldersListLazyPagingItems.itemKey { it.id },
            contentType = foldersListLazyPagingItems.itemContentType { "FolderCard" }
        ) { index ->
            foldersListLazyPagingItems[index]?.let { folder ->
                FolderCard(
                    name = folder.name,
                    excluded = folder.excluded,
                    onClick = { onFolderClick(folder) },
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun NewFolderItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.create_new_folder)) },
        trailingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outline_add_folder),
                contentDescription = stringResource(R.string.cd_create_new_folder)
            )
        },
        modifier = Modifier
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .then(modifier)
    )
}

@Composable
private fun FolderCard(
    name: String,
    excluded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (excluded) TextDecoration.LineThrough
                    else null
                )
            }
        )
    }
}