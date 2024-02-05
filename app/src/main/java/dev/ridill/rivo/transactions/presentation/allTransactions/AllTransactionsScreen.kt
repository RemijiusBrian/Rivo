package dev.ridill.rivo.transactions.presentation.allTransactions

import android.icu.util.Currency
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.navigation.destinations.AllTransactionsScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel0
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.exclusion
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.presentation.components.FolderListSearchSheet
import dev.ridill.rivo.transactions.domain.model.TagInfo
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionOption
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TagInputSheet
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun AllTransactionsScreen(
    snackbarController: SnackbarController,
    state: AllTransactionsState,
    isTagInputEditMode: () -> Boolean,
    tagNameInput: () -> String,
    tagColorInput: () -> Color?,
    tagExclusionInput: () -> Boolean?,
    folderSearchQuery: () -> String,
    foldersList: LazyPagingItems<Folder>,
    actions: AllTransactionsActions,
    navigateToAddEditTransaction: (Long) -> Unit,
    navigateUp: () -> Unit
) {
    val transactionsListState = rememberLazyListState()

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
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val localLayoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(localLayoutDirection),
                    end = paddingValues.calculateEndPadding(localLayoutDirection)
                )
                .padding(top = SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            TagsInfoList(
                currency = state.currency,
                tags = state.tagsWithExpenditures,
                selectedTagId = state.selectedTagId,
                onTagSelect = actions::onTagSelect,
                onTagLongClick = actions::onTagLongClick,
                onNewTagClick = actions::onNewTagClick,
                tagAssignModeActive = state.transactionMultiSelectionModeActive,
                onAssignToTransactions = actions::onAssignTagToTransactions,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(TAGS_LIST_HEIGHT_FRACTION)
            )

            DateFilter(
                selectedDate = state.selectedDate,
                onMonthSelect = actions::onMonthSelect,
                yearsList = state.yearsList,
                onYearSelect = actions::onYearSelect
            )

            TransactionsList(
                currency = state.currency,
                typeFilter = state.selectedTransactionTypeFilter,
                totalSumAmount = state.totalAmount,
                listLabel = state.transactionListLabel,
                transactionsList = state.transactionList,
                selectedTransactionIds = state.selectedTransactionIds,
                selectionState = state.transactionSelectionState,
                multiSelectionModeActive = state.transactionMultiSelectionModeActive,
                onSelectionStateChange = actions::onSelectionStateChange,
                onTransactionOptionClick = actions::onTransactionOptionClick,
                onTransactionClick = navigateToAddEditTransaction,
                onTxLongPress = actions::onTransactionLongPress,
                onTxSelectionChange = actions::onTransactionSelectionChange,
                listContentPadding = PaddingValues(
                    top = SpacingSmall,
                    bottom = paddingValues.calculateBottomPadding() + SpacingListEnd
                ),
                showExcludedTransactions = state.showExcludedTransactions,
                onToggleTransactionTypeFilter = actions::onTransactionTypeFilterToggle,
                onToggleShowExcludedTransactions = actions::onToggleShowExcludedTransactions,
                onDeleteSelectedTransactions = actions::onDeleteSelectedTransactionsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One),
                listState = transactionsListState
            )
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
                selectedColor = tagColorInput,
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

private const val TAGS_LIST_HEIGHT_FRACTION = 0.16f

@Composable
private fun TagsInfoList(
    currency: Currency,
    tags: List<TagInfo>,
    selectedTagId: Long?,
    onTagSelect: (Long) -> Unit,
    onTagLongClick: (Long) -> Unit,
    onNewTagClick: () -> Unit,
    tagAssignModeActive: Boolean,
    onAssignToTransactions: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val isTagsEmpty by remember(tags) {
        derivedStateOf { tags.isEmpty() }
    }

    // Prevent tags list starting of at last index
    LaunchedEffect(lazyListState, isTagsEmpty) {
        if (!isTagsEmpty) {
            lazyListState.scrollToItem(0)
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
                state = lazyListState
            ) {
                items(
                    items = tags,
                    key = { it.id },
                    contentType = { "TagInfoCard" }
                ) { tag ->
                    val selected = tag.id == selectedTagId
                    TagInfoCard(
                        name = tag.name,
                        color = tag.color,
                        isExcluded = tag.excluded,
                        expenditureAmount = TextFormat.currency(tag.expenditure, currency),
                        isSelected = selected,
                        onSelect = { onTagSelect(tag.id) },
                        onLongClick = { onTagLongClick(tag.id) },
                        tagAssignModeActive = tagAssignModeActive,
                        onAssignToTransactions = { onAssignToTransactions(tag.id) },
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .animateItemPlacement()
                    )
                }
            }
        }
        AnimatedVisibility(visible = tagAssignModeActive && tags.isNotEmpty()) {
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

@Composable
private fun TagInfoCard(
    name: String,
    color: Color,
    isExcluded: Boolean,
    expenditureAmount: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLongClick: () -> Unit,
    tagAssignModeActive: Boolean,
    onAssignToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        targetState = isSelected,
        label = "IsSelectedTransition"
    )
    val textScale by transition.animateFloat(
        label = "TextScale",
        targetValueByState = { if (it) 1f else 0.72f }
    )
    val widthMultiplier by transition.animateFloat(
        label = "TagInfoCardWidthMultiplier",
        targetValueByState = { if (it) 2f else 1f }
    )
    val contentColor = remember(color) { color.contentColor() }

    val clickableModifier = if (tagAssignModeActive) Modifier.clickable(
        onClick = onAssignToTransactions,
        onClickLabel = stringResource(R.string.cd_tap_tag_to_assign_to_transactions)
    )
    else Modifier.combinedClickable(
        onClick = onSelect,
        onClickLabel = stringResource(R.string.cd_tap_tag_to_filter_transactions),
        onLongClick = onLongClick,
        onLongClickLabel = stringResource(R.string.cd_long_press_tag_to_edit)
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        ),
        modifier = modifier
            .width(TagInfoCardWidth * widthMultiplier)
            .then(clickableModifier)
    ) {
        Column(
            modifier
                .fillMaxSize()
                .padding(SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge
                    .copy(
                        fontWeight = FontWeight.SemiBold,
                        textMotion = TextMotion.Animated,
                        lineBreak = LineBreak.Heading
                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = TextDecoration.exclusion(isExcluded),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = textScale
                        scaleY = textScale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            )

            if (isSelected) {
                Text(
                    text = stringResource(R.string.amount_worth_spent, expenditureAmount),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!tagAssignModeActive) {
                Text(
                    text = stringResource(R.string.cd_long_press_tag_to_edit),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
        }
    }
}

private val TagInfoCardWidth = 116.dp

@Composable
private fun DateFilter(
    selectedDate: LocalDate,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<Int>,
    onYearSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthsList = remember { Month.values() }

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
private fun TransactionsList(
    listState: LazyListState,
    currency: Currency,
    totalSumAmount: Double,
    typeFilter: TransactionType?,
    listLabel: UiText,
    transactionsList: List<TransactionListItem>,
    selectedTransactionIds: Set<Long>,
    selectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onToggleTransactionTypeFilter: () -> Unit,
    showExcludedTransactions: Boolean,
    onTransactionOptionClick: (TransactionOption) -> Unit,
    multiSelectionModeActive: Boolean,
    onTransactionClick: (Long) -> Unit,
    onTxLongPress: (Long) -> Unit,
    onTxSelectionChange: (Long) -> Unit,
    listContentPadding: PaddingValues,
    onToggleShowExcludedTransactions: (Boolean) -> Unit,
    onDeleteSelectedTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListEmpty by remember(transactionsList) {
        derivedStateOf { transactionsList.isEmpty() }
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isListEmpty) {
            EmptyListIndicator(
                resId = R.raw.lottie_empty_list_ghost,
                messageRes = R.string.all_transactions_list_empty_message
            )
        }
        Column(
            modifier = Modifier
                .matchParentSize()
        ) {
            TotalSumAmount(
                multiSelectionModeActive = multiSelectionModeActive,
                sumAmount = totalSumAmount,
                currency = currency,
                type = typeFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            Divider(
                modifier = Modifier
                    .padding(
                        vertical = SpacingSmall,
                        horizontal = SpacingMedium
                    )
            )

            TransactionListHeader(
                listLabel = listLabel,
                onToggleTransactionTypeFilter = onToggleTransactionTypeFilter,
                showExcludedTransactions = showExcludedTransactions,
                onToggleShowExcludedTransactions = onToggleShowExcludedTransactions,
                multiSelectionModeActive = multiSelectionModeActive,
                multiSelectionState = selectionState,
                onSelectionStateChange = onSelectionStateChange,
                onDeleteClick = onDeleteSelectedTransactions,
                onTransactionOptionClick = onTransactionOptionClick,
                modifier = Modifier
                    .fillMaxWidth()
            )

            LazyColumn(
                contentPadding = listContentPadding,
                verticalArrangement = Arrangement.spacedBy(SpacingSmall),
                state = listState
            ) {
                items(
                    items = transactionsList,
                    key = { it.id },
                    contentType = { "TransactionCard" }
                ) { transaction ->
                    val clickableModifier = if (multiSelectionModeActive) Modifier
                        .toggleable(
                            value = transaction.id in selectedTransactionIds,
                            onValueChange = { onTxSelectionChange(transaction.id) }
                        )
                    else Modifier.combinedClickable(
                        role = Role.Button,
                        onClick = { onTransactionClick(transaction.id) },
                        onClickLabel = stringResource(R.string.cd_tap_to_edit_transaction),
                        onLongClick = { onTxLongPress(transaction.id) },
                        onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection)
                    )

                    TransactionCard(
                        note = transaction.note,
                        amount = transaction.amountFormattedWithCurrency(currency),
                        date = transaction.date,
                        type = transaction.type,
                        selected = transaction.id in selectedTransactionIds,
                        excluded = transaction.excluded,
                        folder = transaction.folder,
                        modifier = Modifier
                            .then(clickableModifier)
                            .animateItemPlacement()
                    )
                }
            }
        }
    }
}

//private val AutoScrollThreshold = 40.dp

/*fun Modifier.transactionListDragHandler(
    lazyListState: LazyListState,
    selectedIds: () -> Set<Long>,
    hapticController: HapticFeedback,
    autoScrollThreshold: Float,
    onDragStart: (Long) -> Unit = {},
    onTxPointed: (Long) -> Unit = {},
    setAutoScrollSpeed: (Float) -> Unit = {}
): Modifier = this.then(
    pointerInput(autoScrollThreshold, onDragStart, onTxPointed, setAutoScrollSpeed) {
        fun txIdAtOffset(hitPoint: Offset): Long? = lazyListState
            .layoutInfo
            .visibleItemsInfo
            .find { itemInfo ->
                val itemTopOffset = itemInfo.index * itemInfo.size
                val itemBottomOffset = itemTopOffset + itemInfo.size
                val hitPointY = hitPoint.round().y

                hitPointY in itemTopOffset..itemBottomOffset
            }?.key as? Long

        var initialTxId: Long? = null
        var currentTxId: Long? = null
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                hapticController.performHapticFeedback(HapticFeedbackType.LongPress)
                txIdAtOffset(offset)?.let { id ->
                    if (!selectedIds().contains(id)) {
                        initialTxId = id
                        currentTxId = id
                        onDragStart(id)
                    }
                }
            },
            onDragCancel = {
                setAutoScrollSpeed(Float.Zero)
                initialTxId = null
            },
            onDragEnd = {
                setAutoScrollSpeed(Float.Zero)
                initialTxId = null
            },
            onDrag = { change, _ ->
                if (initialTxId != null) {
                    val distFromBottom =
                        lazyListState.layoutInfo.viewportSize.height - change.position.y
                    val distFromTop = change.position.y
                    setAutoScrollSpeed(
                        when {
                            distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                            distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                            else -> Float.Zero
                        }
                    )

                    txIdAtOffset(change.position)?.let { pointerTxId ->
                        if (currentTxId != pointerTxId) {
                            onTxPointed(pointerTxId)
                            currentTxId = pointerTxId
                        }
                    }
                }
            }
        )
    }
)*/

@Composable
private fun TransactionListHeader(
    listLabel: UiText,
    showExcludedTransactions: Boolean,
    onToggleTransactionTypeFilter: () -> Unit,
    onToggleShowExcludedTransactions: (Boolean) -> Unit,
    multiSelectionModeActive: Boolean,
    multiSelectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onDeleteClick: () -> Unit,
    onTransactionOptionClick: (TransactionOption) -> Unit,
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
            showExcludedTransactions = showExcludedTransactions,
            onToggleTransactionTypeFilter = onToggleTransactionTypeFilter,
            onToggleShowExcludedTransactions = onToggleShowExcludedTransactions,
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
    showExcludedTransactions: Boolean,
    onToggleTransactionTypeFilter: () -> Unit,
    onToggleShowExcludedTransactions: (Boolean) -> Unit,
    multiSelectionModeActive: Boolean,
    onTransactionOptionClick: (TransactionOption) -> Unit,
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
                    TransactionOption.entries.forEach { option ->
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
                                    id = if (showExcludedTransactions) R.string.hide_excluded_transactions
                                    else R.string.show_excluded_transactions
                                )
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleShowExcludedTransactions(!showExcludedTransactions)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalSumAmount(
    multiSelectionModeActive: Boolean,
    currency: Currency,
    sumAmount: Double,
    type: TransactionType?,
    modifier: Modifier = Modifier
) {
    val arrowRotationDeg by animateFloatAsState(
        targetValue = when (type) {
            TransactionType.CREDIT -> 180f
            TransactionType.DEBIT -> Float.Zero
            null -> if (sumAmount >= Double.Zero) Float.Zero
            else 180f
        },
        label = "ArrowRotationDegree"
    )
    val sumContentDescription = stringResource(
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
                contentDescription = sumContentDescription
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
            verticalAlignment = Alignment.CenterVertically
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

            AnimatedVisibility(sumAmount != Double.Zero) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(arrowRotationDeg)
                )
            }
        }
    }
}

@Composable
private fun TransactionCard(
    note: String,
    amount: String,
    date: LocalDate,
    type: TransactionType,
    folder: Folder?,
    selected: Boolean,
    excluded: Boolean,
    modifier: Modifier = Modifier
) = TransactionListItem(
    note = note,
    amount = amount,
    date = date,
    type = type,
    showTypeIndicator = true,
    tag = null,
    folder = folder,
    modifier = modifier
        .fillMaxWidth(),
    tonalElevation = if (selected) ElevationLevel1 else ElevationLevel0,
    excluded = excluded
)