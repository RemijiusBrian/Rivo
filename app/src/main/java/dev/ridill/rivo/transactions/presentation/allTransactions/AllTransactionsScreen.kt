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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.ExcludedIcon
import dev.ridill.rivo.core.ui.components.ListEmptyIndicatorItem
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.ListSeparator
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
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import dev.ridill.rivo.transactions.presentation.components.NewTransactionFab
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
fun AllTransactionsScreen(
    snackbarController: SnackbarController,
    tagsPagingItems: LazyPagingItems<TagInfo>,
    transactionsLazyPagingItems: LazyPagingItems<TransactionListItemUIModel>,
    state: AllTransactionsState,
    actions: AllTransactionsActions,
    navigateToAllTags: () -> Unit,
    navigateToAddEditTransaction: (Long?, LocalDate?) -> Unit,
    navigateUp: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val areTransactionsEmpty by remember {
        derivedStateOf { transactionsLazyPagingItems.isEmpty() }
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
                    tagsPagingItems = tagsPagingItems,
                    onAllTagsClick = navigateToAllTags,
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
                TransactionListLabel(
                    multiSelectionModeActive = state.transactionMultiSelectionModeActive,
                    totalSumAmount = state.aggregateAmount,
                    selectedTxTypeFilter = state.selectedTransactionTypeFilter,
                    listLabel = state.transactionListLabel,
                    modifier = Modifier
                        .animateItem()
                )
            }

            if (areTransactionsEmpty) {
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
            repeat(transactionsLazyPagingItems.itemCount) { index ->
                transactionsLazyPagingItems[index]?.let { item ->
                    when (item) {
                        is TransactionListItemUIModel.DateSeparator -> {
                            stickyHeader(
                                key = item.date.toString(),
                                contentType = "TransactionDateSeparator"
                            ) {
                                ListSeparator(
                                    label = item.date.format(DateUtil.Formatters.MMMM_yyyy_spaceSep),
                                    modifier = Modifier
                                        .animateItem()
                                )
                            }
                        }

                        is TransactionListItemUIModel.TransactionItem -> {
                            item(
                                key = item.transaction.id,
                                contentType = "TransactionListItem"
                            ) {
                                val selected = remember(state.selectedTransactionIds) {
                                    item.transaction.id in state.selectedTransactionIds
                                }
                                val clickableModifier =
                                    if (state.transactionMultiSelectionModeActive) Modifier
                                        .toggleable(
                                            value = selected,
                                            onValueChange = {
                                                actions.onTransactionSelectionChange(item.transaction.id)
                                            }
                                        )
                                    else Modifier.combinedClickable(
                                        role = Role.Button,
                                        onClick = {
                                            navigateToAddEditTransaction(item.transaction.id, null)
                                        },
                                        onClickLabel = stringResource(R.string.cd_tap_to_edit_transaction),
                                        onLongClick = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            actions.onTransactionLongPress(item.transaction.id)
                                        },
                                        onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection)
                                    )

                                TransactionListItem(
                                    note = item.transaction.note,
                                    amount = item.transaction.amountFormatted,
                                    date = item.transaction.date,
                                    type = item.transaction.type,
                                    tag = item.transaction.tag,
                                    excluded = item.transaction.excluded,
                                    tonalElevation = if (selected) MaterialTheme.elevation.level1 else MaterialTheme.elevation.level0,
                                    modifier = Modifier
                                        .then(clickableModifier)
                                        .animateItem(),
                                )
                            }
                        }
                    }
                }
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
            dateRangeLimits = DateUtil.dateNow().withMonth(1) to DateUtil.dateNow().withMonth(12),
            selectedStartDate = DateUtil.dateNow(),
            onStartDateSelect = {},
            selectedEndDate = DateUtil.dateNow(),
            onEndDateSelect = {},
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
private fun TransactionListLabel(
    multiSelectionModeActive: Boolean,
    totalSumAmount: Double,
    selectedTxTypeFilter: TransactionTypeFilter,
    listLabel: UiText,
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
            AggregateAmount(
                multiSelectionModeActive = multiSelectionModeActive,
                sumAmount = totalSumAmount,
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

            Crossfade(
                targetState = listLabel.asString(),
                label = "SelectedTagNameAnimatedLabel",
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                ListLabel(text = it)
            }
        }
    }
}

@Composable
private fun AggregateAmount(
    multiSelectionModeActive: Boolean,
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
        TextFormat.currencyAmount(sumAmount.absoluteValue)
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
                text = TextFormat.currencyAmount(amount.absoluteValue),
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
    dateRangeLimits: Pair<LocalDate, LocalDate>,
    selectedStartDate: LocalDate?,
    onStartDateSelect: (LocalDate) -> Unit,
    selectedEndDate: LocalDate?,
    onEndDateSelect: (LocalDate) -> Unit,
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
            DateRangeFilter(
                limits = dateRangeLimits,
                selectedStartDate = selectedStartDate,
                onStartDateSelect = onStartDateSelect,
                selectedEndDate = selectedEndDate,
                onEndDateSelect = onEndDateSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
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
private fun DateRangeFilter(
    limits: Pair<LocalDate, LocalDate>,
    selectedStartDate: LocalDate?,
    onStartDateSelect: (LocalDate) -> Unit,
    selectedEndDate: LocalDate?,
    onEndDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
//    val totalMonths = remember(limits) {
//        val (min, max) = limits
//        ChronoUnit.MONTHS.between(min, max)
//    }
//    val rangeSliderState = remember {
//        RangeSliderState(
//            (selectedStartDate ?: DateUtil.dateNow()),
//            100f,
//            valueRange = 0f..100f,
//            onValueChangeFinished = {},
//            steps = totalMonths.toInt()
//        )
//    }
//
//    RangeSlider(
//        state = rangeSliderState,
//        modifier = modifier
//    )
}