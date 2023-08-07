package dev.ridill.mym.expense.presentation.allExpenses

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.domain.util.TextFormatUtil
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.ConfirmationDialog
import dev.ridill.mym.core.ui.components.EmptyListIndicator
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesDestination
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.ElevationLevel0
import dev.ridill.mym.core.ui.theme.ElevationLevel1
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.util.contentColor
import dev.ridill.mym.expense.domain.model.ExpenseBulkOperation
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import dev.ridill.mym.expense.presentation.components.BaseExpenseLayout
import dev.ridill.mym.expense.presentation.components.NewTagDialog
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AllExpensesScreen(
    snackbarHostState: SnackbarHostState,
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

    MYMScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.expenseMultiSelectionModeActive)
                            stringResource(R.string.count_selected, state.selectedExpenseIds.size)
                        else stringResource(AllExpensesDestination.labelRes)
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            TagsInfoList(
                tags = state.tagsWithExpenditures,
                selectedTagId = state.selectedTag,
                onTagClick = actions::onTagClick,
                onTagDeleteClick = actions::onDeleteTagClick,
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
                expenseList = state.expenseList,
                selectedExpenseIds = state.selectedExpenseIds,
                selectionState = state.expenseSelectionState,
                multiSelectionModeActive = state.expenseMultiSelectionModeActive,
                onSelectionStateChange = actions::onSelectionStateChange,
                onBulkOperationClick = actions::onExpenseBulkOperationClick,
                onExpenseLongClick = actions::onExpenseLongClick,
                onExpenseClick = actions::onExpenseClick,
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
            ConfirmationDialog(
                titleRes = R.string.delete_tag_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteTagConfirm,
                onDismiss = actions::onDeleteTagDismiss
            )
        }

        if (state.showNewTagInput) {
            NewTagDialog(
                nameInput = tagNameInput,
                onNameChange = actions::onNewTagNameChange,
                selectedColorCode = tagColorInput(),
                onColorSelect = actions::onNewTagColorSelect,
                onDismiss = actions::onNewTagInputDismiss,
                onConfirm = actions::onNewTagInputConfirm
            )
        }
    }
}

@Composable
private fun TagsInfoList(
    tags: List<TagWithExpenditure>,
    selectedTagId: String?,
    onTagClick: (String) -> Unit,
    onTagDeleteClick: (String) -> Unit,
    onNewTagClick: () -> Unit,
    tagAssignModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        LazyRow(
            modifier = modifier,
            contentPadding = PaddingValues(
                start = SpacingMedium,
                end = SpacingListEnd
            ),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            items(items = tags) { item ->
                TagInfoCard(
                    name = item.tag.name,
                    color = item.tag.color,
                    amount = TextFormatUtil.currency(item.expenditure),
                    percentOfTotalExpenditure = item.percentOfTotalExpenditure,
                    isSelected = item.tag.name == selectedTagId,
                    onClick = { onTagClick(item.tag.name) },
                    onDeleteClick = { onTagDeleteClick(item.tag.name) },
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
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isSelected, label = "IsSelectedTransition")
    val percent by animateFloatAsState(
        targetValue = percentOfTotalExpenditure,
        label = "AnimatedPercentOfTotal"
    )
    val widthFactor by transition.animateFloat(
        label = "TagInfoCardWidthFactor",
        targetValueByState = { if (it) 2f else 1f }
    )
    val textStyleAnimFactor by transition.animateFloat(
        label = "TextStyleAnimationFactor",
        targetValueByState = { if (it) 1f else 0f }
    )
    val selectedTextSize = MaterialTheme.typography.headlineMedium.fontSize
    val unselectedTextSize = MaterialTheme.typography.titleLarge.fontSize
    val titleTextSize by remember(textStyleAnimFactor) {
        derivedStateOf {
            lerp(unselectedTextSize, selectedTextSize, textStyleAnimFactor)
        }
    }
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
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge
                        .copy(
                            fontSize = titleTextSize,
                            fontWeight = FontWeight.SemiBold
                        ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(Float.One)
                )
                AnimatedVisibility(visible = isSelected) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_delete_tag)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = stringResource(R.string.amount_worth_spent, amount),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = stringResource(R.string.percent_of_total, TextFormatUtil.percent(percent)),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT)
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateIndicator(
                date = selectedDate,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
            contentPadding = PaddingValues(
                start = SpacingMedium,
                end = SpacingListEnd
            )
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
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_clock),
                contentDescription = "",
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
        }
    }
}

@Composable
private fun ExpenseList(
    expenseList: List<ExpenseListItem>,
    selectedExpenseIds: List<Long>,
    selectionState: ToggleableState,
    onSelectionStateChange: () -> Unit,
    onBulkOperationClick: (ExpenseBulkOperation) -> Unit,
    multiSelectionModeActive: Boolean,
    onExpenseLongClick: (Long) -> Unit,
    onExpenseClick: (Long) -> Unit,
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
            AnimatedVisibility(visible = multiSelectionModeActive) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ExpenseBulkOperation.values().forEach { operation ->
                        IconButton(onClick = { onBulkOperationClick(operation) }) {
                            Icon(
                                imageVector = operation.icon,
                                contentDescription = stringResource(operation.contentDescriptionRes)
                            )
                        }
                    }
                    TriStateCheckbox(
                        state = selectionState,
                        onClick = onSelectionStateChange
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(
                    bottom = SpacingListEnd
                ),
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                items(items = expenseList, key = { it.id }) { expense ->
                    ExpenseCard(
                        note = expense.note,
                        amount = expense.amount,
                        date = expense.date,
                        selected = expense.id in selectedExpenseIds,
                        onLongClick = { onExpenseLongClick(expense.id) },
                        onClick = { onExpenseClick(expense.id) }
                    )
                }
            }
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
    modifier: Modifier = Modifier
) {
    BaseExpenseLayout(
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
        tonalElevation = if (selected) ElevationLevel1 else ElevationLevel0
    )
}