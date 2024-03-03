package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.service.ExpEvalService
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.ifInfinite
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTransactionScreenSpec
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionInput
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepo: AddEditTransactionRepository,
    private val tagsRepo: TagsRepository,
    private val foldersListRepo: FoldersListRepository,
    private val eventBus: EventBus<AddEditTransactionEvent>,
    private val evalService: ExpEvalService
) : ViewModel(), AddEditTransactionActions {
    private val transactionIdArg = AddEditTransactionScreenSpec
        .getTransactionIdFromSavedStateHandle(savedStateHandle)

    private val linkFolderIdArg = AddEditTransactionScreenSpec
        .getFolderIdToLinkFromSavedStateHandle(savedStateHandle)

    private val isLoading = MutableStateFlow(false)

    private val currentTransactionId: Long
        get() = transactionIdArg.coerceAtLeast(RivoDatabase.DEFAULT_ID_LONG)

    private val txInput = savedStateHandle.getStateFlow(TX_INPUT, TransactionInput.DEFAULT)
    val amountInput = txInput.map { it.amount }
        .asStateFlow(viewModelScope, String.Empty)

    private val amountTransformation = savedStateHandle
        .getStateFlow(SELECTED_AMOUNT_TRANSFORMATION, AmountTransformation.DIVIDE_BY)
    private val showAmountTransformationInput = savedStateHandle
        .getStateFlow(SHOW_AMOUNT_TRANSFORMATION_INPUT, false)

    val noteInput = txInput.map { it.note }

    private val tagsList = tagsRepo.getAllTags()
    private val selectedTagId = txInput.map { it.tagId }
        .distinctUntilChanged()

    private val transactionTimestamp = txInput.map { it.timestamp }
        .distinctUntilChanged()

    private val transactionFolderId = txInput.map { it.folderId }
        .distinctUntilChanged()

    private val transactionType = txInput.map { it.type }
        .distinctUntilChanged()

    private val isTransactionExcluded = txInput.map { it.excluded }
        .distinctUntilChanged()

    private val currency = transactionTimestamp.flatMapLatest {
        transactionRepo.getCurrencyPreference(it)
    }.distinctUntilChanged()

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = transactionRepo.getAmountRecommendations()

    private val showNewTagInput = savedStateHandle
        .getStateFlow(SHOW_NEW_TAG_INPUT, false)
    val tagInput = savedStateHandle
        .getStateFlow<Tag?>(TAG_INPUT, null)
    private val newTagError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    private val showDateTimePicker = savedStateHandle.getStateFlow(SHOW_DATE_TIME_PICKER, false)

    private val showFolderSelection = savedStateHandle.getStateFlow(SHOW_FOLDER_SELECTION, false)

    val folderSearchQuery = savedStateHandle.getStateFlow(FOLDER_SEARCH_QUERY, "")
    val foldersList = folderSearchQuery
        .flatMapLatest { query ->
            foldersListRepo.getFoldersList(query)
        }.cachedIn(viewModelScope)

    private val linkedFolderName = transactionFolderId.map { selectedId ->
        selectedId?.let { foldersListRepo.getFolderById(it) }
    }.map { it?.name }

    val state = combineTuple(
        isLoading,
        currency,
        amountRecommendations,
        amountTransformation,
        showAmountTransformationInput,
        tagsList,
        selectedTagId,
        transactionTimestamp,
        transactionType,
        isTransactionExcluded,
        showDeleteConfirmation,
        showNewTagInput,
        newTagError,
        showDateTimePicker,
        showFolderSelection,
        linkedFolderName
    ).map { (
                isLoading,
                currency,
                amountRecommendations,
                amountTransformation,
                showAmountTransformationInput,
                tagsList,
                selectedTagId,
                transactionTimestamp,
                transactionType,
                isTransactionExcluded,
                showDeleteConfirmation,
                showNewTagInput,
                newTagError,
                showDateTimePicker,
                showFolderSelection,
                linkedFolderName
            ) ->
        AddEditTransactionState(
            isLoading = isLoading,
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
            transactionType = transactionType,
            showFolderSelection = showFolderSelection,
            linkedFolderName = linkedFolderName,
            showTransformationInput = showAmountTransformationInput,
            selectedAmountTransformation = amountTransformation
        )
    }.asStateFlow(viewModelScope, AddEditTransactionState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        val transactionInput = transactionRepo.getTransactionById(transactionIdArg)
            ?: TransactionInput.DEFAULT
        savedStateHandle[TX_INPUT] = transactionInput.copy(
            folderId = linkFolderIdArg ?: transactionInput.folderId
        )
    }

    override fun onAmountChange(value: String) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(amount = value)
    }

    override fun onNoteInputFocused() {
        val amountInput = amountInput.value
            .trim()
            .ifEmpty { return }

        val isExpression = evalService.isExpression(amountInput)
        val result = if (isExpression) evalService.evalOrNull(amountInput)
        else TextFormat.parseNumber(amountInput)
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            amount = TextFormat.number(
                value = result.orZero(),
                isGroupingUsed = false
            )
        )
    }

    override fun onNoteChange(value: String) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(note = value)
    }

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            amount = TextFormat.number(
                value = amount,
                isGroupingUsed = false
            )
        )
    }

    override fun onTagClick(tagId: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            tagId = tagId.takeIf { it != txInput.value.tagId }
        )
    }

    override fun onTransactionTimestampClick() {
        savedStateHandle[SHOW_DATE_TIME_PICKER] = true
    }

    override fun onTransactionTimestampSelectionDismiss() {
        savedStateHandle[SHOW_DATE_TIME_PICKER] = false
    }

    override fun onTransactionTimestampSelectionConfirm(millis: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            timestamp = DateUtil.dateFromMillisWithTime(millis)
        )
        savedStateHandle[SHOW_DATE_TIME_PICKER] = false
    }

    override fun onTransactionTypeChange(type: TransactionType) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            type = type
        )
    }

    override fun onTransactionExclusionToggle(excluded: Boolean) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            excluded = excluded
        )
    }

    override fun onTransformAmountClick() {
        savedStateHandle[SHOW_AMOUNT_TRANSFORMATION_INPUT] = true
    }

    override fun onTransformAmountDismiss() {
        savedStateHandle[SHOW_AMOUNT_TRANSFORMATION_INPUT] = false
    }

    override fun onAmountTransformationSelect(criteria: AmountTransformation) {
        savedStateHandle[SELECTED_AMOUNT_TRANSFORMATION] = criteria
    }

    override fun onAmountTransformationConfirm(value: String) {
        val amount = amountInput.value.toDoubleOrNull() ?: return
        val transformedAmount = when (amountTransformation.value) {
            AmountTransformation.DIVIDE_BY -> amount / value.toDoubleOrNull().orZero()
            AmountTransformation.MULTIPLIER -> amount * value.toDoubleOrNull().orZero()
            AmountTransformation.PERCENT -> amount * (value.toFloatOrNull().orZero() / 100f)
        }
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            amount = TextFormat.number(
                value = transformedAmount.ifInfinite { Double.Zero },
                isGroupingUsed = false
            )
        )
        savedStateHandle[SHOW_AMOUNT_TRANSFORMATION_INPUT] = false
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
        savedStateHandle[TAG_INPUT] = Tag.NEW
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
            savedStateHandle[TX_INPUT] = txInput.value.copy(
                tagId = insertedId
            )
            eventBus.send(
                AddEditTransactionEvent.ShowUiMessage(UiText.StringResource(R.string.tag_saved))
            )
        }
    }

    override fun onAddToFolderClick() {
        savedStateHandle[FOLDER_SEARCH_QUERY] = String.Empty
        savedStateHandle[SHOW_FOLDER_SELECTION] = true
    }

    override fun onRemoveFromFolderClick() {
        savedStateHandle[TRANSACTION_FOLDER_ID] = null
    }

    override fun onFolderSearchQueryChange(query: String) {
        savedStateHandle[FOLDER_SEARCH_QUERY] = query
    }

    override fun onFolderSelectionDismiss() {
        savedStateHandle[SHOW_FOLDER_SELECTION] = false
    }

    override fun onFolderSelect(folder: Folder) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            folderId = folder.id
        )
        savedStateHandle[SHOW_FOLDER_SELECTION] = false
    }

    override fun onCreateFolderClick() {
        viewModelScope.launch {
            savedStateHandle[SHOW_FOLDER_SELECTION] = false
            eventBus.send(AddEditTransactionEvent.NavigateToFolderDetailsForCreation)
        }
    }

    fun onCreateFolderResult(folderIdString: String?) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            folderId = folderIdString?.toLongOrNull()
        )
    }

    private fun clearAndHideTagInput() {
        savedStateHandle[SHOW_NEW_TAG_INPUT] = false
        savedStateHandle[TAG_INPUT] = null
    }

    override fun onBackNav() {
        isLoading.update { true }
        viewModelScope.launch {
            val txInput = txInput.value
            val amountInput = txInput.amount.trim()
            if (amountInput.isEmpty()) {
                eventBus.send(AddEditTransactionEvent.NavigateUp)
                return@launch
            }
            val isExp = evalService.isExpression(amountInput)
            val evaluatedAmount = (if (isExp) evalService.evalOrNull(amountInput)
            else TextFormat.parseNumber(amountInput)) ?: -1.0
            if (evaluatedAmount < Double.Zero) {
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
            transactionRepo.saveTransaction(
                transaction = txInput.copy(
                    amount = evaluatedAmount.toString()
                )
            )
            isLoading.update { false }
            eventBus.send(AddEditTransactionEvent.NavigateUp)
        }
    }

    sealed class AddEditTransactionEvent {
        data object TransactionDeleted : AddEditTransactionEvent()
        data object NavigateUp : AddEditTransactionEvent()
        data class ShowUiMessage(val uiText: UiText) : AddEditTransactionEvent()
        data object NavigateToFolderDetailsForCreation : AddEditTransactionEvent()
    }
}

private const val TX_INPUT = "TX_INPUT"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_NEW_TAG_INPUT = "SHOW_NEW_TAG_INPUT"
private const val TAG_INPUT = "TAG_INPUT"
private const val SHOW_DATE_TIME_PICKER = "SHOW_DATE_TIME_PICKER"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"
private const val TRANSACTION_FOLDER_ID = "TRANSACTION_FOLDER_ID"
private const val SHOW_FOLDER_SELECTION = "SHOW_FOLDER_SELECTION"
private const val FOLDER_SEARCH_QUERY = "FOLDER_SEARCH_QUERY"
private const val SHOW_AMOUNT_TRANSFORMATION_INPUT = "SHOW_AMOUNT_TRANSFORMATION_INPUT"
private const val SELECTED_AMOUNT_TRANSFORMATION = "SELECTED_AMOUNT_TRANSFORMATION"

const val RESULT_TRANSACTION_DELETED = "RESULT_TRANSACTION_DELETED"