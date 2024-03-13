package dev.ridill.rivo.transactions.presentation.addEditTransaction

import android.icu.util.Currency
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.components.AmountVisualTransformation
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.LabelledRadioButton
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.MinWidthOutlinedTextField
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.TabSelector
import dev.ridill.rivo.core.ui.components.TabSelectorItem
import dev.ridill.rivo.core.ui.components.TextFieldSheet
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.SchedulesAndPlansListScreenSpec
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.presentation.components.FolderListSearchSheet
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.AddEditTxOption
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.AmountRecommendationsRow
import dev.ridill.rivo.transactions.presentation.components.NewTagChip
import dev.ridill.rivo.transactions.presentation.components.TagChip
import dev.ridill.rivo.transactions.presentation.components.TagInputSheet
import kotlinx.coroutines.flow.flowOf
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.enums.EnumEntries

@Composable
fun AddEditTransactionScreen(
    isEditMode: Boolean,
    snackbarController: SnackbarController,
    amountInput: () -> String,
    noteInput: () -> String,
    tagNameInput: () -> String,
    tagColorInput: () -> Color?,
    tagExclusionInput: () -> Boolean?,
    folderSearchQuery: () -> String,
    folderList: LazyPagingItems<Folder>,
    state: AddEditTransactionState,
    actions: AddEditTransactionActions
) {
    val amountFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    BackHandler(
        enabled = true,
        onBack = actions::onBackNav
    )

    LaunchedEffect(isEditMode) {
        if (!isEditMode)
            amountFocusRequester.requestFocus()
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val dateNow = remember { DateUtil.dateNow() }
    val datePickerState = if (state.isScheduleTxMode) rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.transactionTimestamp),
        yearRange = IntRange(dateNow.year, DatePickerDefaults.YearRange.last),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= DateUtil.toMillis(
                    date = dateNow.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    else rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.transactionTimestamp),
        yearRange = IntRange(DatePickerDefaults.YearRange.first, dateNow.year),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis < DateUtil.toMillis(
                    date = dateNow.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )

    RivoScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isEditMode) R.string.destination_edit_transaction else R.string.destination_new_transaction
                        )
                    )
                },
                navigationIcon = { BackArrowButton(onClick = actions::onBackNav) },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = actions::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_transaction)
                            )
                        }
                    }

                    AddEditOptions(onOptionClick = actions::onAddEditOptionSelect)
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding(),
        snackbarController = snackbarController
    ) { paddingValues ->
        Box {
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
                    selectedType = state.transactionType,
                    onValueChange = actions::onTransactionTypeChange,
                    modifier = Modifier
                        .fillMaxWidth(TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION)
                        .align(Alignment.CenterHorizontally)
                )

                AmountInput(
                    currency = state.currency,
                    amount = amountInput,
                    onAmountChange = actions::onAmountChange,
                    onTransformClick = actions::onTransformAmountClick,
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
                        currency = state.currency,
                        recommendations = state.amountRecommendations,
                        onRecommendationClick = {
                            actions.onRecommendedAmountClick(it)
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        modifier = Modifier
                            .fillMaxWidth(AMOUNT_RECOMMENDATION_WIDTH_FRACTION)
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FolderIndicator(
                        folderName = state.linkedFolderName,
                        onAddToFolderClick = actions::onAddToFolderClick,
                        onRemoveFolderClick = actions::onRemoveFromFolderClick,
                        modifier = Modifier
                            .weight(weight = Float.One, fill = false)
                    )

                    TransactionDate(
                        date = state.transactionDateFormatted,
                        onDateClick = actions::onTransactionTimestampClick,
                        modifier = Modifier
                            .weight(weight = Float.One, fill = false)
                    )
                }

                ExclusionToggle(
                    excluded = state.isTransactionExcluded,
                    onToggle = actions::onTransactionExclusionToggle,
                    modifier = Modifier
                        .align(Alignment.End)
                )

                AnimatedVisibility(
                    visible = state.isScheduleTxMode,
                    modifier = Modifier
                        .align(Alignment.Start)
                ) {
                    TransactionRepeatModeIndicator(
                        selectedRepeatMode = state.selectedRepeatMode,
                        onClick = actions::onRepeatModeClick,
                        onCancelClick = actions::onCancelSchedulingClick
                    )
                }

                TagsList(
                    tagsList = state.tagsList,
                    selectedTagId = state.selectedTagId,
                    onTagClick = actions::onTagClick,
                    onNewTagClick = actions::onNewTagClick,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = state.isLoading,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        if (state.showDeleteConfirmation) {
            ConfirmationDialog(
                titleRes = if (state.isScheduleTxMode) R.string.delete_schedule_confirmation_title
                else R.string.delete_transaction_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteConfirm,
                onDismiss = actions::onDeleteDismiss
            )
        }

        if (state.showNewTagInput) {
            TagInputSheet(
                nameInput = tagNameInput,
                onNameChange = actions::onNewTagNameChange,
                selectedColor = tagColorInput,
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
                        datePickerState.selectedDateMillis
                            ?.let(actions::onTransactionTimestampSelectionConfirm)
                    }) { Text(stringResource(R.string.action_ok)) }
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
                foldersList = folderList,
                onFolderClick = actions::onFolderSelect,
                onCreateNewClick = actions::onCreateFolderClick,
                onDismiss = actions::onFolderSelectionDismiss
            )
        }

        if (state.showTransformationInput) {
            AmountTransformationSheet(
                onDismiss = actions::onTransformAmountDismiss,
                selectedTransformation = state.selectedAmountTransformation,
                onTransformationSelect = actions::onAmountTransformationSelect,
                onTransformClick = actions::onAmountTransformationConfirm
            )
        }

        if (state.showRepeatModeSelection) {
            RepeatModeSelectionSheet(
                onDismiss = actions::onRepeatModeDismiss,
                selectedRepeatMode = state.selectedRepeatMode,
                onRepeatModeSelect = actions::onRepeatModeSelect
            )
        }
    }
}

@Composable
private fun AmountInput(
    currency: Currency,
    amount: () -> String,
    onAmountChange: (String) -> Unit,
    onTransformClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountContentDescription = stringResource(R.string.cd_enter_amount)
    val showTransformButton by remember {
        derivedStateOf { amount().toDoubleOrNull().orZero() > Double.Zero }
    }
    val focusManager = LocalFocusManager.current
    MinWidthOutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier
            .semantics {
                contentDescription = amountContentDescription
            },
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
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        ),
        visualTransformation = remember { AmountVisualTransformation() },
        trailingIcon = {
            AnimatedVisibility(visible = showTransformButton) {
                IconButton(onClick = onTransformClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_gears),
                        contentDescription = stringResource(R.string.cd_transform_amount)
                    )
                }
            }
        }
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
            capitalization = KeyboardCapitalization.Sentences,
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
                contentDescription = stringResource(R.string.cd_click_to_change_transaction_date)
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
        Crossfade(
            targetState = isFolderLinked,
            label = "FolderIcon"
        ) { isLinked ->
            FilledTonalIconButton(
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
                    contentDescription = stringResource(R.string.cd_add_transaction_to_folder)
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.transaction_folder_indicator_label),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = folderName ?: stringResource(R.string.add_to_folder),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(
                        onClick = onAddToFolderClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClickLabel = stringResource(R.string.cd_click_to_change_folder),
                        role = Role.Button
                    )
            )
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onValueChange: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val typeSelectorContentDescription = stringResource(
        R.string.cd_transaction_type_selector,
        stringResource(selectedType.labelRes)
    )

    val typesCount = remember { TransactionType.entries.size }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .semantics {
                contentDescription = typeSelectorContentDescription
            },
    ) {
        TransactionType.entries.forEachIndexed { index, type ->
            val selected = selectedType == type
            val transactionTypeSelectorContentDescription = if (!selected)
                stringResource(
                    R.string.cd_transaction_type_selector_unselected,
                    stringResource(type.labelRes)
                )
            else null
            SegmentedButton(
                selected = selected,
                onClick = { onValueChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = typesCount),
                modifier = Modifier
                    .semantics {
                        transactionTypeSelectorContentDescription?.let {
                            contentDescription = it
                        }
                    }
            ) {
                Text(stringResource(type.labelRes))
            }
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
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        LabelledSwitch(
            labelRes = R.string.mark_excluded_question,
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
            NewTagChip(onClick = onNewTagClick)
            tagsList.forEach { tag ->
                TagChip(
                    name = tag.name,
                    color = tag.color,
                    excluded = tag.excluded,
                    selected = tag.id == selectedTagId,
                    onClick = { onTagClick(tag.id) }
                )
            }
        }
    }
}

@Composable
private fun AmountTransformationSheet(
    onDismiss: () -> Unit,
    selectedTransformation: AmountTransformation,
    onTransformationSelect: (AmountTransformation) -> Unit,
    onTransformClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val input = rememberSaveable { mutableStateOf(String.Empty) }
    val keyboardOptions = remember {
        KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
    }
    val labelRes = remember(selectedTransformation) {
        when (selectedTransformation) {
            AmountTransformation.DIVIDE_BY -> R.string.enter_divider
            AmountTransformation.MULTIPLIER -> R.string.enter_multiplier
            AmountTransformation.PERCENT -> R.string.enter_percent
        }
    }
    TextFieldSheet(
        title = {
            Text(
                text = stringResource(R.string.transform_amount),
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )
        },
        inputValue = { input.value },
        onValueChange = { input.value = it },
        onDismiss = onDismiss,
        text = {
            TabSelector(
                values = { AmountTransformation.entries },
                selectedItem = { selectedTransformation },
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            ) { transformation ->
                TabSelectorItem(
                    selected = transformation == selectedTransformation,
                    onClick = { onTransformationSelect(transformation) },
                    text = { Text(stringResource(transformation.labelRes)) }
                )
            }
        },
        actionButton = {
            Button(onClick = { onTransformClick(input.value) }) {
                Text(text = stringResource(R.string.transform))
            }
        },
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        label = stringResource(labelRes),
        suffix = { Text(selectedTransformation.symbol) },
        textStyle = LocalTextStyle.current
            .copy(textAlign = TextAlign.End),
        showClearOption = false
    )
}

@Composable
private fun AddEditOptions(
    onOptionClick: (AddEditTxOption) -> Unit,
    modifier: Modifier = Modifier,
    options: EnumEntries<AddEditTxOption> = remember { AddEditTxOption.entries }
) {
    var optionsExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { optionsExpanded = !optionsExpanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_options)
            )
        }

        DropdownMenu(
            expanded = optionsExpanded,
            onDismissRequest = { optionsExpanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        optionsExpanded = false
                        onOptionClick(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun TransactionRepeatModeIndicator(
    selectedRepeatMode: ScheduleRepeatMode,
    onClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        ElevatedAssistChip(
            onClick = onClick,
            label = {
                Text(
                    text = stringResource(
                        R.string.repeat_mode_label_transaction,
                        stringResource(selectedRepeatMode.labelRes)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_repeat_duration),
                    contentDescription = stringResource(R.string.cd_transaction_repeat_mode)
                )
            }
        )

        SpacerExtraSmall()

        Text(
            text = stringResource(
                R.string.scheduled_transactions_can_be_found_in_corresponding_screen,
                stringResource(SchedulesAndPlansListScreenSpec.labelRes)
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .widthIn(max = 240.dp)
        )
    }
}

@Composable
private fun RepeatModeSelectionSheet(
    onDismiss: () -> Unit,
    selectedRepeatMode: ScheduleRepeatMode,
    onRepeatModeSelect: (ScheduleRepeatMode) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(true)
    ) {
        ScheduleRepeatMode.entries.forEach { repeatMode ->
            LabelledRadioButton(
                labelRes = repeatMode.labelRes,
                selected = selectedRepeatMode == repeatMode,
                onClick = { onRepeatModeSelect(repeatMode) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun PreviewScreenContent() {
    RivoTheme {
        AddEditTransactionScreen(
            isEditMode = false,
            snackbarController = rememberSnackbarController(),
            amountInput = { "" },
            noteInput = { "" },
            tagNameInput = { "" },
            tagColorInput = { Color.Black },
            tagExclusionInput = { false },
            folderSearchQuery = { "" },
            folderList = flowOf(PagingData.empty<Folder>()).collectAsLazyPagingItems(),
            state = AddEditTransactionState(
                isScheduleTxMode = true,
                selectedRepeatMode = ScheduleRepeatMode.MONTHLY
            ),
            actions = object : AddEditTransactionActions {
                override fun onAmountChange(value: String) {}
                override fun onNoteInputFocused() {}
                override fun onNoteChange(value: String) {}
                override fun onRecommendedAmountClick(amount: Long) {}
                override fun onTagClick(tagId: Long) {}
                override fun onTransactionTimestampClick() {}
                override fun onTransactionTimestampSelectionDismiss() {}
                override fun onTransactionTimestampSelectionConfirm(millis: Long) {}
                override fun onTransactionTypeChange(type: TransactionType) {}
                override fun onTransactionExclusionToggle(excluded: Boolean) {}
                override fun onTransformAmountClick() {}
                override fun onTransformAmountDismiss() {}
                override fun onAmountTransformationSelect(criteria: AmountTransformation) {}
                override fun onAmountTransformationConfirm(value: String) {}
                override fun onDeleteClick() {}
                override fun onDeleteDismiss() {}
                override fun onDeleteConfirm() {}
                override fun onNewTagClick() {}
                override fun onNewTagNameChange(value: String) {}
                override fun onNewTagColorSelect(color: Color) {}
                override fun onNewTagExclusionChange(excluded: Boolean) {}
                override fun onNewTagInputDismiss() {}
                override fun onNewTagInputConfirm() {}
                override fun onAddToFolderClick() {}
                override fun onRemoveFromFolderClick() {}
                override fun onFolderSearchQueryChange(query: String) {}
                override fun onFolderSelectionDismiss() {}
                override fun onFolderSelect(folder: Folder) {}
                override fun onCreateFolderClick() {}
                override fun onAddEditOptionSelect(option: AddEditTxOption) {}
                override fun onCancelSchedulingClick() {}
                override fun onRepeatModeClick() {}
                override fun onRepeatModeDismiss() {}
                override fun onRepeatModeSelect(repeatMode: ScheduleRepeatMode) {}
                override fun onBackNav() {}
            }
        )
    }
}