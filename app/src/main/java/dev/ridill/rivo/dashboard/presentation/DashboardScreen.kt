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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
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
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.schedules.domain.model.UpcomingSchedule
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import java.util.Currency

@Composable
fun DashboardScreen(
    state: DashboardState,
    snackbarController: SnackbarController,
    navigateToAllTransactions: () -> Unit,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateToBottomNavDestination: (BottomNavDestination) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val areUpcomingSchedulesEmpty by remember(state.upcomingSchedules) {
        derivedStateOf { state.upcomingSchedules.isEmpty() }
    }
    val areRecentSpendsEmpty by remember(state.recentSpends) {
        derivedStateOf { state.recentSpends.isEmpty() }
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
                    FloatingActionButton(
                        onClick = { navigateToAddEditTransaction(null) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_new_transaction_fab)
                        )
                    }
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
                bottom = SpacingListEnd
            )
        ) {
            item(
                key = "BalanceAndBudget",
                contentType = "BalanceAndBudget"
            ) {
                BalanceAndBudget(
                    currency = state.currency,
                    balance = state.balance,
                    budget = state.monthlyBudgetInclCredits,
                    creditAmount = state.creditAmount,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = SpacingMedium)
                        .animateItemPlacement()
                )
            }

            if (!areUpcomingSchedulesEmpty) {
                item(
                    key = "UpcomingSchedulesRow",
                    contentType = "UpcomingSchedulesRow"
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = SpacingSmall)
                            .animateItemPlacement(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = SpacingSmall)
                                .fillParentMaxWidth()
                        ) {
                            ListLabel(
                                text = stringResource(R.string.upcoming_schedules),
                                modifier = Modifier
                                    .padding(horizontal = SpacingMedium),
                                color = MaterialTheme.colorScheme.primary
                            )

                            SpacerSmall()

                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = SpacingMedium),
                                color = MaterialTheme.colorScheme.primary
                            )

                            UpcomingSchedulesRow(
                                currency = state.currency,
                                upcomingSchedules = state.upcomingSchedules,
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
                        .animateItemPlacement()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SpacingSmall),
                        modifier = Modifier
                            .padding(horizontal = SpacingMedium)
                            .padding(top = SpacingMedium)
                    ) {
                        ListLabel(text = stringResource(R.string.recent_spends))

                        SpentAmountAndAllTransactionsButton(
                            currency = state.currency,
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
                items = state.recentSpends,
                key = { it.id },
                contentType = { "RecentSpendCard" }
            ) { transaction ->
                RecentSpendCard(
                    note = transaction.note,
                    amount = transaction.amountFormattedWithCurrency(state.currency),
                    date = transaction.date,
                    type = transaction.type,
                    tag = transaction.tag,
                    folder = transaction.folder,
                    onClick = { navigateToAddEditTransaction(transaction.id) },
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItemPlacement()
                )
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
    currency: Currency,
    balance: Double,
    budget: Double,
    creditAmount: Double,
    modifier: Modifier = Modifier
) {
    val balanceAndBudgetContentDescription = stringResource(
        R.string.cd_balance_and_budget_amounts,
        TextFormat.currency(balance, currency),
        TextFormat.currency(budget, currency)
    )
    Row(
        modifier = modifier
            .mergedContentDescription(balanceAndBudgetContentDescription),
        verticalAlignment = Alignment.Bottom
    ) {
        Balance(
            currency = currency,
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
                    TextFormat.currency(creditAmount, currency)
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
                                TextFormat.currency(amount = it, currency = currency)
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
    currency: Currency,
    amount: Double,
    onAllTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = LocalContentColor.current
    val spentAmountContentDescription = stringResource(
        R.string.cd_new_transaction_fab,
        TextFormat.currency(amount, currency)
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
            horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall)
        ) {
            VerticalNumberSpinnerContent(
                number = amount,
                modifier = Modifier
                    .weight(weight = Float.One, fill = false)
                    .alignBy(LastBaseline)
            ) {
                Text(
                    text = TextFormat.currency(amount = it, currency = currency),
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
private fun UpcomingSchedulesRow(
    currency: Currency,
    upcomingSchedules: List<UpcomingSchedule>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(
            top = SpacingMedium,
            start = SpacingMedium,
            end = SpacingListEnd
        ),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        items(
            items = upcomingSchedules,
            key = { it.id },
            contentType = { "UpcomingScheduleCard" }
        ) { schedule ->
            UpcomingScheduleCard(
                name = schedule.note,
                amount = schedule.amountFormatted(currency),
                dueDate = schedule.dueDateFormatted,
                modifier = Modifier
                    .fillParentMaxWidth(UPCOMING_SCHEDULE_CARD_PARENT_WIDTH_FRACTION)
                    .animateItemPlacement()
            )
        }
    }
}

private const val UPCOMING_SCHEDULE_CARD_PARENT_WIDTH_FRACTION = 0.80f

@Composable
private fun UpcomingScheduleCard(
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
                    horizontal = SpacingMedium,
                    vertical = SpacingSmall
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
                        append(stringResource(R.string.noted))
                        append(String.WhiteSpace)
                        append('\'')
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(name.asString())
                        }
                        append('\'')
                        append(String.WhiteSpace)
                        append(stringResource(R.string.coming_up))
                        append('.')
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(SpacingExtraSmall)
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
                upcomingSchedules = List(3) {
                    UpcomingSchedule(
                        id = it.toLong(),
                        note = UiText.DynamicString("Note"),
                        amount = 200.0,
                        dueDate = DateUtil.dateNow()
                    )
                }
            ),
            navigateToAllTransactions = {},
            navigateToAddEditTransaction = {},
            snackbarController = rememberSnackbarController(),
            navigateToBottomNavDestination = {}
        )
    }
}