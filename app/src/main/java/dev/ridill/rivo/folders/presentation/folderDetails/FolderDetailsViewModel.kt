package dev.ridill.rivo.folders.presentation.folderDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.navigation.destinations.FolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.folders.domain.model.AggregateType
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: FolderDetailsRepository,
    settingsRepo: SettingsRepository,
    private val eventBus: EventBus<FolderDetailsEvent>
) : ViewModel(), FolderDetailsActions {

    private val folderIdArg = FolderDetailsScreenSpec
        .getFolderIdArgFromSavedStateHandle(savedStateHandle)

    private val folderIdFlow = MutableStateFlow(folderIdArg)
    private val isNewFolder = folderIdFlow.map {
        FolderDetailsScreenSpec
            .isIdInvalid(it)
    }.asStateFlow(viewModelScope, true)

    private val folderDetails = folderIdFlow.flatMapLatest {
        repo.getFolderDetailsById(it)
    }

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    val folderNameInput = savedStateHandle.getStateFlow(FOLDER_NAME_INPUT, "")
    private val folderCreatedTimestamp = folderDetails.map {
        it?.createdTimestamp ?: DateUtil.now()
    }.distinctUntilChanged()
    private val isFolderExcluded = savedStateHandle.getStateFlow(IS_FOLDER_EXCLUDED, false)

    private val currency = settingsRepo.getCurrencyPreference()

    private val aggregateAmount = folderDetails.map { it?.aggregateAmount.orZero() }
        .distinctUntilChanged()

    private val aggregateType = folderDetails.map { it?.aggregateType ?: AggregateType.BALANCED }
        .distinctUntilChanged()

    private val editModeActive = savedStateHandle.getStateFlow(EDIT_MODE_ACTIVE, false)

    val transactionsList = folderIdFlow.flatMapLatest {
        repo.getPagedTransactionsInFolder(it)
    }.cachedIn(viewModelScope)

    val state = combineTuple(
        folderIdFlow,
        isNewFolder,
        editModeActive,
        showDeleteConfirmation,
        folderCreatedTimestamp,
        isFolderExcluded,
        currency,
        aggregateAmount,
        aggregateType,
    ).map { (
                folderId,
                isNewFolder,
                editModeActive,
                showDeleteConfirmation,
                folderCreatedTimestamp,
                isFolderExcluded,
                currency,
                aggregateAmount,
                aggregateType,
            ) ->
        FolderDetailsState(
            folderId = folderId,
            isNewFolder = isNewFolder,
            editModeActive = editModeActive,
            showDeleteConfirmation = showDeleteConfirmation,
            createdTimestamp = folderCreatedTimestamp,
            isExcluded = isFolderExcluded,
            currency = currency,
            aggregateAmount = aggregateAmount,
            aggregateType = aggregateType
        )
    }.asStateFlow(viewModelScope, FolderDetailsState())

    val events = eventBus.eventFlow

    init {
        onInit()
        collectFolderDetails()
    }

    private fun onInit() {
        savedStateHandle[EDIT_MODE_ACTIVE] = FolderDetailsScreenSpec
            .isIdInvalid(folderIdArg)
    }

    private fun collectFolderDetails() = viewModelScope.launch {
        folderDetails.collectLatest { updateInputsFromFolderDetails(it) }
    }

    private fun updateInputsFromFolderDetails(details: FolderDetails?) {
        savedStateHandle[FOLDER_NAME_INPUT] = details?.name.orEmpty()
        savedStateHandle[IS_FOLDER_EXCLUDED] = details?.excluded.orFalse()
    }

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteFolderOnlyClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderById(id)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onDeleteFolderAndTransactionsClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderWithTransactions(id)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onEditClick() {
        savedStateHandle[EDIT_MODE_ACTIVE] = true
    }

    override fun onEditDismiss() {
        viewModelScope.launch {
            if (isNewFolder.value) {
                eventBus.send(FolderDetailsEvent.NavigateUp)
                return@launch
            }
            val folderDetails = folderDetails.first()
            updateInputsFromFolderDetails(folderDetails)
            savedStateHandle[EDIT_MODE_ACTIVE] = false
        }
    }

    override fun onEditConfirm() {
        viewModelScope.launch {
            val name = folderNameInput.value.trim()
            if (name.isEmpty()) {
                eventBus.send(
                    FolderDetailsEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_transaction_folder_name,
                            true
                        )
                    )
                )
                return@launch
            }
            val createdTimestamp = folderCreatedTimestamp.first()
            val excluded = isFolderExcluded.value
            val id = folderIdFlow.value.coerceAtLeast(RivoDatabase.DEFAULT_ID_LONG)
            val insertedId = repo.saveFolder(
                id = id,
                name = name,
                createdTimestamp = createdTimestamp,
                excluded = excluded
            )
            val isUpdate = FolderDetailsScreenSpec.isIdInvalid(insertedId)
            if (!isUpdate) {
                folderIdFlow.update { insertedId }
            }
            val txIdsListToAdd = FolderDetailsScreenSpec
                .getTxIdsArgFromSavedStateHandle(savedStateHandle)
            if (txIdsListToAdd.isNotEmpty()) {
                repo.addTransactionsToFolderByIds(
                    folderId = insertedId,
                    transactionIds = txIdsListToAdd
                )
            }
            savedStateHandle[EDIT_MODE_ACTIVE] = false
            val shouldNavigateUp = FolderDetailsScreenSpec.isIdInvalid(folderIdArg)
                    && FolderDetailsScreenSpec.getExitAfterCreateArg(savedStateHandle)

            if (shouldNavigateUp) {
                eventBus.send(FolderDetailsEvent.NavigateUpWithFolderId(insertedId))
            } else {
                eventBus.send(
                    FolderDetailsEvent.ShowUiMessage(
                        UiText.StringResource(
                            if (isUpdate) R.string.transaction_folder_updated
                            else R.string.transaction_folder_created
                        )
                    )
                )
            }
        }
    }

    override fun onNameChange(value: String) {
        savedStateHandle[FOLDER_NAME_INPUT] = value
    }

    override fun onExclusionToggle(excluded: Boolean) {
        savedStateHandle[IS_FOLDER_EXCLUDED] = excluded
    }

    override fun onTransactionSwipeToDismiss(transaction: TransactionListItem) {
        viewModelScope.launch {
            repo.removeTransactionFromFolderById(transaction.id)
            eventBus.send(FolderDetailsEvent.TransactionRemovedFromGroup(transaction))
        }
    }

    fun onRemoveTransactionUndo(transaction: TransactionListItem) = viewModelScope.launch {
        repo.addTransactionToFolder(transaction)
    }

    sealed class FolderDetailsEvent {
        data object NavigateUp : FolderDetailsEvent()
        data class ShowUiMessage(val uiText: UiText) : FolderDetailsEvent()
        data object FolderDeleted : FolderDetailsEvent()
        data class NavigateUpWithFolderId(val folderId: Long) : FolderDetailsEvent()
        data class TransactionRemovedFromGroup(val transaction: TransactionListItem) :
            FolderDetailsEvent()
    }
}

private const val EDIT_MODE_ACTIVE = "EDIT_MODE_ACTIVE"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val FOLDER_NAME_INPUT = "FOLDER_NAME_INPUT"
private const val IS_FOLDER_EXCLUDED = "IS_FOLDER_EXCLUDED"

const val RESULT_FOLDER_DELETED = "RESULT_FOLDER_DELETED"