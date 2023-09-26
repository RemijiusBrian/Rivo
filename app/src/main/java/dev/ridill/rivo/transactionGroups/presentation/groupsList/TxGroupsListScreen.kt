package dev.ridill.rivo.transactionGroups.presentation.groupsList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.MYMScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.TxGroupsListScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupListItem

@Composable
fun TxGroupsListScreen(
    snackbarController: SnackbarController,
    groupsList: List<TxGroupListItem>,
    navigateToGroupDetails: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    MYMScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TxGroupsListScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToGroupDetails(null) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_new_transaction_group)
                )
            }
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (groupsList.isEmpty()) {
                EmptyListIndicator(
                    resId = R.raw.lottie_empty_list_ghost
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + SpacingListEnd
                )
            ) {
                items(items = groupsList, key = { it.id }) { group ->
                    GroupCard(
                        name = group.name,
                        created = group.createdDateFormatted,
                        aggregateAmount = group.aggregateAmountFormatted,
                        onClick = { navigateToGroupDetails(group.id) },
                        modifier = Modifier
                            .animateItemPlacement()
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    name: String,
    created: String,
    aggregateAmount: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingMedium)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = created,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = aggregateAmount,
                style = MaterialTheme.typography.displayMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}