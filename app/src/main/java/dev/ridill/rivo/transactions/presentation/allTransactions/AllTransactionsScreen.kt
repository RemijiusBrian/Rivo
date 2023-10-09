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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.MoreVert
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
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
import dev.ridill.rivo.core.ui.util.exclusion
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.presentation.components.FolderListSearchSheet
import dev.ridill.rivo.transactions.domain.model.TagInfo
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionOption
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TagInputSheet
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

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
    navigateUp: () -> Unit
) {
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
                onTagClick = actions::onTagClick,
                onTagLongClick = actions::onTagLongClick,
                onNewTagClick = actions::onNewTagClick,
                tagAssignModeActive = state.transactionMultiSelectionModeActive,
                transactionMultiSelectionModeActive = state.transactionMultiSelectionModeActive,
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
                selectedTagName = state.selectedTagName,
                currency = state.currency,
                typeFilter = state.selectedTransactionTypeFilter,
                totalSumAmount = state.totalAmount,
                transactionsList = state.transactionList,
                selectedTransactionIds = state.selectedTransactionIds,
                selectionState = state.transactionSelectionState,
                multiSelectionModeActive = state.transactionMultiSelectionModeActive,
                onSelectionStateChange = actions::onSelectionStateChange,
                onTransactionOptionClick = actions::onTransactionOptionClick,
                onTransactionLongClick = actions::onTransactionLongClick,
                onTransactionClick = actions::onTransactionClick,
                listContentPadding = PaddingValues(
                    top = SpacingSmall,
                    bottom = paddingValues.calculateBottomPadding() + SpacingListEnd
                ),
                showExcludedTransactions = state.showExcludedTransactions,
                onToggleShowExcludedTransactions = actions::onToggleShowExcludedTransactions,
                onDeleteSelectedTransactions = actions::onDeleteSelectedTransactionsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One)
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
    onTagClick: (Long) -> Unit,
    onTagLongClick: (Long) -> Unit,
    transactionMultiSelectionModeActive: Boolean,
    onNewTagClick: () -> Unit,
    tagAssignModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    // Prevent tags list starting of at last index
    LaunchedEffect(lazyListState, tags) {
        if (tags.isNotEmpty()) {
            lazyListState.scrollToItem(0)
        }
    }
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        ListLabel(
            text = stringResource(R.string.tags),
            modifier = Modifier
                .padding(horizontal = SpacingMedium)
        )
        LazyRow(
            modifier = modifier,
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
                    expenditureAmount = TextFormat.currency(
                        amount = tag.expenditure,
                        currency = currency
                    ),
                    isSelected = selected,
                    onClick = { onTagClick(tag.id) },
                    onLongClick = {
                        scope.launch {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onTagLongClick(tag.id)
                    },
                    longClickEnabled = !transactionMultiSelectionModeActive,
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .animateItemPlacement()
                )
            }

            if (!transactionMultiSelectionModeActive) {
                item(
                    key = "NewTagCard",
                    contentType = "NewTagCard"
                ) {
                    NewTagCard(
                        onClick = onNewTagClick,
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .animateItemPlacement()
                    )
                }
            }
        }
        AnimatedVisibility(visible = tagAssignModeActive) {
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    longClickEnabled: Boolean,
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

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        ),
        modifier = modifier
            .width(TagInfoCardWidth * widthMultiplier)
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (longClickEnabled) onLongClick else null,
                onLongClickLabel = stringResource(R.string.cd_long_click_tag_to_edit)
            )
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

            if (longClickEnabled) {
                Text(
                    text = stringResource(R.string.cd_long_click_tag_to_edit),
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
private fun NewTagCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(TagInfoCardWidth)
            .clip(CardDefaults.shape)
            .clickable(
                role = Role.Button,
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_create_new_tag)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null
        )
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
                imageVector = Icons.Default.ArrowLeft,
                contentDescription = stringResource(R.string.cd_show_years_list),
                modifier = Modifier
                    .rotate(selectedIndicatorRotation)
            )
        }
    }
}

@Composable
private fun TransactionsList(
    currency: Currency,
    totalSumAmount: Double,
    typeFilter: TransactionType?,
    selectedTagName: String?,
    transactionsList: List<TransactionListItem>,
    selectedTransactionIds: List<Long>,
    selectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onTransactionOptionClick: (TransactionOption) -> Unit,
    multiSelectionModeActive: Boolean,
    onTransactionLongClick: (Long) -> Unit,
    onTransactionClick: (Long) -> Unit,
    listContentPadding: PaddingValues,
    showExcludedTransactions: Boolean,
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
            EmptyListIndicator(resId = R.raw.lottie_empty_list_ghost)
        }
        Column(
            modifier = Modifier
                .matchParentSize()
        ) {
            AnimatedVisibility(
                visible = !multiSelectionModeActive,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            ) {
                TotalSumAmount(
                    sumAmount = totalSumAmount,
                    currency = currency,
                    type = typeFilter ?: TransactionType.DEBIT,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            Divider(
                modifier = Modifier
                    .padding(
                        vertical = SpacingSmall,
                        horizontal = SpacingMedium
                    )
            )

            TransactionListHeader(
                selectedTagName = selectedTagName,
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
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                items(
                    items = transactionsList,
                    key = { it.id },
                    contentType = { "TransactionCard" }
                ) { transaction ->
                    TransactionCard(
                        note = transaction.note,
                        amount = transaction.amountFormattedWithCurrency(currency),
                        date = transaction.date,
                        type = transaction.type,
                        selected = transaction.id in selectedTransactionIds,
                        onLongClick = { onTransactionLongClick(transaction.id) },
                        onClick = { onTransactionClick(transaction.id) },
                        excluded = transaction.excluded,
                        folder = transaction.folder
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionListHeader(
    selectedTagName: String?,
    showExcludedTransactions: Boolean,
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
            targetState = selectedTagName ?: stringResource(R.string.all_transactions),
            label = "SelectedTagNameAnimatedLabel",
            modifier = Modifier
                .padding(horizontal = SpacingMedium)
                .weight(Float.One)
        ) { ListLabel(text = it) }

        TransactionListOptions(
            showExcludedTransactions = showExcludedTransactions,
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
                    TransactionOption.values().forEach { option ->
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
    currency: Currency,
    sumAmount: Double,
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Crossfade(targetState = type, label = "TotalAmountLabel") { txType ->
            Text(
                text = stringResource(
                    id = when (txType) {
                        TransactionType.CREDIT -> R.string.total_credits
                        TransactionType.DEBIT -> R.string.total_debits
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
                text = TextFormat.currency(amount = amount, currency = currency),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
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
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    excluded: Boolean,
    modifier: Modifier = Modifier
) {
    TransactionListItem(
        note = note,
        amount = amount,
        date = date,
        type = type,
        showTypeIndicator = true,
        tag = null,
        folder = folder,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_transaction_selection_change),
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection)
            ),
        tonalElevation = if (selected) ElevationLevel1 else ElevationLevel0,
        excluded = excluded
    )
}