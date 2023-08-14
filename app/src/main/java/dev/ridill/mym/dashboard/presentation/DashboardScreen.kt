package dev.ridill.mym.dashboard.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.domain.util.PartOfDay
import dev.ridill.mym.core.domain.util.TextFormatUtil
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.components.EmptyListIndicator
import dev.ridill.mym.core.ui.components.FadedVisibility
import dev.ridill.mym.core.ui.components.HorizontalSpacer
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.OnLifecycleStartEffect
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.core.ui.navigation.destinations.BottomNavDestination
import dev.ridill.mym.core.ui.theme.ElevationLevel1
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingExtraSmall
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.presentation.components.ExpenseListItem
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DashboardScreen(
    state: DashboardState,
    snackbarController: SnackbarController,
    navigateToAddEditExpense: (Long?) -> Unit,
    navigateToBottomNavDestination: (BottomNavDestination) -> Unit
) {
    MYMScaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                actions = {
                    BottomNavDestination.bottomNavDestinations.forEach { destination ->
                        IconButton(
                            onClick = { navigateToBottomNavDestination(destination) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(destination.iconRes),
                                contentDescription = stringResource(destination.labelRes)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navigateToAddEditExpense(null) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_new_expense)
                        )
                    }
                }
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(SpacingLarge)
        ) {
            Greeting(
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
                    .padding(top = SpacingMedium)
            )
            BalanceAndLimit(
                balance = state.balance,
                monthlyLimit = state.monthlyLimit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            SpendsOverview(
                spentAmount = state.spentAmount,
                recentSpends = state.recentSpends,
                onTransactionClick = { navigateToAddEditExpense(it.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One)
                    .padding(horizontal = SpacingMedium)
            )
        }
    }
}

@Composable
private fun Greeting(
    modifier: Modifier = Modifier
) {
    var partOfDay by remember { mutableStateOf(PartOfDay.MORNING) }

    OnLifecycleStartEffect {
        partOfDay = DateUtil.getPartOfDay()
    }

    Crossfade(targetState = partOfDay, label = "Greeting") { part ->
        Text(
            text = stringResource(R.string.app_greeting, stringResource(part.labelRes)),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
        )
    }
}

@Composable
private fun BalanceAndLimit(
    balance: Double,
    monthlyLimit: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Balance(
            amount = balance,
            modifier = Modifier
                .alignBy(LastBaseline)
        )
        HorizontalSpacer(spacing = SpacingSmall)
        VerticalNumberSpinnerContent(
            number = monthlyLimit,
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            Text(
                text = stringResource(R.string.fwd_slash_amount_value, TextFormatUtil.currency(it)),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalSpacer(spacing = SpacingExtraSmall)
        Text(
            text = stringResource(R.string.budget),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .alignBy(LastBaseline),
            maxLines = 1
        )
    }
}

@Composable
private fun Balance(
    amount: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.your_balance),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
        )
        VerticalNumberSpinnerContent(number = amount) {
            Text(
                text = TextFormatUtil.currency(it),
                style = MaterialTheme.typography.displayLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SpendsOverview(
    spentAmount: Double,
    recentSpends: List<ExpenseListItem>,
    onTransactionClick: (ExpenseListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val showScrollUpButton by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 3 }
    }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        shape = MaterialTheme.shapes.medium
            .copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize),
        modifier = modifier,
        tonalElevation = ElevationLevel1
    ) {
        Column(
            modifier = Modifier
                .padding(top = SpacingMedium)
        ) {
            Text(
                text = stringResource(R.string.recent_spends),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )

            VerticalSpacer(spacing = SpacingSmall)

            SpentAmount(
                amount = spentAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            VerticalSpacer(spacing = SpacingSmall)

            Divider(
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One),
                contentAlignment = Alignment.Center
            ) {
                if (recentSpends.isEmpty()) {
                    EmptyListIndicator(
                        resId = R.raw.lottie_empty_list_ghost
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .matchParentSize(),
                    contentPadding = PaddingValues(
                        top = SpacingSmall,
                        bottom = SpacingListEnd,
                        start = SpacingSmall,
                        end = SpacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(SpacingSmall),
                    state = lazyListState
                ) {
                    items(items = recentSpends, key = { it.id }) { transaction ->
                        RecentSpend(
                            note = transaction.note,
                            amount = transaction.amount,
                            date = transaction.date,
                            onClick = { onTransactionClick(transaction) },
                            tag = transaction.tag,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                        )
                    }
                }

                FadedVisibility(
                    visible = showScrollUpButton,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(SpacingMedium)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (lazyListState.isScrollInProgress)
                                    lazyListState.scrollToItem(Int.Zero)
                                else
                                    lazyListState.animateScrollToItem(Int.Zero)
                            }
                        },
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = stringResource(R.string.cd_scroll_to_top)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpentAmount(
    amount: Double,
    modifier: Modifier = Modifier
) {
    val contentColor = LocalContentColor.current
    Row(
        modifier = modifier
    ) {
        VerticalNumberSpinnerContent(
            number = amount,
            modifier = Modifier
                .alignByBaseline()
        ) {
            Text(
                text = TextFormatUtil.currency(it),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = contentColor
            )
        }

        HorizontalSpacer(spacing = SpacingSmall)

        Text(
            text = stringResource(R.string.spent_this_month),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .alignByBaseline(),
            color = contentColor.copy(
                alpha = 0.80f
            ),
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun RecentSpend(
    note: String,
    amount: String,
    date: LocalDate,
    onClick: () -> Unit,
    tag: ExpenseTag?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        ExpenseListItem(
            note = note,
            amount = amount,
            date = date,
            tag = tag
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDashboardScreen() {
    MYMTheme {
        DashboardScreen(
            state = DashboardState(
                balance = 1_000.0,
                spentAmount = 500.0,
                monthlyLimit = 5_000L
            ),
            navigateToAddEditExpense = {},
            snackbarController = rememberSnackbarController(),
            navigateToBottomNavDestination = {}
        )
    }
}