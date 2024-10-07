package dev.ridill.rivo.dashboard.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.PartOfDay
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.ui.components.ListEmptyIndicatorItem
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.OnLifecycleStartEffect
import dev.ridill.rivo.core.ui.components.RivoPlainTooltip
import dev.ridill.rivo.core.ui.components.RivoRichTooltip
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.AllTransactionsScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.BottomNavDestination
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.NewTransactionFab
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@Composable
fun DashboardScreen(
    snackbarController: SnackbarController,
    recentSpends: LazyPagingItems<TransactionListItem>,
    state: DashboardState,
    navigateToAllTransactions: () -> Unit,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateToBottomNavDestination: (BottomNavDestination) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val areActiveSchedulesEmpty by remember(state.activeSchedules) {
        derivedStateOf { state.activeSchedules.isEmpty() }
    }
    val areRecentSpendsEmpty by remember {
        derivedStateOf { recentSpends.isEmpty() }
    }

    RivoScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Greeting(username = state.signedInUsername) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    BottomNavDestination.bottomNavDestinations.forEach { destination ->
                        RivoPlainTooltip(
                            tooltipText = stringResource(destination.labelRes),
                            focusable = false
                        ) {
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
                    }
                },
                floatingActionButton = {
                    NewTransactionFab(
                        onClick = { navigateToAddEditTransaction(null) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    )
                }
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                bottom = PaddingScrollEnd
            )
        ) {
            item(
                key = "BalanceAndBudget",
                contentType = "BalanceAndBudget"
            ) {
                BalanceAndBudget(
                    balance = state.balance,
                    budget = state.monthlyBudgetInclCredits,
                    creditAmount = state.creditAmount,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .animateItem()
                )
            }

            if (!areActiveSchedulesEmpty) {
                item(
                    key = "ActiveSchedulesRow",
                    contentType = "ActiveSchedulesRow"
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = MaterialTheme.spacing.small)
                            .animateItem(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = MaterialTheme.spacing.small)
                                .fillParentMaxWidth()
                        ) {
                            ListLabel(
                                text = stringResource(R.string.schedules_this_month),
                                modifier = Modifier
                                    .padding(horizontal = MaterialTheme.spacing.medium),
                                color = MaterialTheme.colorScheme.primary
                            )

                            SpacerSmall()

                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = MaterialTheme.spacing.medium),
                                color = MaterialTheme.colorScheme.primary
                            )

                            ActiveSchedulesRow(
                                activeSchedules = state.activeSchedules,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }

            stickyHeader(
                key = "RecentSpendsAmountOverview",
                contentType = "RecentSpendsAmountOverview"
            ) {
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItem()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .padding(top = MaterialTheme.spacing.medium)
                    ) {
                        ListLabel(stringResource(R.string.recent_spends))

                        SpentAmountAndAllTransactionsButton(
                            amount = state.spentAmount,
                            onAllTransactionsClick = navigateToAllTransactions,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        HorizontalDivider()
                    }
                }
            }

            if (areRecentSpendsEmpty) {
                item(
                    key = "EmptyListIndicator",
                    contentType = "EmptyListIndicator"
                ) {
                    ListEmptyIndicatorItem(
                        rawResId = R.raw.lottie_empty_list_ghost,
                        messageRes = R.string.recent_spends_list_empty_message
                    )
                }
            }

            items(
                count = recentSpends.itemCount,
                key = recentSpends.itemKey { it.id },
                contentType = recentSpends.itemContentType { "RecentSpendCard" }
            ) { index ->
                recentSpends[index]?.let { transaction ->
                    RecentSpendCard(
                        note = transaction.note,
                        amount = transaction.amountFormatted,
                        date = transaction.date,
                        type = transaction.type,
                        tag = transaction.tag,
                        folder = transaction.folder,
                        onClick = { navigateToAddEditTransaction(transaction.id) },
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .animateItem()
                    )
                }
            }
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

    val isUsernameAvailable = remember(username) { !username.isNullOrEmpty() }

    Column(
        modifier = modifier
            .mergedContentDescription(
                contentDescription = stringResource(
                    R.string.cd_app_greeting_user,
                    stringResource(partOfDay.labelRes),
                    username.orEmpty()
                )
            )
    ) {
        Crossfade(targetState = partOfDay, label = "Greeting") { part ->
            Text(
                text = stringResource(R.string.app_greeting, stringResource(part.labelRes)),
                style = (if (isUsernameAvailable) MaterialTheme.typography.titleMedium
                else LocalTextStyle.current)
                    .copy(textMotion = TextMotion.Animated)
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
    balance: Double,
    budget: Double,
    creditAmount: Double,
    modifier: Modifier = Modifier
) {
    val balanceAndBudgetContentDescription = stringResource(
        R.string.cd_balance_and_budget_amounts,
        TextFormat.currencyAmount(balance),
        TextFormat.currencyAmount(budget)
    )
    Row(
        modifier = modifier
            .mergedContentDescription(balanceAndBudgetContentDescription),
        verticalAlignment = Alignment.Bottom
    ) {
        Balance(
            amount = balance,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
                .alignBy(LastBaseline)
        )
        SpacerSmall()
        Box(
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            RivoRichTooltip(
                tooltipTitle = stringResource(R.string.budget_includes_credited_amounts),
                tooltipText = stringResource(
                    R.string.budget_includes_credit_amount_of_value,
                    TextFormat.currencyAmount(creditAmount)
                ),
                state = rememberTooltipState(isPersistent = true)
            ) {
                Row {
                    VerticalNumberSpinnerContent(
                        number = budget,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.fwd_slash_amount_value,
                                TextFormat.currencyAmount(amount = it)
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SpacerExtraSmall()
                    Text(
                        text = stringResource(R.string.budget_asterisk),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    )
                }
            }
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
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
        )
        VerticalNumberSpinnerContent(number = amount) {
            Text(
                text = TextFormat.currencyAmount(amount = it),
                style = MaterialTheme.typography.displayLarge
                    .copy(lineBreak = LineBreak.Simple),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SpentAmountAndAllTransactionsButton(
    amount: Double,
    onAllTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = LocalContentColor.current
    val spentAmountContentDescription = stringResource(
        R.string.cd_recent_spent_amount,
        TextFormat.currencyAmount(amount)
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
                .alignBy(LastBaseline)
                .mergedContentDescription(spentAmountContentDescription),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            VerticalNumberSpinnerContent(
                number = amount,
                modifier = Modifier
                    .weight(weight = Float.One, fill = false)
                    .alignBy(LastBaseline)
            ) {
                Text(
                    text = TextFormat.currencyAmount(amount = it),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor
                )
            }

            Text(
                text = stringResource(R.string.spent_this_month),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .alignBy(LastBaseline),
                color = contentColor.copy(
                    alpha = ContentAlpha.SUB_CONTENT
                ),
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }

        TextButton(
            onClick = onAllTransactionsClick,
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            Text("${stringResource(AllTransactionsScreenSpec.labelRes)} >")
        }
    }
}

@Composable
private fun ActiveSchedulesRow(
    activeSchedules: List<ActiveSchedule>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(
            top = MaterialTheme.spacing.medium,
            start = MaterialTheme.spacing.medium,
            end = PaddingScrollEnd
        ),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        items(
            items = activeSchedules,
            key = { it.id },
            contentType = { "ActiveScheduleCard" }
        ) { schedule ->
            ActiveScheduleCard(
                name = schedule.note,
                amount = schedule.amountFormatted,
                dueDate = schedule.dueDateFormatted,
                modifier = Modifier
                    .fillParentMaxWidth(UPCOMING_SCHEDULE_CARD_PARENT_WIDTH_FRACTION)
                    .animateItem()
            )
        }
    }
}

private const val UPCOMING_SCHEDULE_CARD_PARENT_WIDTH_FRACTION = 0.80f

@Composable
private fun ActiveScheduleCard(
    name: UiText,
    amount: String,
    dueDate: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                )
                .heightIn(min = UpcomingScheduleCardMinHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.payment_of_amount))
                        append(String.WhiteSpace)
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(amount)
                        }
                        append(String.WhiteSpace)
                        append(stringResource(R.string.for_note))
                        append(String.WhiteSpace)
                        withStyle(
                            SpanStyle(
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(name.asString())
                        }
                        append('.')
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.extraSmall)
                )
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.due_on_date, dueDate),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val UpcomingScheduleCardMinHeight = 100.dp

@Composable
private fun RecentSpendCard(
    note: String,
    amount: String,
    date: LocalDate,
    type: TransactionType,
    tag: Tag?,
    folder: Folder?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = Card(
    onClick = onClick,
    modifier = modifier
) {
    TransactionListItem(
        note = note,
        amount = amount,
        date = date,
        type = type,
        tag = tag,
        folder = folder
    )
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PreviewDashboardScreen() {
    RivoTheme {
        DashboardScreen(
            state = DashboardState(
                balance = 1_000.0,
                spentAmount = 500.0,
                monthlyBudgetInclCredits = 5_000.0,
                activeSchedules = List(3) {
                    ActiveSchedule(
                        id = it.toLong(),
                        note = UiText.DynamicString("Really long transaction note"),
                        amount = 200.0,
                        dueDate = DateUtil.now()
                    )
                }
            ),
            navigateToAllTransactions = {},
            navigateToAddEditTransaction = {},
            snackbarController = rememberSnackbarController(),
            navigateToBottomNavDestination = {},
            recentSpends = flowOf(PagingData.empty<TransactionListItem>()).collectAsLazyPagingItems()
        )
    }
}