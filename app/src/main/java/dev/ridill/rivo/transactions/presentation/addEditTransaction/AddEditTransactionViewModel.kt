package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.service.ExpEvalService
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTransactionScreenSpec
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepo: AddEditTransactionRepository,
    private val tagsRepo: TagsRepository,
    settingsRepo: SettingsRepository,
    private val eventBus: EventBus<AddEditTransactionEvent>,
    private val evalService: ExpEvalService
) : ViewModel(), AddEditTransactionActions {
    private val transactionIdArg = AddEditTransactionScreenSpec
        .getTransactionIdFromSavedStateHandle(savedStateHandle)
    private val isEditMode = AddEditTransactionScreenSpec.isEditMode(transactionIdArg)

    private val linkGroupIdArg = AddEditTransactionScreenSpec
        .getLinkGroupIdFromSavedStateHandle(savedStateHandle)

    private val currentTransactionId: Long
        get() = transactionIdArg.coerceAtLeast(RivoDatabase.DEFAULT_ID_LONG)

    private val currency = settingsRepo.getCurrencyPreference()

    val amountInput = savedStateHandle.getStateFlow(AMOUNT_INPUT, "")
    val noteInput = savedStateHandle.getStateFlow(NOTE_INPUT, "")

    private val tagsList = tagsRepo.getAllTags()
    private val selectedTagId = savedStateHandle.getStateFlow<Long?>(SELECTED_TAG_ID, null)

    private val transactionTimestamp = savedStateHandle
        .getStateFlow(TRANSACTION_TIMESTAMP, DateUtil.now())

    private val transactionGroupId = savedStateHandle
        .getStateFlow<Long?>(TRANSACTION_GROUP_ID, null)

    private val transactionType = savedStateHandle
        .getStateFlow(TRANSACTION_TYPE, TransactionType.DEBIT)

    private val isTransactionExcluded = savedStateHandle
        .getStateFlow(IS_TRANSACTION_EXCLUDED, false)

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = transactionRepo.getAmountRecommendations()

    private val showNewTagInput = savedStateHandle
        .getStateFlow(SHOW_NEW_TAG_INPUT, false)
    val tagInput = savedStateHandle
        .getStateFlow<TransactionTag?>(TAG_INPUT, null)
    private val newTagError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    private val showDateTimePicker = savedStateHandle.getStateFlow(SHOW_DATE_TIME_PICKER, false)

    val state = combineTuple(
        currency,
        amountRecommendations,
        tagsList,
        selectedTagId,
        transactionTimestamp,
        transactionGroupId,
        transactionType,
        isTransactionExcluded,
        showDeleteConfirmation,
        showNewTagInput,
        newTagError,
        showDateTimePicker
    ).map { (
                currency,
                amountRecommendations,
                tagsList,
                selectedTagId,
                transactionTimestamp,
                transactionGroupId,
                transactionType,
                isTransactionExcluded,
                showDeleteConfirmation,
                showNewTagInput,
                newTagError,
                showDateTimePicker
            ) ->
        AddEditTransactionState(
            currency = currency,
            amountRecommendations = amountRecommendations,
            tagsList = tagsList,
            selectedTagId = selectedTagId,
            transactionTimestamp = transactionTimestamp,
            isTransactionExcluded = isTransactionExcluded,
            showDeleteConfirmation = showDeleteConfirmation,
            showNewTagInput = showNewTagInput,
            newTagError = newTagError,
            showDateTimePicker = showDateTimePicker,
            transactionGroupId = transactionGroupId,
            transactionType = transactionType
        )
    }.asStateFlow(viewModelScope, AddEditTransactionState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        val transaction = transactionRepo.getTransactionById(transactionIdArg)
            ?: Transaction.DEFAULT
        savedStateHandle[AMOUNT_INPUT] = transaction.amount
        savedStateHandle[NOTE_INPUT] = transaction.note
        savedStateHandle[TRANSACTION_TIMESTAMP] = transaction.createdTimestamp
        savedStateHandle[TRANSACTION_TYPE] = transaction.type
        savedStateHandle[SELECTED_TAG_ID] = transaction.tagId
        savedStateHandle[IS_TRANSACTION_EXCLUDED] = transaction.excluded
        savedStateHandle[TRANSACTION_GROUP_ID] = linkGroupIdArg
            ?: transaction.groupId
    }

    override fun onAmountChange(value: String) {
        savedStateHandle[AMOUNT_INPUT] = value
    }

    override fun onNoteInputFocused() {
        val amountInput = amountInput.value
            .trim()
            .ifEmpty { return }

        val isExpression = evalService.isExpression(amountInput)
        val result = if (isExpression) evalService.evalOrNull(amountInput)
        else TextFormat.parseNumber(amountInput)
        savedStateHandle[AMOUNT_INPUT] = TextFormat.number(
            value = result.orZero(),
            isGroupingUsed = false
        )
    }

    override fun onNoteChange(value: String) {
        savedStateHandle[NOTE_INPUT] = value
    }

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[AMOUNT_INPUT] = amount.toString()
    }

    override fun onTagClick(tagId: Long) {
        savedStateHandle[SELECTED_TAG_ID] = tagId
            .takeIf { selectedTagId.value != it }
    }

    override fun onTransactionTimestampClick() {
        savedStateHandle[SHOW_DATE_TIME_PICKER] = true
    }

    override fun onTransactionTimestampSelectionDismiss() {
        savedStateHandle[SHOW_DATE_TIME_PICKER] = false
    }

    override fun onTransactionTimestampSelectionConfirm(dateTime: LocalDateTime) {
        savedStateHandle[TRANSACTION_TIMESTAMP] = dateTime
        savedStateHandle[SHOW_DATE_TIME_PICKER] = false
    }

    override fun onTransactionTypeChange(type: TransactionType) {
        savedStateHandle[TRANSACTION_TYPE] = type
    }

    override fun onTransactionExclusionToggle(excluded: Boolean) {
        savedStateHandle[IS_TRANSACTION_EXCLUDED] = excluded
    }

    override fun onSaveClick() {
        viewModelScope.launch {
            val amountInput = amountInput.value.trim()
            val isExp = evalService.isExpression(amountInput)
            val amount = (if (isExp) evalService.evalOrNull(amountInput)
            else TextFormat.parseNumber(amountInput)) ?: -1.0
            if (amount < Double.Zero) {
                eventBus.send(
                    AddEditTransactionEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_amount,
                            true
                        )
                    )
                )
                return@launch
            }
            val note = noteInput.value.trim()
            if (note.isEmpty()) {
                eventBus.send(
                    AddEditTransactionEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_transaction_note,
                            true
                        )
                    )
                )
                return@launch
            }
            val type = transactionType.value
            val tagId = selectedTagId.value
            val groupId = transactionGroupId.value
            val excluded = isTransactionExcluded.value
            transactionRepo.saveTransaction(
                id = currentTransactionId,
                amount = amount,
                note = note,
                dateTime = transactionTimestamp.value,
                transactionType = type,
                tagId = tagId,
                groupId = groupId,
                excluded = excluded
            )
            val event = if (isEditMode) AddEditTransactionEvent.TransactionUpdated
            else AddEditTransactionEvent.TransactionAdded
            eventBus.send(event)
        }
    }

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            transactionRepo.deleteTransaction(currentTransactionId)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(AddEditTransactionEvent.TransactionDeleted)
        }
    }

    override fun onNewTagClick() {
        savedStateHandle[TAG_INPUT] = TransactionTag.NEW
        savedStateHandle[SHOW_NEW_TAG_INPUT] = true
    }

    override fun onNewTagNameChange(value: String) {
        savedStateHandle[TAG_INPUT] = tagInput.value
            ?.copy(name = value)
        savedStateHandle[NEW_TAG_ERROR] = null

    }

    override fun onNewTagColorSelect(color: Color) {
        savedStateHandle[TAG_INPUT] = tagInput.value
            ?.copy(colorCode = color.toArgb())
    }

    override fun onNewTagExclusionChange(excluded: Boolean) {
        savedStateHandle[TAG_INPUT] = tagInput.value
            ?.copy(excluded = excluded)
    }

    override fun onNewTagInputDismiss() {
        clearAndHideTagInput()
    }

    override fun onNewTagInputConfirm() {
        val tagInput = tagInput.value ?: return
        viewModelScope.launch {
            val name = tagInput.name.trim()
            val color = tagInput.color

            if (name.isEmpty()) {
                savedStateHandle[NEW_TAG_ERROR] = UiText.StringResource(
                    R.string.error_invalid_tag_name,
                    true
                )
                return@launch
            }

            val insertedId = tagsRepo.saveTag(
                id = tagInput.id,
                name = name,
                color = color,
                excluded = tagInput.excluded,
                timestamp = DateUtil.now()
            )

            clearAndHideTagInput()
            savedStateHandle[SELECTED_TAG_ID] = insertedId
            eventBus.send(
                AddEditTransactionEvent.ShowUiMessage(UiText.StringResource(R.string.tag_saved))
            )
        }
    }

    private fun clearAndHideTagInput() {
        savedStateHandle[SHOW_NEW_TAG_INPUT] = false
        savedStateHandle[TAG_INPUT] = null
    }

    sealed class AddEditTransactionEvent {
        object TransactionAdded : AddEditTransactionEvent()
        object TransactionUpdated : AddEditTransactionEvent()
        object TransactionDeleted : AddEditTransactionEvent()
        data class ShowUiMessage(val uiText: UiText) : AddEditTransactionEvent()
    }
}

private const val AMOUNT_INPUT = "AMOUNT_INPUT"
private const val NOTE_INPUT = "NOTE_INPUT"
private const val SELECTED_TAG_ID = "SELECTED_TAG_ID"
private const val TRANSACTION_TIMESTAMP = "TRANSACTION_TIMESTAMP"
private const val IS_TRANSACTION_EXCLUDED = "IS_TRANSACTION_EXCLUDED"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_NEW_TAG_INPUT = "SHOW_NEW_TAG_INPUT"
private const val TAG_INPUT = "TAG_INPUT"
private const val SHOW_DATE_TIME_PICKER = "SHOW_DATE_TIME_PICKER"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"
private const val TRANSACTION_GROUP_ID = "TRANSACTION_GROUP_ID"
private const val TRANSACTION_TYPE = "TRANSACTION_TYPE"

const val RESULT_TRANSACTION_ADDED = "RESULT_TRANSACTION_ADDED"
const val RESULT_TRANSACTION_UPDATED = "RESULT_TRANSACTION_UPDATED"
const val RESULT_TRANSACTION_DELETED = "RESULT_TRANSACTION_DELETED"