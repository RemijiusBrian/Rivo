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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.Formatter
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.ConfirmationDialog
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.MinWidthTextField
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.util.contentColor
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.presentation.components.NewTagChip

@Composable
fun AddEditExpenseScreen(
    snackbarHostState: SnackbarHostState,
    amountInput: () -> String,
    noteInput: () -> String,
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = actions::onSave) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.cd_save)
                )
            }
        },
        modifier = Modifier
            .imePadding(),
        snackbarHostState = snackbarHostState
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
                AmountRecommendations(
                    recommendations = state.amountRecommendations,
                    onRecommendationClick = {
                        actions.onRecommendedAmountClick(it)
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    modifier = Modifier
                        .fillMaxWidth(AMOUNT_RECOMMENDATION_WIDTH_FRACTION)
                )
            }

            ExpenseDate(
                date = state.expenseDateFormatted,
                time = state.expenseTimeFormatted,
                modifier = Modifier
                    .align(Alignment.Start)
            )

            TagsList(
                tagsList = state.tagsList,
                selectedTag = state.selectedTagId,
                onTagClick = actions::onTagClick,
                modifier = Modifier
                    .fillMaxWidth(),
                onNewTagClick = actions::onNoteInputFocused
            )
        }

        if (state.showDeleteConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_expense_confirmation_title,
                contentRes = R.string.delete_expense_confirmation_content,
                onConfirm = actions::onDeleteConfirm,
                onDismiss = actions::onDeleteDismiss
            )
        }
    }
}

@Composable
private fun AmountInput(
    amount: () -> String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MinWidthTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier,
        leadingIcon = { Text(Formatter.currencySymbol()) },
        textStyle = MaterialTheme.typography.headlineMedium,
        placeholder = { Text(stringResource(R.string.amount_zero)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
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

@Composable
private fun AmountRecommendations(
    recommendations: List<Long>,
    onRecommendationClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        recommendations.forEach { amount ->
            SuggestionChip(
                onClick = { onRecommendationClick(amount) },
                label = { Text(Formatter.currency(amount)) }
            )
        }
    }
}

private const val AMOUNT_RECOMMENDATION_WIDTH_FRACTION = 0.80f

@Composable
private fun ExpenseDate(
    date: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_clock),
            contentDescription = stringResource(R.string.cd_expense_date)
        )
        Column {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current
                    .copy(alpha = ContentAlpha.SUB_CONTENT)
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
                    label = { Text(tag.name) },
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

@Composable
private fun NewChipInput() {

}