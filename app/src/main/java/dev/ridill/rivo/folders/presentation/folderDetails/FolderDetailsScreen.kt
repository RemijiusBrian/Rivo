package dev.ridill.rivo.folders.presentation.folderDetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.DismissBackground
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.ListEmptyIndicatorItem
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.SwipeToDismissContainer
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.navigation.destinations.FolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.folders.domain.model.AggregateType
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import java.time.LocalDate
import java.util.Currency
import kotlin.math.absoluteValue

@Composable
fun FolderDetailsScreen(
    appCurrencyPreference: Currency,
    snackbarController: SnackbarController,
    state: FolderDetailsState,
    transactionPagingItems: LazyPagingItems<TransactionListItemUIModel>,
    folderName: () -> String,
    actions: FolderDetailsActions,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    val areTransactionsEmpty by remember {
        derivedStateOf { transactionPagingItems.isEmpty() }
    }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                top = SpacingMedium,
                bottom = SpacingListEnd
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            item(
                key = "FolderDetails",
                contentType = "FolderDetails"
            ) {
                FolderDetails(
                    folderName = folderName,
                    onNameChange = actions::onNameChange,
                    editModeActive = state.editModeActive,
                    isExcluded = state.isExcluded,
                    onExclusionToggle = actions::onExclusionToggle,
                    currency = appCurrencyPreference,
                    aggregateAmount = state.aggregateAmount,
                    aggregateType = state.aggregateType,
                    createdTimestamp = state.createdTimestampFormatted,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItemPlacement()
                )
            }

            if (!state.isNewFolder) {
                stickyHeader(
                    key = "TransactionListHeader",
                    contentType = "TransactionListHeader"
                ) {
                    TransactionListHeader(
                        onNewTransactionClick = { navigateToAddEditTransaction(null) },
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = SpacingMedium)
                            .animateItemPlacement()
                    )
                }

                if (areTransactionsEmpty) {
                    item(
                        key = "EmptyListIndicator",
                        contentType = "EmptyListIndicator"
                    ) {
                        ListEmptyIndicatorItem(
                            rawResId = R.raw.lottie_empty_list_ghost,
                            messageRes = R.string.transactions_in_folder_list_empty_message
                        )
                    }
                }

                repeat(transactionPagingItems.itemCount) { index ->
                    transactionPagingItems[index]?.let { item ->
                        when (item) {
                            is TransactionListItemUIModel.DateSeparator -> {
                                stickyHeader(
                                    key = item.date.toString(),
                                    contentType = "TransactionDateSeparator"
                                ) {
                                    ListSeparator(
                                        label = item.date.format(DateUtil.Formatters.MMMM_yyyy_spaceSep),
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
                                    SwipeToDismissContainer(
                                        item = item.transaction,
                                        onDismiss = actions::onTransactionSwipeToDismiss,
                                        backgroundContent = {
                                            DismissBackground(
                                                swipeDismissState = it,
                                                icon = ImageVector.vectorResource(R.drawable.ic_outline_remove_folder),
                                                contentDescription = stringResource(R.string.cd_remove_from_folder),
                                                enableDismissFromEndToStart = false,
                                                modifier = Modifier
                                                    .padding(horizontal = SpacingLarge)
                                            )
                                        },
                                        enableDismissFromEndToStart = false,
                                        modifier = Modifier
                                            .animateItemPlacement()
                                    ) {
                                        TransactionCard(
                                            note = item.transaction.note,
                                            amount = item.transaction
                                                .amountFormattedWithCurrency(appCurrencyPreference),
                                            date = item.transaction.date,
                                            type = item.transaction.type,
                                            excluded = item.transaction.excluded,
                                            tag = item.transaction.tag,
                                            onClick = { navigateToAddEditTransaction(item.transaction.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showDeleteConfirmation) {
            if (transactionPagingItems.itemCount == 0) {
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
private fun FolderDetails(
    folderName: () -> String,
    onNameChange: (String) -> Unit,
    editModeActive: Boolean,
    isExcluded: Boolean,
    onExclusionToggle: (Boolean) -> Unit,
    currency: Currency,
    aggregateAmount: Double,
    aggregateType: AggregateType,
    createdTimestamp: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        NameField(
            name = folderName,
            onNameChange = onNameChange,
            editModeActive = editModeActive,
            modifier = Modifier
                .fillMaxWidth()
        )

        LabelledSwitch(
            labelRes = R.string.mark_excluded_question,
            checked = isExcluded,
            onCheckedChange = onExclusionToggle,
            enabled = editModeActive,
            modifier = Modifier
                .align(Alignment.End)
        )

        AggregateAmountAndCreatedDate(
            currency = currency,
            aggregateAmount = aggregateAmount,
            aggregateType = aggregateType,
            date = createdTimestamp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingMedium)
        )

        HorizontalDivider()
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
    aggregateType: AggregateType,
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
private fun TransactionListHeader(
    onNewTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
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
}

@Composable
private fun AggregateAmount(
    amount: Double,
    currency: Currency,
    type: AggregateType,
    modifier: Modifier = Modifier
) {
    val aggregateAmountContentDescription = when (type) {
        AggregateType.BALANCED -> stringResource(R.string.cd_folder_aggregate_amount_balanced)
        else -> stringResource(
            R.string.cd_folder_aggregate_amount_unbalanced,
            TextFormat.currency(amount, currency),
            stringResource(type.labelRes)
        )
    }
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
                style = MaterialTheme.typography.headlineLarge
                    .copy(lineBreak = LineBreak.Heading),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        SpacerExtraSmall()

        Crossfade(
            targetState = type.labelRes,
            label = "AggregateType",
            modifier = Modifier
                .alignBy(LastBaseline)
        ) { resId ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.titleMedium
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