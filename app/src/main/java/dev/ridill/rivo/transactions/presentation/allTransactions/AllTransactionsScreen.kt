package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import dev.ridill.rivo.core.ui.components.ListEmptyIndicatorItem
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.components.icons.Tags
import dev.ridill.rivo.core.ui.navigation.destinations.AllTransactionsScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel0
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.IconSizeSmall
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.presentation.components.FolderListSearchSheet
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.NewTransactionFab
import dev.ridill.rivo.transactions.presentation.components.TagInputSheet
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun AllTransactionsScreen(
    snackbarController: SnackbarController,
    tagsPagingItems: LazyPagingItems<Tag>,
    state: AllTransactionsState,
    isTagInputEditMode: () -> Boolean,
    tagNameInput: () -> String,
    tagInputColorCode: () -> Int?,
    tagExclusionInput: () -> Boolean?,
    folderSearchQuery: () -> String,
    foldersList: LazyPagingItems<Folder>,
    actions: AllTransactionsActions,
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
            verticalArrangement = Arrangement.spacedBy(SpacingSmall),
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding() + SpacingListEnd
            )
        ) {
            item(
                key = "TagsHorizontalList",
                contentType = "TagsHorizontalList"
            ) {
                TagsInfoList(
                    tagsPagingItems = tagsPagingItems,
                    selectedTagId = state.selectedTagId,
                    onTagSelect = actions::onTagSelect,
                    onTagLongClick = actions::onTagLongClick,
                    onNewTagClick = actions::onNewTagClick,
                    tagAssignModeActive = state.transactionMultiSelectionModeActive,
                    onAssignToTransactions = actions::onAssignTagToTransactions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TagsRowMinHeight)
                        .animateItemPlacement()
                )
            }

            stickyHeader(
                key = "TransactionListHeader",
                contentType = "TransactionListHeader"
            ) {
                TransactionListHeader(
                    selectedDate = state.selectedDate,
                    onMonthSelect = actions::onMonthSelect,
                    yearsList = state.yearsList,
                    onYearSelect = actions::onYearSelect,
                    multiSelectionModeActive = state.transactionMultiSelectionModeActive,
                    totalSumAmount = state.aggregateAmount,
                    currency = state.currency,
                    selectedTxTypeFilter = state.selectedTransactionTypeFilter,
                    listLabel = state.transactionListLabel,
                    showExcludedOption = state.showExcludedOption,
                    onToggleTransactionTypeFilter = actions::onTransactionTypeFilterToggle,
                    onToggleShowExcludedOption = actions::onToggleShowExcludedOption,
                    multiSelectionState = state.transactionSelectionState,
                    onSelectionStateChange = actions::onSelectionStateChange,
                    onDeleteClick = actions::onDeleteSelectedTransactionsClick,
                    onTransactionOptionClick = actions::onTransactionOptionClick,
                    modifier = Modifier
                        .animateItemPlacement()
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
                    showTypeIndicator = true,
                    tag = transaction.tag,
                    tonalElevation = if (selected) ElevationLevel1 else ElevationLevel0,
                    note = transaction.note,
                    amount = transaction.amountFormattedWithCurrency(state.currency),
                    date = transaction.date,
                    type = transaction.type,
                    excluded = transaction.excluded,
                    folder = transaction.folder,
                    modifier = Modifier
                        .then(clickableModifier)
                        .animateItemPlacement()
                )
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

        if (state.showDeleteTagConfirmation) {
            MultiActionConfirmationDialog(
                title = stringResource(R.string.delete_tag_confirmation_title, tagNameInput()),
                text = stringResource(R.string.action_irreversible_message),
                primaryActionLabelRes = R.string.delete_tag,
                onPrimaryActionClick = actions::onDeleteTagConfirm,
                secondaryActionLabelRes = R.string.delete_tag_with_transactions,
                onSecondaryActionClick = actions::onDeleteTagWithTransactionsClick,
                onDismiss = actions::onDeleteTagDismiss
            )
        }

        if (state.showTagInput) {
            TagInputSheet(
                nameInput = tagNameInput,
                onNameChange = actions::onTagInputNameChange,
                selectedColorCode = tagInputColorCode,
                onColorSelect = actions::onTagInputColorSelect,
                excluded = tagExclusionInput,
                onExclusionToggle = actions::onTagInputExclusionChange,
                onDismiss = actions::onTagInputDismiss,
                onConfirm = actions::onTagInputConfirm,
                errorMessage = state.tagInputError,
                isEditMode = isTagInputEditMode,
                onDeleteClick = actions::onDeleteTagClick
            )
        }

        if (state.showFolderSelection) {
            FolderListSearchSheet(
                searchQuery = folderSearchQuery,
                onSearchQueryChange = actions::onTransactionFolderQueryChange,
                foldersList = foldersList,
                onFolderClick = actions::onTransactionFolderSelect,
                onCreateNewClick = actions::onCreateNewFolderClick,
                onDismiss = actions::onTransactionFolderSelectionDismiss
            )
        }
    }
}

private val TagsRowMinHeight = 100.dp

@Composable
private fun TagsInfoList(
    tagsPagingItems: LazyPagingItems<Tag>,
    selectedTagId: Long?,
    onTagSelect: (Long) -> Unit,
    onTagLongClick: (Long) -> Unit,
    onNewTagClick: () -> Unit,
    tagAssignModeActive: Boolean,
    onAssignToTransactions: (Long) -> Unit,
    modifier: Modifier = Modifier,
    tagsListState: LazyListState = rememberLazyListState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var scrollJob: Job? = remember { null }
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
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ListLabel(
                text = stringResource(R.string.tags),
                modifier = Modifier
                    .weight(Float.One)
                    .padding(horizontal = SpacingMedium),
            )
            SpacerSmall()
            TextButton(onClick = onNewTagClick) {
                Text(text = stringResource(R.string.create_new_tag))
                Spacer(spacing = ButtonDefaults.IconSpacing)
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                )
            }
        }
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if (isTagsEmpty) {
                Text(
                    text = stringResource(R.string.tags_list_empty_message),
                    color = LocalContentColor.current
                        .copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
            LazyRow(
                modifier = Modifier
                    .matchParentSize(),
                contentPadding = PaddingValues(
                    start = SpacingMedium,
                    end = SpacingListEnd
                ),
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
                state = tagsListState
            ) {
                items(
                    count = tagsPagingItems.itemCount,
                    key = tagsPagingItems.itemKey { it.id },
                    contentType = tagsPagingItems.itemContentType { "TagCard" }
                ) { index ->
                    tagsPagingItems[index]?.let { tag ->
                        val selected = tag.id == selectedTagId
                        TagCard(
                            name = tag.name,
                            color = Color(tag.colorCode),
                            isExcluded = tag.excluded,
                            createdTimestamp = tag.createdTimestampFormatted,
                            isSelected = selected,
                            onSelect = {
                                scrollJob?.cancel()
                                scrollJob = coroutineScope.launch {
                                    onTagSelect(tag.id)
                                    tagsListState.animateScrollToItem(index)
                                }
                            },
                            onLongClick = { onTagLongClick(tag.id) },
                            tagAssignModeActive = tagAssignModeActive,
                            onAssignToTransactions = { onAssignToTransactions(tag.id) },
                            modifier = Modifier
                                .animateContentSize()
                                .fillParentMaxHeight()
                                .then(
                                    if (selected) Modifier
                                        .fillParentMaxWidth()
                                    else Modifier
                                        .widthIn(
                                            min = TagInfoCardMinWidth,
                                            max = TagInfoCardMaxWidth
                                        )
                                )
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = tagAssignModeActive && !isTagsEmpty) {
            Text(
                text = stringResource(R.string.tap_tag_to_assign_to_selected_transactions),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium),
                color = LocalContentColor.current
                    .copy(alpha = 0.80f)
            )
        }
    }
}

private val TagInfoCardMinWidth = 100.dp
private val TagInfoCardMaxWidth = 200.dp

@Composable
private fun TagCard(
    name: String,
    color: Color,
    isExcluded: Boolean,
    isSelected: Boolean,
    createdTimestamp: String,
    onSelect: () -> Unit,
    onLongClick: () -> Unit,
    tagAssignModeActive: Boolean,
    onAssignToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = remember(color) { color.contentColor() }
    val clickableModifier = if (tagAssignModeActive) Modifier.clickable(
        onClick = onAssignToTransactions,
        onClickLabel = stringResource(R.string.cd_tap_tag_to_assign_to_transactions)
    )
    else if (isSelected) Modifier.combinedClickable(
        onClick = onSelect,
        onClickLabel = stringResource(R.string.cd_tap_tag_to_filter_transactions),
        onLongClick = onLongClick,
        onLongClickLabel = stringResource(R.string.cd_long_press_tag_to_edit)
    )
    else Modifier.clickable(
        onClick = onSelect,
        onClickLabel = stringResource(R.string.cd_tap_tag_to_filter_transactions)
    )

    val showLongPressMessage by remember(isSelected, tagAssignModeActive) {
        derivedStateOf { isSelected && !tagAssignModeActive }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        ),
        modifier = modifier
            .then(clickableModifier)
            .exclusionGraphicsLayer(isExcluded)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingMedium),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tags,
                    contentDescription = null,
                    modifier = Modifier
                        .size(IconSizeSmall)
                )
                SpacerSmall()
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
                text = stringResource(R.string.created_colon_timestamp_value, createdTimestamp),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
                    .copy(alpha = ContentAlpha.SUB_CONTENT)
            )

            AnimatedVisibility(showLongPressMessage) {
                Text(
                    text = stringResource(R.string.asterisk_long_press_to_edit),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
        }
    }
}

@Composable
private fun TransactionListHeader(
    selectedDate: LocalDate,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<Int>,
    onYearSelect: (Int) -> Unit,
    multiSelectionModeActive: Boolean,
    totalSumAmount: Double,
    currency: Currency,
    selectedTxTypeFilter: TransactionType?,
    listLabel: UiText,
    showExcludedOption: Boolean,
    onToggleTransactionTypeFilter: () -> Unit,
    onToggleShowExcludedOption: (Boolean) -> Unit,
    multiSelectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onDeleteClick: () -> Unit,
    onTransactionOptionClick: (AllTransactionsMultiSelectionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier
            .then(modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingSmall),
            modifier = Modifier
                .padding(vertical = SpacingSmall)
        ) {
            DateFilter(
                selectedDate = selectedDate,
                onMonthSelect = onMonthSelect,
                yearsList = yearsList,
                onYearSelect = onYearSelect,
                modifier = Modifier
                    .fillMaxWidth()
            )
            AggregateAmount(
                multiSelectionModeActive = multiSelectionModeActive,
                sumAmount = totalSumAmount,
                currency = currency,
                type = selectedTxTypeFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        vertical = SpacingSmall,
                        horizontal = SpacingMedium
                    )
            )

            TransactionListLabelAndOptions(
                listLabel = listLabel,
                onToggleTransactionTypeFilter = onToggleTransactionTypeFilter,
                showExcludedOption = showExcludedOption,
                onToggleShowExcludedOption = onToggleShowExcludedOption,
                multiSelectionModeActive = multiSelectionModeActive,
                multiSelectionState = multiSelectionState,
                onSelectionStateChange = onSelectionStateChange,
                onDeleteClick = onDeleteClick,
                onTransactionOptionClick = onTransactionOptionClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
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
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateIndicator(
                date = selectedDate,
                isSelected = showYearsList,
                onClick = { showYearsList = !showYearsList },
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )
            AnimatedVisibility(
                visible = showYearsList,
                enter = slideInHorizontally { it / 2 } + fadeIn(),
                exit = slideOutHorizontally { it / 2 } + fadeOut()
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
                    contentPadding = PaddingValues(
                        start = SpacingMedium,
                        end = SpacingListEnd
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
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
            contentPadding = PaddingValues(
                start = SpacingMedium,
                end = SpacingListEnd
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
                                .animateItemPlacement()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DateIndicator(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndicatorRotation by animateFloatAsState(
        targetValue = if (isSelected) 180f else 0f,
        animationSpec = tween(AnimationConstants.DefaultDurationMillis),
        label = "SelectedIndicatorRotation"
    )
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        selected = isSelected,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
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
}

@Composable
private fun TransactionListLabelAndOptions(
    listLabel: UiText,
    showExcludedOption: Boolean,
    onToggleTransactionTypeFilter: () -> Unit,
    onToggleShowExcludedOption: (Boolean) -> Unit,
    multiSelectionModeActive: Boolean,
    multiSelectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onDeleteClick: () -> Unit,
    onTransactionOptionClick: (AllTransactionsMultiSelectionOption) -> Unit,
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
                .padding(horizontal = SpacingMedium)
                .weight(Float.One)
        ) { ListLabel(text = it) }

        TransactionListOptions(
            showExcludedOption = showExcludedOption,
            onToggleTransactionTypeFilter = onToggleTransactionTypeFilter,
            onToggleShowExcludedOption = onToggleShowExcludedOption,
            multiSelectionModeActive = multiSelectionModeActive,
            onTransactionOptionClick = onTransactionOptionClick,
            selectionState = multiSelectionState,
            onSelectionStateChange = onSelectionStateChange,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
private fun TransactionListOptions(
    showExcludedOption: Boolean,
    onToggleTransactionTypeFilter: () -> Unit,
    onToggleShowExcludedOption: (Boolean) -> Unit,
    multiSelectionModeActive: Boolean,
    onTransactionOptionClick: (AllTransactionsMultiSelectionOption) -> Unit,
    selectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AnimatedVisibility(visible = !multiSelectionModeActive) {
            IconButton(onClick = onToggleTransactionTypeFilter) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_funnel_dollar),
                    contentDescription = stringResource(R.string.cd_filter_transactions_by_type)
                )
            }
        }
        AnimatedVisibility(visible = multiSelectionModeActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall)
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = stringResource(R.string.cd_delete_selected_transactions)
                    )
                }
                TriStateCheckbox(
                    state = selectionState,
                    onClick = onSelectionStateChange
                )
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.cd_options)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                if (multiSelectionModeActive) {
                    AllTransactionsMultiSelectionOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes)) },
                            onClick = {
                                menuExpanded = false
                                onTransactionOptionClick(option)
                            }
                        )
                    }
                } else {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = if (showExcludedOption) R.string.hide_excluded_transactions
                                    else R.string.show_excluded_transactions
                                )
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleShowExcludedOption(!showExcludedOption)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AggregateAmount(
    multiSelectionModeActive: Boolean,
    currency: Currency,
    sumAmount: Double,
    type: TransactionType?,
    modifier: Modifier = Modifier
) {
    val showTypeIcon by remember(type) {
        derivedStateOf { type != null }
    }
    val aggContentDescription = stringResource(
        R.string.cd_total_transaction_sum,
        stringResource(
            id = when {
                multiSelectionModeActive -> R.string.selected_aggregate
                type == null -> R.string.aggregate
                else -> R.string.total
            }
        ),
        TextFormat.currency(amount = sumAmount.absoluteValue, currency = currency)
    )
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .semantics(true) {}
            .clearAndSetSemantics {
                contentDescription = aggContentDescription
            }
    ) {
        Crossfade(targetState = type, label = "TotalAmountLabel") { txType ->
            Text(
                text = stringResource(
                    id = when {
                        multiSelectionModeActive -> R.string.selected_aggregate
                        txType == null -> R.string.aggregate
                        else -> R.string.total
                    }
                ),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SpacerSmall()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
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

            AnimatedVisibility(showTypeIcon) {
                type?.let {
                    Icon(
                        imageVector = ImageVector.vectorResource(it.iconRes),
                        contentDescription = null
                    )
                }
            }
        }
    }
}