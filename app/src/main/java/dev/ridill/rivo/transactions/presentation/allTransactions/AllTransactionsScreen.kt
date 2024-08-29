package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.ExcludedIcon
import dev.ridill.rivo.core.ui.components.ListEmptyIndicatorItem
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.RivoModalBottomSheet
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.navigation.destinations.AllTagsScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.AllTransactionsScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.IconSizeSmall
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.theme.elevation
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.settings.presentation.components.SwitchPreference
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.model.TagInfo
import dev.ridill.rivo.tags.presentation.components.ElevatedTagChip
import dev.ridill.rivo.transactions.domain.model.AllTransactionsMultiSelectionOption
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import dev.ridill.rivo.transactions.presentation.components.NewTransactionFab
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun AllTransactionsScreen(
    snackbarController: SnackbarController,
    tagsPagingItems: LazyPagingItems<TagInfo>,
    state: AllTransactionsState,
    actions: AllTransactionsActions,
    navigateToAllTags: () -> Unit,
    navigateToAddEditTransaction: (Long?, LocalDate?) -> Unit,
    navigateUp: () -> Unit
) {
    val isTransactionListEmpty by remember(state.transactionList) {
        derivedStateOf { state.transactionList.isEmpty() }
    }

    BackHandler(
        enabled = state.transactionMultiSelectionModeActive,
        onBack = actions::onDismissMultiSelectionMode
    )

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    RivoScaffold(
        snackbarController = snackbarController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.transactionMultiSelectionModeActive)
                            stringResource(
                                R.string.count_selected,
                                state.selectedTransactionIds.size
                            )
                        else stringResource(AllTransactionsScreenSpec.labelRes)
                    )
                },
                navigationIcon = {
                    if (state.transactionMultiSelectionModeActive) {
                        IconButton(onClick = actions::onDismissMultiSelectionMode) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_transaction_selection)
                            )
                        }
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                actions = {
                    IconButton(onClick = actions::onFilterOptionsClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(id = R.string.cd_filter_options)
                        )
                    }
                    AnimatedVisibility(visible = state.transactionMultiSelectionModeActive) {
                        IconButton(onClick = actions::onMultiSelectionOptionsClick) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_options)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            NewTransactionFab(onClick = { navigateToAddEditTransaction(null, state.selectedDate) })
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val localLayoutDirection = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(localLayoutDirection),
                    end = paddingValues.calculateEndPadding(localLayoutDirection)
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding() + PaddingScrollEnd
            )
        ) {
            item(
                key = "TagsHorizontalList",
                contentType = "TagsHorizontalList"
            ) {
                TagsInfoList(
                    currency = state.currency,
                    onAllTagsClick = navigateToAllTags,
                    tagsPagingItems = tagsPagingItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TagsRowMinHeight)
                        .animateItem()
                )
            }

            stickyHeader(
                key = "TransactionListHeader",
                contentType = "TransactionListHeader"
            ) {
                TransactionListDateFilterAndLabel(
                    selectedDate = state.selectedDate,
                    onMonthSelect = actions::onMonthSelect,
                    yearsList = state.yearsList,
                    onYearSelect = actions::onYearSelect,
                    multiSelectionModeActive = state.transactionMultiSelectionModeActive,
                    totalSumAmount = state.aggregateAmount,
                    currency = state.currency,
                    selectedTxTypeFilter = state.selectedTransactionTypeFilter,
                    listLabel = state.transactionListLabel,
                    multiSelectionState = state.transactionSelectionState,
                    onSelectionStateChange = actions::onSelectionStateChange,
                    modifier = Modifier
                        .animateItem()
                )
            }

            if (isTransactionListEmpty) {
                item(
                    key = "ListEmptyIndicator",
                    contentType = "ListEmptyIndicator"
                ) {
                    ListEmptyIndicatorItem(
                        rawResId = R.raw.lottie_empty_list_ghost,
                        messageRes = R.string.all_transactions_list_empty_message,
                        heightFraction = 0.25f
                    )
                }
            }
            items(
                items = state.transactionList,
                key = { it.id },
                contentType = { "TransactionCard" }
            ) { transaction ->
                val clickableModifier = if (state.transactionMultiSelectionModeActive) Modifier
                    .toggleable(
                        value = transaction.id in state.selectedTransactionIds,
                        onValueChange = { actions.onTransactionSelectionChange(transaction.id) }
                    )
                else Modifier.combinedClickable(
                    role = Role.Button,
                    onClick = { navigateToAddEditTransaction(transaction.id, null) },
                    onClickLabel = stringResource(R.string.cd_tap_to_edit_transaction),
                    onLongClick = { actions.onTransactionLongPress(transaction.id) },
                    onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection)
                )

                val selected = remember(state.selectedTransactionIds) {
                    transaction.id in state.selectedTransactionIds
                }

                TransactionListItem(
                    note = transaction.note,
                    amount = transaction.amountFormattedWithCurrency(state.currency),
                    date = transaction.date,
                    type = transaction.type,
                    modifier = Modifier
                        .then(clickableModifier)
                        .animateItem(),
                    tag = transaction.tag,
                    folder = transaction.folder,
                    excluded = transaction.excluded,
                    tonalElevation = if (selected) MaterialTheme.elevation.level1 else MaterialTheme.elevation.level0
                )
            }
        }
    }

    if (state.showDeleteTransactionConfirmation) {
        ConfirmationDialog(
            titleRes = R.string.delete_multiple_transaction_confirmation_title,
            contentRes = R.string.action_irreversible_message,
            onConfirm = actions::onDeleteTransactionConfirm,
            onDismiss = actions::onDeleteTransactionDismiss
        )
    }

    if (state.showAggregationConfirmation) {
        ConfirmationDialog(
            titleRes = R.string.transaction_aggregation_confirmation_title,
            contentRes = R.string.transaction_aggregation_confirmation_message,
            onConfirm = actions::onAggregationConfirm,
            onDismiss = actions::onAggregationDismiss
        )
    }

    if (state.showMultiSelectionOptions) {
        MultiSelectionOptionsSheet(
            onDismiss = actions::onMultiSelectionOptionsDismiss,
            onOptionClick = actions::onMultiSelectionOptionSelect
        )
    }

    if (state.showFilterOptions) {
        FilterOptionsSheet(
            onDismissRequest = actions::onFilterOptionsDismiss,
            selectedDate = state.selectedDate,
            yearsList = state.yearsList,
            onMonthSelect = actions::onMonthSelect,
            onYearSelect = actions::onYearSelect,
            selectedTypeFilter = state.selectedTransactionTypeFilter,
            onTypeFilterSelect = actions::onTypeFilterSelect,
            showExcluded = state.showExcludedTransactions,
            onShowExcludedToggle = actions::onShowExcludedToggle,
            selectedTags = state.selectedTagFilters,
            onClearTagSelectionClick = actions::onClearTagFilterClick,
            onChangeTagSelectionClick = actions::onChangeTagFiltersClick
        )
    }
}

private val TagsRowMinHeight = 100.dp

@Composable
private fun TagsInfoList(
    currency: Currency,
    tagsPagingItems: LazyPagingItems<TagInfo>,
    onAllTagsClick: () -> Unit,
    modifier: Modifier = Modifier,
    tagsListState: LazyListState = rememberLazyListState()
) {
    val isTagsEmpty by remember {
        derivedStateOf { tagsPagingItems.isEmpty() }
    }

    // Prevent tags list starting of at last index
    LaunchedEffect(tagsListState, isTagsEmpty) {
        if (!isTagsEmpty) {
            tagsListState.scrollToItem(0)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ListLabel(
                text = stringResource(R.string.your_top_tags),
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium),
            )
            SpacerSmall()
            TextButton(onClick = onAllTagsClick) {
                Text(text = "${stringResource(AllTagsScreenSpec.labelRes)} >")
            }
        }
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if (isTagsEmpty) {
                Text(
                    text = stringResource(R.string.top_tags_list_empty_message),
                    color = LocalContentColor.current
                        .copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
            LazyRow(
                modifier = Modifier
                    .matchParentSize(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.medium,
                    end = PaddingScrollEnd
                ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                state = tagsListState
            ) {
                items(
                    count = tagsPagingItems.itemCount,
                    key = tagsPagingItems.itemKey { it.id },
                    contentType = tagsPagingItems.itemContentType { "TagInfoCard" }
                ) { index ->
                    tagsPagingItems[index]?.let { tag ->
                        TagInfoCard(
                            name = tag.name,
                            color = tag.color,
                            isExcluded = tag.excluded,
                            createdTimestamp = tag.createdTimestampFormatted,
                            aggregateAmount = tag.aggregate,
                            currency = currency,
                            modifier = Modifier
                                .animateContentSize()
                                .fillParentMaxHeight()
                                .widthIn(
                                    min = TagInfoCardMinWidth,
                                    max = TagInfoCardMaxWidth
                                )
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

private val TagInfoCardMinWidth = 100.dp
private val TagInfoCardMaxWidth = 200.dp

@Composable
private fun TagInfoCard(
    name: String,
    color: Color,
    isExcluded: Boolean,
    createdTimestamp: String,
    currency: Currency,
    aggregateAmount: Double,
    modifier: Modifier = Modifier
) {
    val contentColor = remember(color) { color.contentColor() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        ),
        modifier = modifier
            .exclusionGraphicsLayer(isExcluded)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (isExcluded) {
                    ExcludedIcon(size = IconSizeSmall)
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineBreak = LineBreak.Heading
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
            }

            Text(
                text = createdTimestamp,
//                stringResource(R.string.created_colon_timestamp_value, createdTimestamp),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
                    .copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }
    }
}

@Composable
private fun TransactionListDateFilterAndLabel(
    selectedDate: LocalDate,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<Int>,
    onYearSelect: (Int) -> Unit,
    multiSelectionModeActive: Boolean,
    totalSumAmount: Double,
    currency: Currency,
    selectedTxTypeFilter: TransactionTypeFilter,
    listLabel: UiText,
    multiSelectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier
            .then(modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.small)
        ) {
            /*DateFilter(
                selectedDate = selectedDate,
                onMonthSelect = onMonthSelect,
                yearsList = yearsList,
                onYearSelect = onYearSelect,
                modifier = Modifier
                    .fillMaxWidth()
            )*/
            AggregateAmount(
                multiSelectionModeActive = multiSelectionModeActive,
                sumAmount = totalSumAmount,
                currency = currency,
                typeFilter = selectedTxTypeFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        vertical = MaterialTheme.spacing.small,
                        horizontal = MaterialTheme.spacing.medium
                    )
            )

            TransactionLabelHeader(
                listLabel = listLabel,
                multiSelectionModeActive = multiSelectionModeActive,
                multiSelectionState = multiSelectionState,
                onSelectionStateChange = onSelectionStateChange,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

/*@Composable
private fun DateFilter(
    selectedDate: LocalDate,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<Int>,
    onYearSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthsList = remember { Month.entries.toTypedArray() }

    val monthsListState = rememberLazyListState()

    var showYearsList by remember { mutableStateOf(false) }

    // Scroll to initial selected month
    LaunchedEffect(monthsListState) {
        monthsListState.scrollToItem(selectedDate.monthValue - 1)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateIndicator(
                date = selectedDate,
                isExpanded = showYearsList,
                onClick = { showYearsList = !showYearsList },
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
            AnimatedVisibility(
                visible = showYearsList,
                enter = slideInHorizontally { it / 2 } + fadeIn(),
                exit = slideOutHorizontally { it / 2 } + fadeOut()
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.medium,
                        end = PaddingScrollEnd
                    )
                ) {
                    items(
                        items = yearsList,
                        key = { it },
                        contentType = { "YearChip" }
                    ) { year ->
                        ElevatedFilterChip(
                            selected = year == selectedDate.year,
                            onClick = { onYearSelect(year) },
                            label = { Text(year.toString()) },
                            modifier = Modifier
                                .animateItem()
                        )
                    }
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = PaddingScrollEnd
            ),
            state = monthsListState
        ) {
            items(
                items = monthsList,
                key = { it.value },
                contentType = { "MonthChip" }
            ) { month ->
                ElevatedFilterChip(
                    selected = month == selectedDate.month,
                    onClick = { onMonthSelect(month) },
                    label = {
                        Text(
                            text = month.getDisplayName(
                                TextStyle.FULL_STANDALONE,
                                Locale.getDefault()
                            ),
                            modifier = Modifier
                                .animateItem()
                        )
                    }
                )
            }
        }
    }
}*/

/*@Composable
private fun DateIndicator(
    date: LocalDate,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndicatorRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(AnimationConstants.DefaultDurationMillis),
        label = "SelectedIndicatorRotation"
    )

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarClock,
                contentDescription = null,
                modifier = Modifier
                    .size(FloatingActionButtonDefaults.LargeIconSize)
            )
            AnimatedContent(
                targetState = date,
                label = "AnimatedDateText"
            ) { date ->
                Column {
                    Text(
                        text = date.month.getDisplayName(
                            TextStyle.FULL_STANDALONE,
                            Locale.getDefault()
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = date.year.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = stringResource(R.string.cd_show_years_list),
                modifier = Modifier
                    .rotate(selectedIndicatorRotation)
            )
        }
    }
}*/

@Composable
private fun TransactionLabelHeader(
    listLabel: UiText,
    multiSelectionModeActive: Boolean,
    multiSelectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(
            targetState = listLabel.asString(),
            label = "SelectedTagNameAnimatedLabel",
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
                .weight(Float.One)
        ) { ListLabel(text = it) }

        AnimatedVisibility(visible = multiSelectionModeActive) {
            TriStateCheckbox(
                state = multiSelectionState,
                onClick = onSelectionStateChange
            )
        }
    }
}

@Composable
private fun AggregateAmount(
    multiSelectionModeActive: Boolean,
    currency: Currency,
    sumAmount: Double,
    typeFilter: TransactionTypeFilter,
    modifier: Modifier = Modifier
) {
    val aggContentDescription = stringResource(
        R.string.cd_total_transaction_sum,
        stringResource(
            id = when {
                multiSelectionModeActive -> R.string.selected_aggregate
                else -> typeFilter.labelRes
            }
        ),
        TextFormat.currency(amount = sumAmount.absoluteValue, currency = currency)
    )
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .mergedContentDescription(aggContentDescription)
    ) {
        Crossfade(targetState = typeFilter, label = "TotalAmountLabel") { txType ->
            Text(
                text = stringResource(
                    id = when {
                        multiSelectionModeActive -> R.string.selected_aggregate
                        txType == TransactionTypeFilter.ALL -> R.string.aggregate
                        else -> txType.labelRes
                    }
                ),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SpacerSmall()

        VerticalNumberSpinnerContent(sumAmount) { amount ->
            Text(
                text = TextFormat.currency(amount = amount.absoluteValue, currency = currency),
                style = MaterialTheme.typography.headlineMedium
                    .copy(lineBreak = LineBreak.Heading),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MultiSelectionOptionsSheet(
    onDismiss: () -> Unit,
    onOptionClick: (AllTransactionsMultiSelectionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    RivoModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.medium,
                bottom = PaddingScrollEnd
            )
        ) {
            items(
                items = AllTransactionsMultiSelectionOption.entries,
                key = { it.name },
                contentType = { "MultiSelectionOptionItem" }
            ) { option ->
                OutlinedCard(
                    onClick = { onOptionClick(option) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(MaterialTheme.spacing.medium)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(option.iconRes),
                            contentDescription = null
                        )
                        SpacerMedium()
                        Text(
                            text = stringResource(option.labelRes)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterOptionsSheet(
    onDismissRequest: () -> Unit,
    selectedDate: LocalDate,
    yearsList: List<Int>,
    onMonthSelect: (Month) -> Unit,
    onYearSelect: (Int) -> Unit,
    selectedTypeFilter: TransactionTypeFilter,
    onTypeFilterSelect: (TransactionTypeFilter) -> Unit,
    selectedTags: List<Tag>,
    onChangeTagSelectionClick: () -> Unit,
    onClearTagSelectionClick: () -> Unit,
    showExcluded: Boolean,
    onShowExcludedToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    RivoModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            FilterSectionTitle(R.string.date)
            DateFilterList(
                selectedDate = selectedDate,
                onMonthSelect = onMonthSelect,
                yearsList = yearsList,
                onYearSelect = onYearSelect
            )
            SpacerMedium()

            FilterSectionTitle(R.string.filter_section_transaction_type)
            val filterEntries = remember { TransactionTypeFilter.entries }
            val filterEntriesSize = remember(filterEntries) { filterEntries.size }
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                filterEntries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = filter == selectedTypeFilter,
                        onClick = { onTypeFilterSelect(filter) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = filterEntriesSize
                        ),
                        label = { Text(stringResource(filter.labelRes)) }
                    )
                }
            }
            SpacerMedium()

            val isSelectedTagsNotEmpty by remember(selectedTags) {
                derivedStateOf { selectedTags.isNotEmpty() }
            }

            FilterSectionTitle(
                R.string.filter_section_tags,
                additionalTrailingContent = {
                    if (isSelectedTagsNotEmpty) {
                        TextButton(onClick = onClearTagSelectionClick) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = MaterialTheme.spacing.small,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.Center
                ) {
                    selectedTags.forEach { tag ->
                        ElevatedTagChip(
                            name = tag.name,
                            color = tag.color,
                            excluded = tag.excluded,
                        )
                    }
                }
                OutlinedButton(onClick = onChangeTagSelectionClick) {
                    Text(stringResource(R.string.select_tags))
                }
            }

            FilterSectionTitle(resId = R.string.filter_section_more)
            SwitchPreference(
                titleRes = R.string.show_excluded_transactions,
                value = showExcluded,
                onValueChange = onShowExcludedToggle
            )
        }
    }
}

@Composable
private fun FilterSectionTitle(
    @StringRes resId: Int,
    modifier: Modifier = Modifier,
    additionalTrailingContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ListLabel(stringResource(resId))
            additionalTrailingContent?.invoke()
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.small)
        )
    }
}

@Composable
private fun DateFilterList(
    selectedDate: LocalDate,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<Int>,
    onYearSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthsList = remember { Month.entries.toTypedArray() }

    val monthsListState = rememberLazyListState()

    // Scroll to initial selected month
    LaunchedEffect(monthsListState) {
        monthsListState.scrollToItem(selectedDate.monthValue - 1)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = PaddingScrollEnd
            )
        ) {
            items(
                items = yearsList,
                key = { it },
                contentType = { "YearChip" }
            ) { year ->
                ElevatedFilterChip(
                    selected = year == selectedDate.year,
                    onClick = { onYearSelect(year) },
                    label = { Text(year.toString()) },
                    modifier = Modifier
                        .animateItem()
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = PaddingScrollEnd
            ),
            state = monthsListState
        ) {
            items(
                items = monthsList,
                key = { it.value },
                contentType = { "MonthChip" }
            ) { month ->
                ElevatedFilterChip(
                    selected = month == selectedDate.month,
                    onClick = { onMonthSelect(month) },
                    label = {
                        Text(
                            text = month.getDisplayName(
                                TextStyle.FULL_STANDALONE,
                                Locale.getDefault()
                            ),
                            modifier = Modifier
                                .animateItem()
                        )
                    }
                )
            }
        }
    }
}