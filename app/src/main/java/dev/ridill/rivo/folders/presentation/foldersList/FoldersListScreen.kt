package dev.ridill.rivo.folders.presentation.foldersList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.AmountWithArrow
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.ExcludedIndicatorSmall
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.FoldersListScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.folders.domain.model.AggregateType
import dev.ridill.rivo.folders.domain.model.FolderUIModel
import kotlin.math.absoluteValue

@Composable
fun FoldersListScreen(
    snackbarController: SnackbarController,
    foldersPagingItems: LazyPagingItems<FolderUIModel>,
    navigateToAddFolder: () -> Unit,
    navigateToFolderDetails: (Long) -> Unit,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val areFoldersListEmpty by remember {
        derivedStateOf { foldersPagingItems.isEmpty() }
    }
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(FoldersListScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToAddFolder) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_add_folder),
                    contentDescription = stringResource(R.string.cd_new_folder)
                )
            }
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (areFoldersListEmpty) {
                    EmptyListIndicator(
                        rawResId = R.raw.lottie_empty_list_ghost,
                        messageRes = R.string.folders_list_empty_message
                    )
                }
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.medium,
                        bottom = PaddingScrollEnd,
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium
                    ),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalItemSpacing = MaterialTheme.spacing.medium
                ) {
                    repeat(foldersPagingItems.itemCount) { index ->
                        foldersPagingItems[index]?.let { item ->
                            when (item) {
                                is FolderUIModel.AggregateTypeSeparator -> {
                                    item(
                                        key = item.type.name,
                                        contentType = "AggregateTypeSeparator",
                                        span = StaggeredGridItemSpan.FullLine
                                    ) {
                                        ListSeparator(
                                            label = stringResource(item.type.labelRes),
                                            modifier = Modifier
                                                .animateItem()
                                        )
                                    }
                                }

                                is FolderUIModel.FolderListItem -> {
                                    item(
                                        key = item.folderDetails.id,
                                        contentType = "FolderCard"
                                    ) {
                                        FolderCard(
                                            name = item.folderDetails.name,
                                            created = item.folderDetails.createdDateFormatted,
                                            excluded = item.folderDetails.excluded,
                                            aggregateAmount = TextFormat.compactAmount(
                                                item.folderDetails.aggregateAmount.absoluteValue
                                            ),
                                            aggregateType = item.folderDetails.aggregateType,
                                            onClick = { navigateToFolderDetails(item.folderDetails.id) },
                                            modifier = Modifier
                                                .animateItem()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(
    name: String,
    created: String,
    excluded: Boolean,
    aggregateAmount: String,
    aggregateType: AggregateType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameStyle = MaterialTheme.typography.titleMedium
    val createdDateStyle = MaterialTheme.typography.bodySmall
        .copy(
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )

    val folderContentDescription = when (aggregateType) {
        AggregateType.BALANCED -> stringResource(
            R.string.cd_folder_list_item_with_aggregate_amount,
            name,
            created
        )

        else -> stringResource(
            R.string.cd_folder_list_item_without_aggregate_amount,
            name,
            created,
            aggregateAmount,
            stringResource(aggregateType.labelRes)
        )
    }

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .mergedContentDescription(folderContentDescription)
            .exclusionGraphicsLayer(excluded)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (excluded) {
                    ExcludedIndicatorSmall()
                }
                Text(
                    text = name,
                    style = nameStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = created,
                style = createdDateStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AmountWithArrow(
                value = aggregateAmount,
                type = aggregateType,
                modifier = Modifier
                    .align(Alignment.End)
            )
        }
    }
}