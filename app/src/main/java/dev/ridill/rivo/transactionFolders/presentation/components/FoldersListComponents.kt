package dev.ridill.rivo.transactionFolders.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.ListSearchSheet
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder

@Composable
fun FolderListSearchSheet(
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    foldersList: List<TransactionFolder>,
    onFolderClick: (TransactionFolder) -> Unit,
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
            start = SpacingMedium,
            end = SpacingMedium,
            bottom = SpacingListEnd
        ),
        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        item(key = "CreateNewGroupItem") {
            ListItem(
                headlineContent = { Text(stringResource(R.string.create_new_folder)) },
                trailingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outline_add_folder),
                        contentDescription = stringResource(R.string.cd_create_new_transaction_folder)
                    )
                },
                modifier = Modifier
                    .clickable(
                        onClick = onCreateNewClick,
                        role = Role.Button
                    )
            )
        }
        items(items = foldersList, key = { it.id }) { folder ->
            OutlinedCard(onClick = { onFolderClick(folder) }) {
                ListItem(
                    headlineContent = { Text(folder.name) },
                    modifier = modifier
                        .animateItemPlacement()
                )
            }
        }
    }
}