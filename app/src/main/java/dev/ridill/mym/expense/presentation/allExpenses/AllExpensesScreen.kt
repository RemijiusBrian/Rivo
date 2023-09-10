package dev.ridill.mym.expense.presentation.allExpenses

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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.ConfirmationDialog
import dev.ridill.mym.core.ui.components.EmptyListIndicator
import dev.ridill.mym.core.ui.components.ListLabel
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.components.SpacerExtraSmall
import dev.ridill.mym.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesScreenSpec
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.ElevationLevel0
import dev.ridill.mym.core.ui.theme.ElevationLevel1
import dev.ridill.mym.core.ui.theme.MYMTheme
import dev.ridill.mym.core.ui.theme.SpacingExtraSmall
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.theme.contentColor
import dev.ridill.mym.core.ui.util.TextFormat
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseOption
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import dev.ridill.mym.expense.presentation.components.ExpenseListItem
import dev.ridill.mym.expense.presentation.components.NewTagSheet
import dev.ridill.mym.expense.presentation.components.TagColors
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AllExpensesScreen(
    snackbarController: SnackbarController,
    state: AllExpensesState,
    tagNameInput: () -> String,
    tagColorInput: () -> Int?,
    actions: AllExpensesActions,
    navigateUp: () -> Unit
) {
    BackHandler(
        enabled = state.expenseMultiSelectionModeActive,
        onBack = actions::onDismissMultiSelectionMode
    )

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    MYMScaffold(
        snackbarController = snackbarController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.expenseMultiSelectionModeActive)
                            stringResource(R.string.count_selected, state.selectedExpenseIds.size)
                        else stringResource(AllExpensesScreenSpec.labelRes)
                    )
                },
                navigationIcon = {
                    if (state.expenseMultiSelectionModeActive) {
                        IconButton(onClick = actions::onDismissMultiSelectionMode) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_expense_selection)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                )
                .padding(top = SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            TagsInfoList(
                tags = state.tagsWithExpenditures,
                selectedTagId = state.selectedTag?.id,
                onTagClick = actions::onTagClick,
                onTagEditClick = actions::onEditTagClick,
                onNewTagClick = actions::onNewTagClick,
                tagAssignModeActive = state.expenseMultiSelectionModeActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.20f)
            )

            DateFilter(
                selectedDate = state.selectedDate,
                onMonthSelect = actions::onMonthSelect,
                yearsList = state.yearsList,
                onYearSelect = actions::onYearSelect
            )

            ExpenseList(
                selectedTagName = state.selectedTag?.name,
                expenseList = state.expenseList,
                totalExpenditure = state.totalExpenditure,
                selectedExpenseIds = state.selectedExpenseIds,
                selectionState = state.expenseSelectionState,
                multiSelectionModeActive = state.expenseMultiSelectionModeActive,
                onSelectionStateChange = actions::onSelectionStateChange,
                onExpenseOptionClick = actions::onExpenseOptionClick,
                onExpenseLongClick = actions::onExpenseLongClick,
                onExpenseClick = actions::onExpenseClick,
                listContentPadding = PaddingValues(
                    top = SpacingSmall,
                    bottom = paddingValues.calculateBottomPadding() + SpacingListEnd
                ),
                showExcludedExpenses = state.showExcludedExpenses,
                onToggleShowExcludedExpenses = actions::onToggleShowExcludedExpenses,
                onDeleteSelectedExpenses = actions::onDeleteSelectedExpensesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One)
            )
        }

        if (state.showDeleteExpenseConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_multiple_expense_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteExpenseConfirm,
                onDismiss = actions::onDeleteExpenseDismiss
            )
        }

        if (state.showDeleteTagConfirmation) {
            DeleteTagDialog(
                onDeleteTag = actions::onDeleteTagConfirm,
                onDeleteTagWithExpenses = actions::onDeleteTagWithExpensesClick,
                onDismiss = actions::onDeleteTagDismiss
            )
        }

        if (state.showNewTagInput) {
            NewTagSheet(
                nameInput = tagNameInput,
                onNameChange = actions::onNewTagNameChange,
                selectedColorCode = tagColorInput(),
                onColorSelect = actions::onNewTagColorSelect,
                onDismiss = actions::onNewTagInputDismiss,
                onConfirm = actions::onNewTagInputConfirm,
                errorMessage = state.newTagError
            )
        }
    }
}

@Composable
private fun TagsInfoList(
    tags: List<TagWithExpenditure>,
    selectedTagId: Long?,
    onTagClick: (ExpenseTag) -> Unit,
    onTagEditClick: (ExpenseTag) -> Unit,
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
            items(items = tags, key = { it.tag.id }) { item ->
                TagInfoCard(
                    name = item.tag.name,
                    color = item.tag.color,
                    amount = TextFormat.currency(item.expenditure),
                    percentOfTotalExpenditure = item.percentOfTotalExpenditure,
                    isSelected = item.tag.id == selectedTagId,
                    onClick = { onTagClick(item.tag) },
                    onEditClick = { onTagEditClick(item.tag) },
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .animateItemPlacement()
                )
            }

            item(key = "NewTagCard") {
                NewTagCard(
                    onClick = onNewTagClick,
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .animateItemPlacement()
                )
            }
        }
        AnimatedVisibility(visible = tagAssignModeActive) {
            Text(
                text = stringResource(R.string.click_tag_to_assign_to_selected_expenses),
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
    amount: String,
    percentOfTotalExpenditure: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        targetState = isSelected,
        label = "IsSelectedTransition"
    )
    val percent by animateFloatAsState(
        targetValue = percentOfTotalExpenditure,
        label = "AnimatedPercentOfTotal"
    )
    val widthFactor by transition.animateFloat(
        label = "TagInfoCardWidthFactor",
        targetValueByState = { if (it) 2f else 1f }
    )
    val textScale by transition.animateFloat(
        label = "TextScale",
        targetValueByState = { if (it) 1f else 0.8f }
    )
    val contentColor = remember(color) { color.contentColor() }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(TagInfoCardWidth * widthFactor)
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingMedium),
        ) {
            Column(
                modifier = Modifier
                    .weight(Float.One),
                verticalArrangement = Arrangement.spacedBy(SpacingExtraSmall)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
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
                        modifier = Modifier
                            .weight(Float.One)
                            .graphicsLayer {
                                scaleX = textScale
                                scaleY = textScale
                                transformOrigin = TransformOrigin(0f, 0f)
                            },
                    )
                    if (isSelected) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.cd_edit_tag),
                                modifier = Modifier
                                    .size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }

                if (isSelected) {
                    Text(
                        text = stringResource(R.string.amount_worth_spent, amount),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = if (percent.isNaN()) stringResource(R.string.no_expenditure_yet)
                else stringResource(
                    R.string.percent_of_expenditure,
                    TextFormat.percent(percent)
                ),
                style = MaterialTheme.typography.bodySmall.copy(
                    textMotion = TextMotion.Animated
                ),
                color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT),
                overflow = TextOverflow.Ellipsis
            )

            SpacerExtraSmall()

            LinearProgressIndicator(
                progress = percent,
                modifier = Modifier
                    .fillMaxWidth(),
                color = color,
                strokeCap = StrokeCap.Round
            )
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

private val TagInfoCardWidth = 120.dp

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
                    items(items = yearsList, key = { it }) { year ->
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
            items(items = monthsList, key = { it.value }) { month ->
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
                imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_clock),
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
private fun ExpenseList(
    selectedTagName: String?,
    expenseList: List<ExpenseListItem>,
    totalExpenditure: Double,
    selectedExpenseIds: List<Long>,
    selectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onExpenseOptionClick: (ExpenseOption) -> Unit,
    multiSelectionModeActive: Boolean,
    onExpenseLongClick: (Long) -> Unit,
    onExpenseClick: (Long) -> Unit,
    listContentPadding: PaddingValues,
    showExcludedExpenses: Boolean,
    onToggleShowExcludedExpenses: (Boolean) -> Unit,
    onDeleteSelectedExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListEmpty by remember(expenseList) {
        derivedStateOf { expenseList.isEmpty() }
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
                TotalExpenditureAmount(
                    expenditure = totalExpenditure,
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

            ExpenseListHeader(
                selectedTagName = selectedTagName,
                showExcludedExpenses = showExcludedExpenses,
                onToggleShowExcludedExpenses = onToggleShowExcludedExpenses,
                multiSelectionModeActive = multiSelectionModeActive,
                multiSelectionState = selectionState,
                onSelectionStateChange = onSelectionStateChange,
                onDeleteClick = onDeleteSelectedExpenses,
                onExpenseOptionClick = onExpenseOptionClick,
                modifier = Modifier
                    .fillMaxWidth()
            )

            LazyColumn(
                contentPadding = listContentPadding,
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                items(items = expenseList, key = { it.id }) { expense ->
                    ExpenseCard(
                        note = expense.note,
                        amount = expense.amount,
                        date = expense.date,
                        selected = expense.id in selectedExpenseIds,
                        onLongClick = { onExpenseLongClick(expense.id) },
                        onClick = { onExpenseClick(expense.id) },
                        showExcludedIndicator = expense.excluded
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseListHeader(
    selectedTagName: String?,
    showExcludedExpenses: Boolean,
    onToggleShowExcludedExpenses: (Boolean) -> Unit,
    multiSelectionModeActive: Boolean,
    multiSelectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onDeleteClick: () -> Unit,
    onExpenseOptionClick: (ExpenseOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(
            targetState = selectedTagName ?: stringResource(R.string.all_expenses),
            label = "SelectedTagNameAnimatedLabel",
            modifier = Modifier
                .padding(horizontal = SpacingMedium)
        ) { tag ->
            ListLabel(text = tag)
        }

        ExpenseListOptions(
            showExcludedExpenses = showExcludedExpenses,
            onToggleShowExcludedExpenses = onToggleShowExcludedExpenses,
            multiSelectionModeActive = multiSelectionModeActive,
            onExpenseOptionClick = onExpenseOptionClick,
            selectionState = multiSelectionState,
            onSelectionStateChange = onSelectionStateChange,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
private fun ExpenseListOptions(
    showExcludedExpenses: Boolean,
    onToggleShowExcludedExpenses: (Boolean) -> Unit,
    multiSelectionModeActive: Boolean,
    onExpenseOptionClick: (ExpenseOption) -> Unit,
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
                        contentDescription = stringResource(R.string.cd_delete)
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
                    ExpenseOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes)) },
                            onClick = {
                                menuExpanded = false
                                onExpenseOptionClick(option)
                            }
                        )
                    }
                } else {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = if (showExcludedExpenses) R.string.hide_excluded_expenses
                                    else R.string.show_excluded_expenses
                                )
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleShowExcludedExpenses(!showExcludedExpenses)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalExpenditureAmount(
    expenditure: Double,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.total_expenditure),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        VerticalNumberSpinnerContent(expenditure) { amount ->
            Text(
                text = TextFormat.currency(amount),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    note: String,
    amount: String,
    date: LocalDate,
    selected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    showExcludedIndicator: Boolean,
    modifier: Modifier = Modifier
) {
    ExpenseListItem(
        note = note,
        amount = amount,
        date = date,
        tag = null,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_expense_selection_change),
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(R.string.cd_long_press_to_select_expense)
            ),
        tonalElevation = if (selected) ElevationLevel1 else ElevationLevel0,
        overlineContent = if (showExcludedIndicator) {
            { Text(stringResource(R.string.excluded_expense)) }
        } else null
    )
}

@Composable
private fun DeleteTagDialog(
    onDeleteTag: () -> Unit,
    onDeleteTagWithExpenses: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpacingExtraSmall)
            ) {
                Button(
                    onClick = onDeleteTag,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_delete))
                }
                OutlinedButton(
                    onClick = onDeleteTagWithExpenses,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_tag_with_expenses))
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        },
        title = { Text(stringResource(R.string.delete_tag_confirmation_title)) },
        text = { Text(stringResource(R.string.action_irreversible_message)) }
    )
}

@Preview
@Composable
private fun PreviewAllExpensesScreen() {
    MYMTheme {
        AllExpensesScreen(
            snackbarController = rememberSnackbarController(),
            state = AllExpensesState(),
            tagNameInput = { "" },
            tagColorInput = { TagColors.first().toArgb() },
            actions = object : AllExpensesActions {
                override fun onMonthSelect(month: Month) {}
                override fun onYearSelect(year: Int) {}
                override fun onTagClick(tag: ExpenseTag) {}
                override fun onNewTagClick() {}
                override fun onNewTagNameChange(value: String) {}
                override fun onNewTagColorSelect(color: Color) {}
                override fun onNewTagInputDismiss() {}
                override fun onNewTagInputConfirm() {}
                override fun onToggleShowExcludedExpenses(value: Boolean) {}
                override fun onExpenseLongClick(id: Long) {}
                override fun onExpenseClick(id: Long) {}
                override fun onSelectionStateChange() {}
                override fun onDismissMultiSelectionMode() {}
                override fun onExpenseOptionClick(option: ExpenseOption) {}
                override fun onDeleteExpenseDismiss() {}
                override fun onDeleteExpenseConfirm() {}
                override fun onEditTagClick(tag: ExpenseTag) {}
                override fun onDeleteTagClick(tagId: Long) {}
                override fun onDeleteTagDismiss() {}
                override fun onDeleteTagConfirm() {}
                override fun onDeleteTagWithExpensesClick() {}
                override fun onDeleteSelectedExpensesClick() {}
            },
            navigateUp = {}
        )
    }
}