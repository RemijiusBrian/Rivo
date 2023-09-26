package dev.ridill.rivo.dashboard.presentation

import android.icu.util.Currency
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.PartOfDay
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.FadedVisibility
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.MYMScaffold
import dev.ridill.rivo.core.ui.components.OnLifecycleStartEffect
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.AllExpensesScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.BottomNavDestination
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.MYMTheme
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.expense.domain.model.ExpenseListItem
import dev.ridill.rivo.expense.domain.model.ExpenseTag
import dev.ridill.rivo.expense.presentation.components.ExpenseListItem
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DashboardScreen(
    state: DashboardState,
    snackbarController: SnackbarController,
    navigateToAllExpenses: () -> Unit,
    navigateToAddEditExpense: (Long?) -> Unit,
    navigateToBottomNavDestination: (BottomNavDestination) -> Unit
) {
    val recentSpendsListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    MYMScaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                actions = {
                    BottomNavDestination.bottomNavDestinations.forEach { destination ->
                        PlainTooltipBox(tooltip = { Text(stringResource(destination.labelRes)) }) {
                            IconButton(
                                onClick = { navigateToBottomNavDestination(destination) },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .tooltipAnchor()
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.labelRes)
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navigateToAddEditExpense(null) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
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
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            Greeting(
                username = state.signedInUsername,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
                    .padding(top = SpacingMedium)
            )
            BalanceAndBudget(
                currency = state.currency,
                balance = state.balance,
                monthlyLimit = state.monthlyBudget,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            SpendsOverview(
                currency = state.currency,
                spentAmount = state.spentAmount,
                recentSpends = state.recentSpends,
                onTransactionClick = { navigateToAddEditExpense(it.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One)
                    .padding(horizontal = SpacingSmall),
                onAllExpensesClick = navigateToAllExpenses,
                listState = recentSpendsListState,
                onNavigateUpClick = {
                    coroutineScope.launch {
                        if (recentSpendsListState.isScrollInProgress)
                            recentSpendsListState.scrollToItem(Int.Zero)
                        else
                            recentSpendsListState.animateScrollToItem(Int.Zero)
                    }
                }
            )
        }
    }
}

@Composable
private fun Greeting(
    username: String?,
    modifier: Modifier = Modifier
) {
    var partOfDay by remember { mutableStateOf(PartOfDay.MORNING) }

    OnLifecycleStartEffect {
        partOfDay = DateUtil.getPartOfDay()
    }

    Column(
        modifier = modifier
    ) {
        Crossfade(targetState = partOfDay, label = "Greeting") { part ->
            Text(
                text = stringResource(R.string.app_greeting, stringResource(part.labelRes)),
                style = MaterialTheme.typography.titleMedium
            )
        }
        username?.let { name ->
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BalanceAndBudget(
    currency: Currency,
    balance: Double,
    monthlyLimit: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Balance(
            currency = currency,
            amount = balance,
            modifier = Modifier
                .alignBy(LastBaseline)
        )
        SpacerSmall()
        VerticalNumberSpinnerContent(
            number = monthlyLimit,
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            Text(
                text = stringResource(
                    R.string.fwd_slash_amount_value,
                    TextFormat.currency(amount = it, currency = currency)
                ),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        SpacerExtraSmall()
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
    currency: Currency,
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
                text = TextFormat.currency(amount = it, currency = currency),
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
    currency: Currency,
    spentAmount: Double,
    recentSpends: Map<Boolean, List<ExpenseListItem>>,
    onTransactionClick: (ExpenseListItem) -> Unit,
    onAllExpensesClick: () -> Unit,
    listState: LazyListState,
    onNavigateUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showScrollUpButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

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

            SpacerSmall()

            SpentAmountAndAllExpenses(
                currency = currency,
                amount = spentAmount,
                onAllExpensesClick = onAllExpensesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            SpacerSmall()

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
                    state = listState
                ) {
                    recentSpends.forEach { (excluded, spends) ->
                        if (spends.isNotEmpty() && excluded) {
                            stickyHeader(key = "ExcludedHeader") {
                                ListLabel(
                                    text = stringResource(R.string.excluded),
                                    modifier = Modifier
                                        .animateItemPlacement()
                                )
                            }
                        }
                        items(items = spends, key = { it.id }) { transaction ->
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
                }

                FadedVisibility(
                    visible = showScrollUpButton,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(SpacingMedium)
                ) {
                    FilledTonalIconButton(
                        onClick = onNavigateUpClick,
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
private fun SpentAmountAndAllExpenses(
    currency: Currency,
    amount: Double,
    onAllExpensesClick: () -> Unit,
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
                text = TextFormat.currency(amount = it, currency = currency),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = contentColor
            )
        }

        SpacerSmall()

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

        Spacer(weight = Float.One)

        TextButton(
            onClick = onAllExpensesClick,
            modifier = Modifier
                .alignByBaseline()
        ) {
            Text(text = "${stringResource(AllExpensesScreenSpec.labelRes)} >")
        }
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
                monthlyBudget = 5_000L
            ),
            navigateToAllExpenses = {},
            navigateToAddEditExpense = {},
            snackbarController = rememberSnackbarController(),
            navigateToBottomNavDestination = {}
        )
    }
}