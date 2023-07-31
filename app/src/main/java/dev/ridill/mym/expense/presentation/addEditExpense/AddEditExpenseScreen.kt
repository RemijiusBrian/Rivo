package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Formatter
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.ConfirmationDialog
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.OnLifecycleStartEffect
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall

@Composable
fun AddEditExpenseScreen(
    snackbarHostState: SnackbarHostState,
    amountInput: String,
    noteInput: String,
    isEditMode: Boolean,
    showDeleteConfirmation: Boolean,
    recommendations: List<Long>,
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
            verticalArrangement = Arrangement.spacedBy(SpacingLarge)
        ) {
            VerticalSpacer(spacing = 20.dp)

            AmountInput(
                amount = amountInput,
                onAmountChange = actions::onAmountChange,
                focusRequester = amountFocusRequester
            )

            TextField(
                value = noteInput,
                onValueChange = actions::onNoteChange,
                modifier = Modifier
                    .wrapContentWidth(),
                label = { Text(stringResource(R.string.note)) },
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )

            if (!isEditMode) {
                AmountRecommendations(
                    recommendations = recommendations,
                    onRecommendationClick = {
                        actions.onRecommendedAmountClick(it)
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                )
            }

            ExpenseDate(
                modifier = Modifier
                    .align(Alignment.End)
            )
        }

        if (showDeleteConfirmation) {
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
    amount: String,
    onAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.how_much_did_you_spend),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        TextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier
                .widthIn(min = AmountInputMinWidth)
                .focusRequester(focusRequester),
            leadingIcon = {
                Text(
                    text = Formatter.currencySymbol(),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            textStyle = MaterialTheme.typography.titleLarge,
            placeholder = { Text(stringResource(R.string.amount)) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
    }
}

private val AmountInputMinWidth = 10.dp

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

@Composable
private fun ExpenseDate(
    modifier: Modifier = Modifier
) {
    var date by remember { mutableStateOf("") }
    OnLifecycleStartEffect {
        val currentDateTime = DateUtil.now()
        date = currentDateTime.format(DateUtil.Formatters.localizedLong)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Icon(
            imageVector = Icons.Rounded.CalendarToday,
            contentDescription = stringResource(R.string.expense_deleted)
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}