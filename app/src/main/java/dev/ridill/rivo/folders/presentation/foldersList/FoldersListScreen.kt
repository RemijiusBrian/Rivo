package dev.ridill.rivo.folders.presentation.foldersList

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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.FoldersListScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.exclusion
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.model.FolderSortCriteria
import dev.ridill.rivo.folders.domain.model.FoldersListOption
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlin.math.absoluteValue

@Composable
fun FoldersListScreen(
    snackbarController: SnackbarController,
    foldersList: LazyPagingItems<FolderDetails>,
    state: FoldersListState,
    actions: FoldersListActions,
    navigateToFolderDetails: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(FoldersListScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior,
                actions = {
                    FolderListOptions(
                        selectedSortCriteria = state.sortCriteria,
                        selectedSortOrder = state.sortOrder,
                        onSortOptionSelect = actions::onSortOptionSelect,
                        selectedListMode = state.listMode,
                        onListModeToggle = actions::onListModeToggle,
                        showBalancedFolders = state.showBalancedFolders,
                        onOptionSelect = actions::onListOptionSelect
                    )
                }
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToFolderDetails(null) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_add_folder),
                    contentDescription = stringResource(R.string.cd_new_folder)
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
                        messageRes = R.string.folders_list_empty_message
                    )
                }
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(
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
                    verticalItemSpacing = SpacingMedium
                ) {
                    items(
                        count = foldersList.itemCount,
                        key = foldersList.itemKey { it.id },
                        contentType = foldersList.itemContentType { "FolderListCard" }
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
private fun FolderListOptions(
    selectedSortCriteria: FolderSortCriteria,
    selectedSortOrder: SortOrder,
    onSortOptionSelect: (FolderSortCriteria) -> Unit,
    selectedListMode: ListMode,
    onListModeToggle: () -> Unit,
    showBalancedFolders: Boolean,
    onOptionSelect: (FoldersListOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortOptionsMenu(
            selectedSortCriteria = selectedSortCriteria,
            selectedSortOrder = selectedSortOrder,
            onSortOptionSelect = onSortOptionSelect
        )
        IconButton(onClick = onListModeToggle) {
            Crossfade(
                targetState = selectedListMode,
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
        FoldersListOptions(
            showBalancedFolders = showBalancedFolders,
            onOptionSelect = onOptionSelect
        )
    }
}

@Composable
private fun SortOptionsMenu(
    selectedSortCriteria: FolderSortCriteria,
    selectedSortOrder: SortOrder,
    onSortOptionSelect: (FolderSortCriteria) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { isExpanded = !isExpanded }
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = stringResource(R.string.cd_toggle_list_mode)
            )
        }

        val sortContentDescription = stringResource(
            R.string.cd_list_sorted,
            stringResource(selectedSortCriteria.labelRes),
            stringResource(selectedSortOrder.labelRes)
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .semantics {
                    contentDescription = sortContentDescription
                }
        ) {
            FolderSortCriteria.entries.forEach { criteria ->
                val selected = criteria == selectedSortCriteria
                val sortOptionContentDescription = stringResource(
                    R.string.cd_sort_option,
                    stringResource(criteria.labelRes),
                    stringResource((!selectedSortOrder).labelRes)
                )
                DropdownMenuItem(
                    text = { Text(stringResource(criteria.labelRes)) },
                    onClick = {
                        isExpanded = false
                        onSortOptionSelect(criteria)
                    },
                    trailingIcon = {
                        if (selected) {
                            Icon(
                                imageVector = selectedSortOrder.icon,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = sortOptionContentDescription
                        }
                )
            }
        }
    }
}

@Composable
private fun FoldersListOptions(
    showBalancedFolders: Boolean,
    onOptionSelect: (FoldersListOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_folders_list_option_toggle)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FoldersListOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(
                                id = if (showBalancedFolders) R.string.folder_list_option_hide_balanced
                                else R.string.folder_list_option_show_balanced
                            )
                        )
                    },
                    onClick = {
                        expanded = false
                        onOptionSelect(option)
                    }
                )
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

    val folderContentDescription = aggregateDirection?.let {
        stringResource(
            R.string.cd_folder_list_item_without_aggregate_amount,
            name,
            created,
            aggregateAmount,
            stringResource(it.labelRes)
        )
    } ?: stringResource(
        R.string.cd_folder_list_item_with_aggregate_amount,
        name,
        created
    )

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .mergedContentDescription(folderContentDescription)
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
                                textDecoration = TextDecoration.exclusion(excluded)
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
                            type = aggregateDirection,
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .fillMaxWidth(AMOUNT_TEXT_WIDTH_FRACTION)
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
                            textDecoration = TextDecoration.exclusion(excluded)
                        )

                        Text(
                            text = created,
                            style = createdDateStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        AggregateAmountText(
                            amount = aggregateAmount,
                            type = aggregateDirection,
                            horizontalAlignment = Alignment.Start
                        )
                    }
                }
            }
        }
    }
}

private const val AMOUNT_TEXT_WIDTH_FRACTION = 0.50f

@Composable
private fun AggregateAmountText(
    amount: String,
    type: TransactionType?,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier
) {
    val aggregateTypeText = stringResource(
        id = when (type) {
            TransactionType.CREDIT -> R.string.credited
            TransactionType.DEBIT -> R.string.debited
            else -> R.string.balanced
        }
    )

    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = aggregateTypeText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (type == null) TextDecoration.Underline
            else null
        )
    }
}