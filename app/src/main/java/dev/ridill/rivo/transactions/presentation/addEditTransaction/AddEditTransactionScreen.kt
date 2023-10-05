package dev.ridill.rivo.transactions.presentation.addEditTransaction

import android.icu.util.Currency
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.MinWidthOutlinedTextField
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.transactionFolders.presentation.components.FolderListSearchSheet
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.AmountRecommendationsRow
import dev.ridill.rivo.transactions.presentation.components.NewTagChip
import dev.ridill.rivo.transactions.presentation.components.TagInputSheet

@Composable
fun AddEditTransactionScreen(
    snackbarController: SnackbarController,
    amountInput: () -> String,
    noteInput: () -> String,
    tagNameInput: () -> String,
    tagColorInput: () -> Int?,
    tagExclusionInput: () -> Boolean?,
    isEditMode: Boolean,
    folderSearchQuery: () -> String,
    state: AddEditTransactionState,
    actions: AddEditTransactionActions,
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
        initialSelectedDateMillis = DateUtil.toMillis(state.transactionTimestamp),
        yearRange = IntRange(DatePickerDefaults.YearRange.first, state.transactionTimestamp.year)
    )

    RivoScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isEditMode) R.string.destination_edit_transaction
                            else R.string.destination_new_transaction
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
            TransactionTypeSelector(
                currentType = state.transactionType,
                onValueChange = actions::onTransactionTypeChange,
                modifier = Modifier
                    .fillMaxWidth(TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION)
                    .align(Alignment.CenterHorizontally)
            )

            AmountInput(
                currency = state.currency,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FolderIndicator(
                    folderName = state.linkedFolderName,
                    onAddToFolderClick = actions::onAddToFolderClick,
                    onRemoveFolderClick = actions::onRemoveFromFolderClick
                )

                TransactionDate(
                    date = state.transactionDateFormatted,
                    onDateClick = actions::onTransactionTimestampClick
                )
            }

            ExclusionToggle(
                excluded = state.isTransactionExcluded,
                onToggle = actions::onTransactionExclusionToggle,
                modifier = Modifier
                    .align(Alignment.End)
            )

            TagsList(
                tagsList = state.tagsList,
                selectedTagId = state.selectedTagId,
                onTagClick = actions::onTagClick,
                onNewTagClick = actions::onNewTagClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        if (state.showDeleteConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_transaction_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteConfirm,
                onDismiss = actions::onDeleteDismiss
            )
        }

        if (state.showNewTagInput) {
            TagInputSheet(
                nameInput = tagNameInput,
                onNameChange = actions::onNewTagNameChange,
                selectedColorCode = tagColorInput,
                onColorSelect = actions::onNewTagColorSelect,
                excluded = tagExclusionInput,
                onExclusionToggle = actions::onNewTagExclusionChange,
                onDismiss = actions::onNewTagInputDismiss,
                onConfirm = actions::onNewTagInputConfirm,
                errorMessage = state.newTagError,
                isEditMode = { false },
                onDeleteClick = null
            )
        }

        if (state.showDateTimePicker) {
            DatePickerDialog(
                onDismissRequest = actions::onTransactionTimestampSelectionDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val dateTime =
                                DateUtil.dateFromMillisWithTime(it, state.transactionTimestamp)
                            actions.onTransactionTimestampSelectionConfirm(dateTime)
                        }
                    }) {
                        Text(stringResource(R.string.action_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = actions::onTransactionTimestampSelectionDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            ) {
                DatePicker(datePickerState)
            }
        }

        if (state.showFolderSelection) {
            FolderListSearchSheet(
                searchQuery = folderSearchQuery,
                onSearchQueryChange = actions::onFolderSearchQueryChange,
                foldersList = state.folderList,
                onFolderClick = actions::onFolderSelect,
                onCreateNewClick = actions::onCreateFolderClick,
                onDismiss = actions::onFolderSelectionDismiss
            )
        }
    }
}

@Composable
private fun AmountInput(
    currency: Currency,
    amount: () -> String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MinWidthOutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier,
        prefix = { Text(currency.symbol) },
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

private const val TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION = 0.80f
private const val AMOUNT_RECOMMENDATION_WIDTH_FRACTION = 0.80f

@Composable
private fun TransactionDate(
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
                imageVector = Icons.Outlined.CalendarClock,
                contentDescription = stringResource(R.string.cd_transaction_date)
            )
        }
    }
}

@Composable
private fun FolderIndicator(
    folderName: String?,
    onAddToFolderClick: () -> Unit,
    onRemoveFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFolderLinked by remember(folderName) {
        derivedStateOf { !folderName.isNullOrEmpty() }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(targetState = isFolderLinked, label = "FolderIcon") { isLinked ->
            IconButton(
                onClick = {
                    if (isLinked) onRemoveFolderClick()
                    else onAddToFolderClick()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = if (isLinked) R.drawable.ic_outline_remove_folder
                        else R.drawable.ic_outline_move_to_folder
                    ),
                    contentDescription = stringResource(R.string.cd_add_to_folder)
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.transaction_folder_indicator_label),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold
            )
            Crossfade(
                targetState = folderName ?: stringResource(R.string.add_to_folder),
                label = "FolderNameContent"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    currentType: TransactionType,
    onValueChange: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex by remember(currentType) {
        derivedStateOf {
            TransactionType.values().indexOf(currentType)
        }
    }
    val indicatorColor = MaterialTheme.colorScheme.secondaryContainer
        .copy(alpha = ContentAlpha.PERCENT_32)
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier
            .clip(CircleShape)
            .border(
                width = BorderWidthStandard,
                color = indicatorColor,
                shape = CircleShape
            )
            .then(modifier),
        indicator = {
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(it[selectedIndex])
                    .fillMaxSize()
                    .drawBehind {
                        drawRoundRect(color = indicatorColor)
                    }
            )
        },
        divider = {}
    ) {
        TransactionType.values().forEach { direction ->
            Tab(
                selected = currentType == direction,
                onClick = { onValueChange(direction) },
                text = {
                    Text(
                        text = stringResource(direction.labelRes),
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier,
                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedContentColor = LocalContentColor.current
            )
        }
    }
}

@Composable
private fun ExclusionToggle(
    excluded: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        LabelledSwitch(
            labelRes = R.string.exclude_from_expenditure_ques,
            checked = excluded,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun TagsList(
    tagsList: List<Tag>,
    selectedTagId: Long?,
    onTagClick: (Long) -> Unit,
    onNewTagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        Text(text = stringResource(R.string.tag_your_transaction))
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            tagsList.forEach { tag ->
                val selected = tag.id == selectedTagId
                FilterChip(
                    selected = selected,
                    onClick = { onTagClick(tag.id) },
                    label = {
                        Text(
                            text = tag.name,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (tag.excluded) TextDecoration.LineThrough
                            else null
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