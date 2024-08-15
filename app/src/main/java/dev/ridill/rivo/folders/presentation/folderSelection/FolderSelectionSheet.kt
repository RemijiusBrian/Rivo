package dev.ridill.rivo.folders.presentation.folderSelection

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.folders.domain.model.Folder

@Composable
fun FolderSelectionSheet(
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    foldersListLazyPagingItems: LazyPagingItems<Folder>,
    selectedId: Long?,
    onFolderSelect: (Long) -> Unit,
    onCreateNewClick: () -> Unit,
    onClearSelectionClick: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListSearchSheet(
        title = stringResource(R.string.select_folder),
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onDismiss = onDismiss,
        placeholder = stringResource(R.string.search_folder),
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = PaddingScrollEnd
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        additionalEndContent = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        }
    ) {
        stickyHeader(
            key = "ClearSelection",
            contentType = "ClearSelection"
        ) {
            Surface {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onClearSelectionClick,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text = stringResource(R.string.clear_selection),
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
        item(
            key = "CreateNewFolderItem",
            contentType = "CreateNewFolderItem"
        ) {
            NewFolderItem(
                onClick = onCreateNewClick,
                modifier = Modifier
                    .animateItem()
            )
        }
        items(
            count = foldersListLazyPagingItems.itemCount,
            key = foldersListLazyPagingItems.itemKey { it.id },
            contentType = foldersListLazyPagingItems.itemContentType { "FolderCard" }
        ) { index ->
            foldersListLazyPagingItems[index]?.let { folder ->
                FolderSelectionCard(
                    name = folder.name,
                    excluded = folder.excluded,
                    onClick = { onFolderSelect(folder.id) },
                    selected = folder.id == selectedId,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .animateItem()
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
private fun FolderSelectionCard(
    name: String,
    excluded: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        label = "FolderCardBorderColor"
    )

    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(
            width = BorderWidthStandard,
            borderColor
        )
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