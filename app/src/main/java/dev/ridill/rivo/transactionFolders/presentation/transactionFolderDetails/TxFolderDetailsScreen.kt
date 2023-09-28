package dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import dev.ridill.rivo.core.ui.navigation.destinations.TransactionFolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
fun TxFolderDetailsScreen(
    snackbarController: SnackbarController,
    state: TxFolderDetailsState,
    folderName: () -> String,
    actions: TxFolderDetailsActions,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    BackHandler(
        enabled = state.editModeActive,
        onBack = actions::onEditDismiss
    )

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TransactionFolderDetailsScreenSpec.labelRes)) },
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
                                contentDescription = stringResource(R.string.cd_delete)
                            )
                        }
                    }
                    if (state.editModeActive) {
                        IconButton(onClick = actions::onEditConfirm) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.cd_save_transaction_folder)
                            )
                        }
                    } else {
                        IconButton(onClick = actions::onEditClick) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.cd_edit_transaction_folder)
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

            FolderCreatedDate(
                date = state.createdTimestampFormatted,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
                    .align(Alignment.End)
            )

            if (!state.isNewFolder) {
                TransactionsInFolder(
                    currency = state.currency,
                    aggregateAmount = state.aggregateAmount,
                    aggregateType = state.aggregateType,
                    transactionsMap = state.transactions,
                    onTransactionClick = { navigateToAddEditTransaction(it) },
                    onNewTransactionClick = { navigateToAddEditTransaction(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(Float.One),
                    insetPadding = paddingValues
                )
            }
        }

        if (state.showDeleteConfirmation) {
            if (state.transactions.isEmpty()) {
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
            contentDescription = stringResource(R.string.cd_transaction_folder_created_date)
        )
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
        textStyle = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun TransactionsInFolder(
    currency: Currency,
    aggregateAmount: Double,
    aggregateType: TransactionType?,
    transactionsMap: Map<LocalDate, List<TransactionListItem>>,
    onTransactionClick: (Long) -> Unit,
    onNewTransactionClick: () -> Unit,
    modifier: Modifier = Modifier,
    insetPadding: PaddingValues = PaddingValues()
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (transactionsMap.isEmpty()) {
            EmptyListIndicator(
                resId = R.raw.lottie_empty_list_ghost
            )
        }
        Column(
            modifier = Modifier
                .matchParentSize()
        ) {
            AggregateAmount(
                currency = currency,
                amount = aggregateAmount,
                type = aggregateType,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )

            Divider(
                modifier = Modifier
                    .padding(
                        vertical = SpacingSmall,
                        horizontal = SpacingMedium
                    )
            )

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
                        contentDescription = stringResource(R.string.cd_new_transaction)
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
                transactionsMap.forEach { (date, transactions) ->
                    stickyHeader(key = date.toString()) {
                        TransactionDateHeader(
                            date = date,
                            modifier = Modifier
                                .animateItemPlacement()
                        )
                    }

                    items(items = transactions, key = { it.id }) { transaction ->
                        TransactionCard(
                            note = transaction.note,
                            amount = TextFormat.compactNumber(
                                value = transaction.amount,
                                currency = currency
                            ),
                            date = transaction.date,
                            type = transaction.type,
                            tag = transaction.tag,
                            onClick = { onTransactionClick(transaction.id) },
                            modifier = Modifier
                                .animateItemPlacement()
                        )
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
    Row(
        modifier = modifier
    ) {
        VerticalNumberSpinnerContent(
            number = amount.absoluteValue,
            modifier = Modifier
                .alignByBaseline()
        ) {
            Text(
                text = TextFormat.currency(it, currency = currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        SpacerSmall()

        Crossfade(
            targetState = aggregateTypeText,
            label = "AggregateType",
            modifier = Modifier
                .alignByBaseline()
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TransactionDateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
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
    tag: TransactionTag?,
    type: TransactionType,
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
            tag = tag,
            type = type
        )
    }
}