package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.TextFormat
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.ConfirmationDialog
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.MinWidthOutlinedTextField
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.util.contentColor
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.presentation.components.AmountRecommendationsRow
import dev.ridill.mym.expense.presentation.components.NewTagChip
import dev.ridill.mym.expense.presentation.components.NewTagDialog

@Composable
fun AddEditExpenseScreen(
    snackbarController: SnackbarController,
    amountInput: () -> String,
    noteInput: () -> String,
    tagNameInput: () -> String,
    tagColorInput: () -> Int?,
    isEditMode: Boolean,
    state: AddEditExpenseState,
    actions: AddEditExpenseActions,
    navigateUp: () -> Unit
) {
    val amountFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditMode) {
        if (!isEditMode)
            amountFocusRequester.requestFocus()
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.expenseTimestamp),
        yearRange = IntRange(DatePickerDefaults.YearRange.first, state.expenseTimestamp.year)
    )

    MYMScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isEditMode) R.string.destination_edit_expense
                            else R.string.destination_add_expense
                        )
                    )
                },
                navigationIcon = {
                    BackArrowButton(onClick = navigateUp)
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = actions::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = actions::onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.cd_save)
                )
            }
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding(),
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            VerticalSpacer(spacing = SpacingLarge)

            AmountInput(
                amount = amountInput,
                onAmountChange = actions::onAmountChange,
                modifier = Modifier
                    .focusRequester(amountFocusRequester)
            )

            NoteInput(
                input = noteInput,
                onValueChange = actions::onNoteChange,
                onFocused = actions::onNoteInputFocused
            )

            if (!isEditMode) {
                AmountRecommendationsRow(
                    recommendations = state.amountRecommendations,
                    onRecommendationClick = {
                        actions.onRecommendedAmountClick(it)
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    modifier = Modifier
                        .fillMaxWidth(AMOUNT_RECOMMENDATION_WIDTH_FRACTION)
                )
            }

            Divider()

            ExpenseDate(
                date = state.expenseDateFormatted,
                modifier = Modifier
                    .align(Alignment.End),
                onDateClick = actions::onExpenseTimestampClick
            )

            TagsList(
                tagsList = state.tagsList,
                selectedTag = state.selectedTagId,
                onTagClick = actions::onTagClick,
                onNewTagClick = actions::onNewTagClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = SpacingListEnd)
            )
        }

        if (state.showDeleteConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_expense_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteConfirm,
                onDismiss = actions::onDeleteDismiss
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

        if (state.showDateTimePicker) {
            DatePickerDialog(
                onDismissRequest = actions::onExpenseTimestampSelectionDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val dateTime =
                                DateUtil.dateFromMillisWithTime(it, state.expenseTimestamp)
                            actions.onExpenseTimestampSelectionConfirm(dateTime)
                        }
                    }) {
                        Text(stringResource(R.string.action_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = actions::onExpenseTimestampSelectionDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            ) {
                DatePicker(datePickerState)
            }
        }
    }
}

@Composable
private fun AmountInput(
    amount: () -> String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MinWidthOutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier,
        prefix = { Text(TextFormat.currencySymbol()) },
        textStyle = MaterialTheme.typography.headlineMedium,
        placeholder = {
            Text(
                text = stringResource(R.string.amount_zero),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        )
    )
}

@Composable
fun NoteInput(
    input: () -> String,
    onValueChange: (String) -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = input(),
        onValueChange = onValueChange,
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) onFocused()
            },
        placeholder = { Text(stringResource(R.string.add_a_note)) },
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}

private const val AMOUNT_RECOMMENDATION_WIDTH_FRACTION = 0.80f

@Composable
private fun ExpenseDate(
    date: String,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodyLarge
        )
        FilledTonalIconButton(onClick = onDateClick) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_clock),
                contentDescription = stringResource(R.string.cd_expense_date)
            )
        }
    }
}

@Composable
fun TagsList(
    tagsList: List<ExpenseTag>,
    selectedTag: String?,
    onTagClick: (String) -> Unit,
    onNewTagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        Text(text = stringResource(R.string.tag_your_expense))
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            tagsList.forEach { tag ->
                FilterChip(
                    selected = tag.name == selectedTag,
                    onClick = { onTagClick(tag.name) },
                    label = {
                        Text(
                            text = tag.name,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tag.color,
                        selectedLabelColor = tag.color.contentColor()
                    )
                )
            }

            NewTagChip(onClick = onNewTagClick)
        }
    }
}