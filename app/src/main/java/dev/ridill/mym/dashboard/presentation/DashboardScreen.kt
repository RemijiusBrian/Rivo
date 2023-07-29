package dev.ridill.mym.dashboard.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Formatter
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.domain.util.PartOfDay
import dev.ridill.mym.core.ui.components.HorizontalSpacer
import dev.ridill.mym.core.ui.components.OnLifecycleStartEffect
import dev.ridill.mym.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.dashboard.domain.model.RecentTransaction

@Composable
fun DashboardScreen(
    state: DashboardState,
    navigateToAddEditExpense: (Long?) -> Unit
) {
    val listState = rememberLazyListState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(onClick = { navigateToAddEditExpense(null) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_new_expense)
                        )
                    }
                }
            )
        }
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

            RecentTransactionsList(
                listState = listState,
                spentAmount = state.spentAmount,
                recentTransactions = state.recentTransactions,
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

    Crossfade(targetState = partOfDay) { part ->
        Text(
            text = stringResource(R.string.app_greeting, stringResource(part.labelRes)),
            color = MaterialTheme.colorScheme.primary,
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
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        Balance(
            amount = balance,
            modifier = Modifier
                .alignBy(LastBaseline)
        )
        VerticalNumberSpinnerContent(
            number = monthlyLimit,
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            Text(
                text = stringResource(R.string.fwd_slash_limit, Formatter.currency(it)),
                style = MaterialTheme.typography.titleLarge,
            )
        }
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
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        VerticalNumberSpinnerContent(number = amount) {
            Text(
                text = Formatter.currency(it),
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}

@Composable
private fun RecentTransactionsList(
    listState: LazyListState,
    spentAmount: Double,
    recentTransactions: List<RecentTransaction>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium
            .copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize),
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = SpacingMedium)
                .padding(top = SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Text(
                text = stringResource(R.string.recent_spends),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            SpentAmount(
                amount = spentAmount,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Divider(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    top = SpacingSmall,
                    bottom = SpacingListEnd
                ),
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                items(items = recentTransactions, key = { it.id }) { transaction ->
                    RecentTransactionCard(
                        note = transaction.note,
                        amount = transaction.amount,
                        dayOfMonth = transaction.dayOfMonth,
                        dayOfWeek = transaction.dayOfWeek,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                    )
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
    Row(
        modifier = modifier
    ) {
        VerticalNumberSpinnerContent(
            number = amount,
            modifier = Modifier
                .alignByBaseline()
        ) {
            Text(
                text = Formatter.currency(it),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalSpacer(spacing = SpacingSmall)

        Text(
            text = stringResource(R.string.spent_this_month),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .alignByBaseline()
        )
    }
}

@Composable
private fun RecentTransactionCard(
    note: String,
    amount: String,
    dayOfMonth: String,
    dayOfWeek: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        ListItem(
            headlineContent = { Text(note) },
            leadingContent = {
                TransactionDate(
                    dayOfMonth = dayOfMonth,
                    dayOfWeek = dayOfWeek
                )
            },
            trailingContent = {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun TransactionDate(
    dayOfMonth: String,
    dayOfWeek: String,
    modifier: Modifier = Modifier
) {
    ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
        Column(
            modifier = Modifier
                .widthIn(min = DateContainerMinWidth)
                .clip(MaterialTheme.shapes.small)
                .background(
                    color = MaterialTheme.colorScheme.primary
                        .copy(alpha = 0.12f)
                )
                .padding(SpacingSmall)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(dayOfMonth)
            Text(dayOfWeek)
        }
    }
}

private val DateContainerMinWidth = 56.dp

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
            navigateToAddEditExpense = {}
        )
    }
}