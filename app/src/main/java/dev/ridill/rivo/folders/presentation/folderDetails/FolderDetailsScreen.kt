package dev.ridill.rivo.folders.presentation.folderDetails

import android.icu.util.Currency
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.navigation.destinations.FolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
fun FolderDetailsScreen(
    snackbarController: SnackbarController,
    state: FolderDetailsState,
    transactionsList: LazyPagingItems<TransactionListItemUIModel>,
    folderName: () -> String,
    actions: FolderDetailsActions,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    BackHandler(
        enabled = state.editModeActive,
        onBack = actions::onEditDismiss
    )

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(FolderDetailsScreenSpec.labelRes)) },
                navigationIcon = {
                    if (state.editModeActive) {
                        IconButton(onClick = actions::onEditDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.action_cancel)
                            )
                        }
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                actions = {
                    if (!state.isNewFolder) {
                        IconButton(onClick = actions::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_folder)
                            )
                        }
                    }
                    if (state.editModeActive) {
                        IconButton(onClick = actions::onEditConfirm) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.cd_save_folder)
                            )
                        }
                    } else {
                        IconButton(onClick = actions::onEditClick) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.cd_edit_folder)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding()
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
            NameField(
                name = folderName,
                onNameChange = actions::onNameChange,
                editModeActive = state.editModeActive,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )

            LabelledSwitch(
                labelRes = R.string.mark_excluded_question,
                checked = state.isExcluded,
                onCheckedChange = actions::onExclusionToggle,
                enabled = state.editModeActive,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
                    .align(Alignment.End)
            )

            AggregateAmountAndCreatedDate(
                currency = state.currency,
                aggregateAmount = state.aggregateAmount,
                aggregateType = state.aggregateType,
                date = state.createdTimestampFormatted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            )

            if (!state.isNewFolder) {
                Divider(
                    modifier = Modifier
                        .padding(horizontal = SpacingMedium)
                )
                TransactionsInFolder(
                    currency = state.currency,
                    onTransactionClick = { navigateToAddEditTransaction(it) },
                    onNewTransactionClick = { navigateToAddEditTransaction(null) },
                    onTransactionSwipeDismiss = actions::onTransactionSwipeToDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(Float.One),
                    insetPadding = paddingValues,
                    pagingItems = transactionsList
                )
            }
        }

        if (state.showDeleteConfirmation) {
            if (transactionsList.itemCount == 0) {
                ConfirmationDialog(
                    titleRes = R.string.delete_transaction_folder_confirmation_title,
                    contentRes = R.string.action_irreversible_message,
                    onConfirm = actions::onDeleteFolderOnlyClick,
                    onDismiss = actions::onDeleteDismiss
                )
            } else {
                MultiActionConfirmationDialog(
                    title = stringResource(R.string.delete_transaction_folder_confirmation_title),
                    text = stringResource(R.string.action_irreversible_message),
                    primaryActionLabelRes = R.string.delete_folder,
                    onPrimaryActionClick = actions::onDeleteFolderOnlyClick,
                    secondaryActionLabelRes = R.string.delete_folder_and_transactions,
                    onSecondaryActionClick = actions::onDeleteFolderAndTransactionsClick,
                    onDismiss = actions::onDeleteDismiss
                )
            }
        }
    }
}

@Composable
private fun NameField(
    name: () -> String,
    onNameChange: (String) -> Unit,
    editModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (editModeActive) MaterialTheme.colorScheme.surfaceVariant
        else Color.Transparent,
        label = "NameFieldContainerColor"
    )

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(editModeActive) {
        if (editModeActive) {
            focusRequester.requestFocus()
        }
    }

    TextField(
        value = name(),
        onValueChange = onNameChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        readOnly = !editModeActive,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        label = { Text(stringResource(R.string.label_transaction_folder_name)) },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = containerColor,
            focusedContainerColor = containerColor
        ),
        textStyle = MaterialTheme.typography.headlineMedium,
        placeholder = { Text(stringResource(R.string.enter_folder_name)) }
    )
}

@Composable
private fun AggregateAmountAndCreatedDate(
    currency: Currency,
    aggregateAmount: Double,
    aggregateType: TransactionType?,
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        AggregateAmount(
            currency = currency,
            amount = aggregateAmount,
            type = aggregateType,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
        )
        SpacerSmall()
        FolderCreatedDate(
            date = date
        )
    }
}

@Composable
private fun FolderCreatedDate(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.created),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        SpacerSmall()
        Icon(
            imageVector = Icons.Outlined.CalendarClock,
            contentDescription = stringResource(R.string.cd_folder_created_date)
        )
    }
}

@Composable
private fun TransactionsInFolder(
    currency: Currency,
    onTransactionClick: (Long) -> Unit,
    onNewTransactionClick: () -> Unit,
    onTransactionSwipeDismiss: (TransactionListItem) -> Unit,
    modifier: Modifier = Modifier,
    insetPadding: PaddingValues = PaddingValues(),
    pagingItems: LazyPagingItems<TransactionListItemUIModel>
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (pagingItems.isEmpty()) {
            EmptyListIndicator(
                resId = R.raw.lottie_empty_list_ghost,
                messageRes = R.string.transactions_in_folder_list_empty_message
            )
        }
        Column(
            modifier = Modifier
                .matchParentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListLabel(stringResource(R.string.transactions))
                FilledTonalIconButton(onClick = onNewTransactionClick) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.cd_new_transaction_fab)
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    top = SpacingSmall,
                    bottom = insetPadding.calculateBottomPadding() + SpacingListEnd
                ),
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                repeat(pagingItems.itemCount) { index ->
                    pagingItems[index]?.let { item ->
                        when (item) {
                            is TransactionListItemUIModel.DateSeparator -> {
                                stickyHeader(
                                    key = item.date.toString(),
                                    contentType = "TransactionDateSeparator"
                                ) {
                                    TransactionDateSeparator(
                                        date = item.date,
                                        modifier = Modifier
                                            .animateItemPlacement()
                                    )
                                }
                            }

                            is TransactionListItemUIModel.TransactionItem -> {
                                item(
                                    key = item.transaction.id,
                                    contentType = "TransactionListItem"
                                ) {
                                    val dismissState = rememberDismissState(
                                        confirmValueChange = {
                                            if (it == DismissValue.DismissedToStart) {
                                                onTransactionSwipeDismiss(item.transaction)
                                            }
                                            true
                                        }
                                    )
                                    SwipeToDismiss(
                                        state = dismissState,
                                        background = {},
                                        directions = setOf(DismissDirection.EndToStart),
                                        dismissContent = {
                                            TransactionCard(
                                                note = item.transaction.note,
                                                amount = item.transaction
                                                    .amountFormattedWithCurrency(currency),
                                                date = item.transaction.date,
                                                type = item.transaction.type,
                                                excluded = item.transaction.excluded,
                                                tag = item.transaction.tag,
                                                onClick = { onTransactionClick(item.transaction.id) },
                                                modifier = Modifier
                                                    .animateItemPlacement()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AggregateAmount(
    amount: Double,
    currency: Currency,
    type: TransactionType?,
    modifier: Modifier = Modifier
) {
    val aggregateTypeText = stringResource(
        id = when (type) {
            TransactionType.CREDIT -> R.string.aggregate_amount_credited
            TransactionType.DEBIT -> R.string.aggregate_amount_debited
            else -> R.string.aggregate_amount_zero
        }
    )
    val aggregateAmountContentDescription = type?.let {
        stringResource(
            R.string.cd_folder_aggregate_amount_unbalanced,
            TextFormat.currency(amount, currency),
            aggregateTypeText
        )
    } ?: stringResource(R.string.cd_folder_aggregate_amount_balanced)
    Row(
        modifier = modifier
            .mergedContentDescription(aggregateAmountContentDescription)
    ) {
        VerticalNumberSpinnerContent(
            number = amount.absoluteValue,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
                .alignBy(LastBaseline)
        ) {
            Text(
                text = TextFormat.currency(it, currency = currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        SpacerSmall()

        Crossfade(
            targetState = aggregateTypeText,
            label = "AggregateType",
            modifier = Modifier
                .alignBy(LastBaseline)
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TransactionDateSeparator(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer
                    .copy(alpha = ContentAlpha.PERCENT_16)
            )
            .padding(
                vertical = SpacingSmall,
                horizontal = SpacingMedium
            )
    ) {
        Text(
            text = date.format(DateUtil.Formatters.MMMM_yyyy_spaceSep),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransactionCard(
    note: String,
    amount: String,
    date: LocalDate,
    type: TransactionType,
    excluded: Boolean,
    tag: Tag?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        TransactionListItem(
            note = note,
            amount = amount,
            date = date,
            type = type,
            tag = tag,
            showTypeIndicator = true,
            excluded = excluded
        )
    }
}