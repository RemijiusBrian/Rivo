package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.components.AmountVisualTransformation
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.ExcludedIcon
import dev.ridill.rivo.core.ui.components.LabelledRadioButton
import dev.ridill.rivo.core.ui.components.MinWidthOutlinedTextField
import dev.ridill.rivo.core.ui.components.RivoDatePickerDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.RivoTimePickerDialog
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.AllSchedulesScreenSpec
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.settings.presentation.components.SimplePreference
import dev.ridill.rivo.settings.presentation.components.SwitchPreference
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.presentation.components.TopTagsSelectorFlowRow
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.AmountRecommendationsRow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Currency

@Composable
fun AddEditTransactionScreen(
    isEditMode: Boolean,
    appCurrencyPreference: Currency,
    snackbarController: SnackbarController,
    amountInput: () -> String,
    noteInput: () -> String,
    topTagsLazyPagingItems: LazyPagingItems<Tag>,
    state: AddEditTransactionState,
    actions: AddEditTransactionActions,
    navigateUp: () -> Unit,
    navigateToAmountTransformationSelection: () -> Unit,
) {
    val amountFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditMode) {
        if (!isEditMode)
            amountFocusRequester.requestFocus()
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val dateNowUtc = remember { DateUtil.dateNow(ZoneId.of(ZoneOffset.UTC.id)) }
    val datePickerState = if (state.isScheduleTxMode) rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.timestampUtc),
        yearRange = IntRange(dateNowUtc.year, DatePickerDefaults.YearRange.last),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= DateUtil.toMillis(
                    date = dateNowUtc.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    else rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.timestampUtc),
        yearRange = IntRange(DatePickerDefaults.YearRange.first, dateNowUtc.year),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis < DateUtil.toMillis(
                    date = dateNowUtc.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.timestamp.hour,
        initialMinute = state.timestamp.minute
    )

    RivoScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isEditMode) {
                                if (state.isScheduleTxMode) R.string.edit_schedule
                                else R.string.destination_edit_transaction
                            } else {
                                if (state.isScheduleTxMode) R.string.new_schedule
                                else R.string.destination_new_transaction
                            }
                        )
                    )
                },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    AnimatedVisibility(!state.isScheduleTxMode) {
                        IconButton(onClick = actions::onScheduleForLaterClick) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_time_forward),
                                contentDescription = stringResource(R.string.cd_schedule_transaction_for_later)
                            )
                        }
                    }

                    if (isEditMode) {
                        IconButton(onClick = actions::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_transaction)
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
                    imageVector = Icons.Rounded.Save,
                    contentDescription = stringResource(
                        id = if (state.isScheduleTxMode) R.string.cd_save_schedule
                        else R.string.cd_save_transaction
                    )
                )
            }
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
                    .padding(
                        top = MaterialTheme.spacing.medium,
                        bottom = PaddingScrollEnd
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                TransactionTypeSelector(
                    selectedType = state.transactionType,
                    onValueChange = actions::onTypeChange,
                    modifier = Modifier
                        .fillMaxWidth(TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION)
                        .align(Alignment.CenterHorizontally)
                )

                AmountInput(
                    currency = appCurrencyPreference,
                    amount = amountInput,
                    onAmountChange = actions::onAmountChange,
                    onTransformClick = navigateToAmountTransformationSelection,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .focusRequester(amountFocusRequester)
                )

                NoteInput(
                    input = noteInput,
                    onValueChange = actions::onNoteChange,
                    onFocused = actions::onNoteInputFocused,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                if (!isEditMode) {
                    AmountRecommendationsRow(
                        currency = appCurrencyPreference,
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

                TransactionTimestamp(
                    timestamp = state.timestamp,
                    onClick = actions::onTimestampClick,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .align(Alignment.End)
                )

                FolderIndicator(
                    folderName = state.linkedFolderName,
                    onSelectFolderClick = actions::onSelectFolderClick,
                    modifier = Modifier
                        .align(Alignment.Start)
                )

                AnimatedVisibility(
                    visible = state.isScheduleTxMode,
                    modifier = Modifier
                        .align(Alignment.Start)
                ) {
                    TransactionRepeatModeIndicator(
                        selectedRepeatMode = state.selectedRepeatMode,
                        onClick = actions::onRepeatModeClick,
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )
                }

                TagSelection(
                    tagsLazyPagingItems = topTagsLazyPagingItems,
                    selectedTagId = state.selectedTagId,
                    onTagClick = actions::onTagSelect,
                    onViewAllClick = actions::onViewAllTagsClick,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                AnimatedVisibility(
                    visible = !state.isScheduleTxMode,
                    modifier = Modifier
                ) {
                    SwitchPreference(
                        titleRes = R.string.exclude_from_expenditure,
                        value = state.isTransactionExcluded,
                        onValueChange = actions::onExclusionToggle,
                        leadingIcon = { ExcludedIcon() }
                    )
                }
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

        if (state.showDatePicker) {
            RivoDatePickerDialog(
                onDismiss = actions::onDateSelectionDismiss,
                onConfirm = actions::onDateSelectionConfirm,
                onPickTimeClick = actions::onPickTimeClick,
                state = datePickerState
            )
        }

        if (state.showTimePicker) {
            RivoTimePickerDialog(
                onDismiss = actions::onTimeSelectionDismiss,
                onConfirm = actions::onTimeSelectionConfirm,
                onPickDateClick = actions::onPickDateClick,
                state = timePickerState
            )
        }

        if (state.showRepeatModeSelection) {
            RepeatModeSelectionSheet(
                onDismiss = actions::onRepeatModeDismiss,
                selectedRepeatMode = state.selectedRepeatMode,
                onRepeatModeSelect = actions::onRepeatModeSelect,
                onCancelClick = actions::onCancelSchedulingClick
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
            imeAction = ImeAction.Default
        ),
        singleLine = false,
        maxLines = NOTE_MAX_LINES,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}

private const val NOTE_MAX_LINES = 5

private const val TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION = 0.80f
private const val AMOUNT_RECOMMENDATION_WIDTH_FRACTION = 0.80f

@Composable
private fun TransactionTimestamp(
    timestamp: LocalDateTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.timestamp_label),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = timestamp.format(DateUtil.Formatters.localizedDateMediumTimeShort),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilledTonalIconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.CalendarClock,
                contentDescription = stringResource(R.string.cd_tap_to_pick_timestamp)
            )
        }
    }
}

@Composable
private fun FolderIndicator(
    folderName: String?,
    onSelectFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SimplePreference(
        title = folderName ?: stringResource(R.string.transaction_folder_indicator_label),
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outline_move_to_folder),
                contentDescription = stringResource(R.string.cd_add_transaction_to_folder)
            )
        },
        onClick = onSelectFolderClick,
        modifier = modifier
            .fillMaxWidth(),
        summary = stringResource(R.string.tap_to_select_folder)
    )
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
                    },
                label = { Text(stringResource(type.labelRes)) }
            )
        }
    }
}

@Composable
private fun TagSelection(
    tagsLazyPagingItems: LazyPagingItems<Tag>,
    selectedTagId: Long?,
    onTagClick: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        BodyMediumText(stringResource(R.string.tag_your_transaction))
        TopTagsSelectorFlowRow(
            topTagsLazyPagingItems = tagsLazyPagingItems,
            selectedTagId = selectedTagId,
            onTagClick = onTagClick,
            onViewAllClick = onViewAllClick,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun TransactionRepeatModeIndicator(
    selectedRepeatMode: ScheduleRepeatMode,
    onClick: () -> Unit,
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
                stringResource(AllSchedulesScreenSpec.labelRes)
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
    onCancelClick: () -> Unit,
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
        Button(
            onClick = onCancelClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.small)
        ) {
            Text(text = stringResource(R.string.cancel_scheduling))
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
            topTagsLazyPagingItems = flowOf(PagingData.empty<Tag>()).collectAsLazyPagingItems(),
            state = AddEditTransactionState(
                isScheduleTxMode = true,
                selectedRepeatMode = ScheduleRepeatMode.MONTHLY
            ),
            actions = object : AddEditTransactionActions {
                override fun onAmountChange(value: String) {}
                override fun onNoteInputFocused() {}
                override fun onNoteChange(value: String) {}
                override fun onRecommendedAmountClick(amount: Long) {}
                override fun onTagSelect(tagId: Long) {}
                override fun onViewAllTagsClick() {}
                override fun onTimestampClick() {}
                override fun onDateSelectionDismiss() {}
                override fun onPickTimeClick() {}
                override fun onPickDateClick() {}
                override fun onDateSelectionConfirm(millis: Long) {}
                override fun onTimeSelectionDismiss() {}
                override fun onTimeSelectionConfirm(hour: Int, minute: Int) {}
                override fun onTypeChange(type: TransactionType) {}
                override fun onExclusionToggle(excluded: Boolean) {}
                override fun onDeleteClick() {}
                override fun onDeleteDismiss() {}
                override fun onDeleteConfirm() {}
                override fun onSelectFolderClick() {}
                override fun onScheduleForLaterClick() {}
                override fun onCancelSchedulingClick() {}
                override fun onRepeatModeClick() {}
                override fun onRepeatModeDismiss() {}
                override fun onRepeatModeSelect(repeatMode: ScheduleRepeatMode) {}
                override fun onSaveClick() {}
            },
            navigateUp = {},
            appCurrencyPreference = LocaleUtil.defaultCurrency,
            navigateToAmountTransformationSelection = {}
        )
    }
}