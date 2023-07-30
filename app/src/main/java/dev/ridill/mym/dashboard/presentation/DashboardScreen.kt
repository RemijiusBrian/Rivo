package dev.ridill.mym.dashboard.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Formatter
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.domain.util.PartOfDay
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.components.EmptyListIndicator
import dev.ridill.mym.core.ui.components.FadedVisibility
import dev.ridill.mym.core.ui.components.HorizontalSpacer
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.OnLifecycleStartEffect
import dev.ridill.mym.core.ui.components.TextInputDialog
import dev.ridill.mym.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.mym.core.ui.components.rememberSnackbarHostState
import dev.ridill.mym.core.ui.navigation.destinations.BottomNavDestination
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingExtraSmall
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.dashboard.domain.model.RecentSpend
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    state: DashboardState,
    actions: DashboardActions,
    snackbarHostState: SnackbarHostState,
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
                    if (state.isLimitSet) {
                        FloatingActionButton(onClick = { navigateToAddEditExpense(null) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.cd_new_expense)
                            )
                        }
                    }
                }
            )
        },
        snackbarHostState = snackbarHostState
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
                spentAmount = state.spentAmount,
                recentSpends = state.recentSpends,
                onTransactionClick = { navigateToAddEditExpense(it.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One)
                    .padding(horizontal = SpacingMedium)
            )
        }

        if (state.showLimitInput) {
            TextInputDialog(
                titleRes = R.string.monthly_limit_input_title,
                contentRes = R.string.monthly_limit_input_content,
                onConfirm = actions::onSetLimitConfirm,
                onDismiss = actions::onSetLimitDismiss,
                isInputError = state.isLimitInputError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                errorRes = R.string.error_invalid_amount,
                placeholder = stringResource(R.string.enter_monthly_limit)
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
                text = stringResource(R.string.fwd_slash_limit, Formatter.currency(it)),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        HorizontalSpacer(spacing = SpacingExtraSmall)
        Text(
            text = stringResource(R.string.monthly_limit),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .alignBy(LastBaseline)
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
                text = Formatter.currency(it),
                style = MaterialTheme.typography.displayLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentTransactionsList(
    spentAmount: Double,
    recentSpends: List<RecentSpend>,
    onTransactionClick: (RecentSpend) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val showScrollUpButton by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 3 }
    }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        shape = MaterialTheme.shapes.medium
            .copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize),
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier
                .padding(top = SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Text(
                text = stringResource(R.string.recent_spends),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )

            SpentAmount(
                amount = spentAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            Divider(
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Box(
                modifier = Modifier
                    .weight(Float.One),
                contentAlignment = Alignment.Center
            ) {
                if (recentSpends.isEmpty()) {
                    EmptyListIndicator(
                        resId = R.raw.empty_list_ghost
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        top = SpacingSmall,
                        bottom = SpacingListEnd
                    ),
                    verticalArrangement = Arrangement.spacedBy(SpacingSmall),
                    state = lazyListState
                ) {
                    items(items = recentSpends, key = { it.id }) { transaction ->
                        RecentTransactionItem(
                            note = transaction.note,
                            amount = transaction.amount,
                            dayOfMonth = transaction.dayOfMonth,
                            dayOfWeek = transaction.dayOfWeek,
                            onClick = { onTransactionClick(transaction) },
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
                    SmallFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                if (lazyListState.isScrollInProgress)
                                    lazyListState.scrollToItem(Int.Zero)
                                else
                                    lazyListState.animateScrollToItem(Int.Zero)
                            }
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                text = Formatter.currency(it),
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
private fun RecentTransactionItem(
    note: String,
    amount: String,
    dayOfMonth: String,
    dayOfWeek: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = note,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
    )
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
            actions = object : DashboardActions {
                override fun onSetLimitDismiss() {}
                override fun onSetLimitConfirm(value: String) {}
            },
            navigateToAddEditExpense = {},
            snackbarHostState = rememberSnackbarHostState(),
            navigateToBottomNavDestination = {}
        )
    }
}