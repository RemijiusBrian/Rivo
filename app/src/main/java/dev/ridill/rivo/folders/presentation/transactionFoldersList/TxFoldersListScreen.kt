package dev.ridill.rivo.folders.presentation.transactionFoldersList

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.navigation.destinations.TxFoldersListScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.isNotEmpty
import dev.ridill.rivo.folders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlin.math.absoluteValue

@Composable
fun TxFoldersListScreen(
    snackbarController: SnackbarController,
    foldersList: LazyPagingItems<TransactionFolderDetails>,
    state: TxFoldersListState,
    actions: TxFoldersListActions,
    navigateToFolderDetails: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TxFoldersListScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior,
                actions = {
                    if (foldersList.isNotEmpty()) {
                        IconButton(onClick = actions::onListModeToggle) {
                            Crossfade(
                                targetState = state.listMode,
                                label = "ListModeIcon"
                            ) { listMode ->
                                Icon(
                                    imageVector = when (listMode) {
                                        ListMode.LIST -> Icons.Rounded.ViewList
                                        ListMode.GRID -> Icons.Rounded.GridView
                                    },
                                    contentDescription = stringResource(R.string.cd_toggle_list_mode)
                                )
                            }
                        }
                    }
                }
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToFolderDetails(null) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_add_folder),
                    contentDescription = stringResource(R.string.cd_new_transaction_folder)
                )
            }
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        val localLayoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(localLayoutDirection),
                    end = paddingValues.calculateEndPadding(localLayoutDirection)
                )
                .padding(horizontal = SpacingMedium)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (foldersList.isEmpty()) {
                    EmptyListIndicator(
                        resId = R.raw.lottie_empty_list_ghost,
                        messageRes = R.string.transaction_folders_list_empty_message
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(
                        when (state.listMode) {
                            ListMode.LIST -> 1
                            ListMode.GRID -> 2
                        }
                    ),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = SpacingMedium,
                        bottom = paddingValues.calculateBottomPadding() + SpacingListEnd
                    ),
                    horizontalArrangement = Arrangement.spacedBy(SpacingMedium),
                    verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                ) {
                    items(
                        count = foldersList.itemCount,
                        key = foldersList.itemKey { it.id },
                        contentType = foldersList.itemContentType { "FolderListItem" }
                    ) { index ->
                        foldersList[index]?.let { folder ->
                            FolderCard(
                                listMode = state.listMode,
                                name = folder.name,
                                created = folder.createdDateFormatted,
                                excluded = folder.excluded,
                                aggregateDirection = folder.aggregateType,
                                aggregateAmount = TextFormat.compactNumber(
                                    value = folder.aggregateAmount.absoluteValue,
                                    currency = state.currency
                                ),
                                onClick = { navigateToFolderDetails(folder.id) },
                                modifier = Modifier
                                    .animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(
    listMode: ListMode,
    name: String,
    created: String,
    excluded: Boolean,
    aggregateAmount: String,
    aggregateDirection: TransactionType?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameStyle = MaterialTheme.typography.titleMedium
    val createdDateStyle = MaterialTheme.typography.bodySmall
        .copy(
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Crossfade(
            targetState = listMode,
            label = "FolderCardContent"
        ) { mode ->
            when (mode) {
                ListMode.LIST -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(Float.One)
                        ) {
                            Text(
                                text = name,
                                style = nameStyle,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (excluded) TextDecoration.LineThrough
                                else null
                            )

                            Text(
                                text = created,
                                style = createdDateStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        AggregateAmountText(
                            amount = aggregateAmount,
                            type = aggregateDirection
                        )
                    }
                }

                ListMode.GRID -> {
                    Column(
                        modifier = Modifier
                            .padding(SpacingMedium)
                    ) {
                        Text(
                            text = name,
                            style = nameStyle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (excluded) TextDecoration.LineThrough
                            else null
                        )

                        Text(
                            text = created,
                            style = createdDateStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        AggregateAmountText(
                            amount = aggregateAmount,
                            type = aggregateDirection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AggregateAmountText(
    amount: String,
    type: TransactionType?,
    modifier: Modifier = Modifier
) {
    val aggregateTypeText = stringResource(
        id = when (type) {
            TransactionType.CREDIT -> R.string.credited
            TransactionType.DEBIT -> R.string.debited
            else -> R.string.balanced
        }
    )

    Row(
        modifier = modifier,
    ) {
        type?.let {
            Icon(
                imageVector = it.directionIcon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alignByBaseline()
        )
        SpacerExtraSmall()
        Text(
            text = aggregateTypeText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alignByBaseline(),
            textDecoration = if (type == null) TextDecoration.Underline
            else null
        )
    }
}