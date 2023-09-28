package dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.navigation.destinations.TransactionFolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.repository.FolderDetailsRepository
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
class TxFolderDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: FolderDetailsRepository,
    settingsRepo: SettingsRepository,
    private val eventBus: EventBus<TxFolderDetailsEvent>
) : ViewModel(), TxFolderDetailsActions {

    private val folderIdArg = TransactionFolderDetailsScreenSpec
        .getFolderIdArgFromSavedStateHandle(savedStateHandle)

    private val folderIdFlow = MutableStateFlow(folderIdArg)
    private val isNewFolder = folderIdFlow.map {
        !TransactionFolderDetailsScreenSpec
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

    private val aggregateType = folderDetails.map { it?.aggregateType }
        .distinctUntilChanged()

    private val editModeActive = savedStateHandle.getStateFlow(EDIT_MODE_ACTIVE, false)

    private val transactions = folderIdFlow.flatMapLatest {
        repo.getTransactionsInFolder(it)
    }

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
        transactions
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
                transactions
            ) ->
        TxFolderDetailsState(
            folderId = folderId,
            isNewFolder = isNewFolder,
            editModeActive = editModeActive,
            showDeleteConfirmation = showDeleteConfirmation,
            createdTimestamp = folderCreatedTimestamp,
            isExcluded = isFolderExcluded,
            currency = currency,
            aggregateAmount = aggregateAmount,
            aggregateType = aggregateType,
            transactions = transactions
        )
    }.asStateFlow(viewModelScope, TxFolderDetailsState())

    val events = eventBus.eventFlow

    init {
        onInit()
        collectFolderDetails()
    }

    private fun onInit() {
        savedStateHandle[EDIT_MODE_ACTIVE] = TransactionFolderDetailsScreenSpec
            .isIdInvalid(folderIdArg)
    }

    private fun collectFolderDetails() = viewModelScope.launch {
        folderDetails.collectLatest { updateInputsFromFolderDetails(it) }
    }

    private fun updateInputsFromFolderDetails(details: TransactionFolderDetails?) {
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
            eventBus.send(TxFolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onDeleteFolderAndTransactionsClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderWithTransactions(id)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(TxFolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onEditClick() {
        savedStateHandle[EDIT_MODE_ACTIVE] = true
    }

    override fun onEditDismiss() {
        viewModelScope.launch {
            if (isNewFolder.value) {
                eventBus.send(TxFolderDetailsEvent.NavigateUp)
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
                    TxFolderDetailsEvent.ShowUiMessage(
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
            folderIdFlow.update { insertedId }
            if (isNewFolder.value) {
                val txIdsListToAdd = TransactionFolderDetailsScreenSpec
                    .getTxIdsArgFromSavedStateHandle(savedStateHandle)
                repo.addTransactionsToFolderByIds(
                    folderId = insertedId,
                    transactionIds = txIdsListToAdd
                )
            }
            savedStateHandle[EDIT_MODE_ACTIVE] = false
            val shouldNavigateUp = TransactionFolderDetailsScreenSpec.isIdInvalid(folderIdArg)
                    && TransactionFolderDetailsScreenSpec.getExitAfterCreateArg(savedStateHandle)

            if (shouldNavigateUp) {
                eventBus.send(TxFolderDetailsEvent.NavigateUpWithFolderId(insertedId))
            } else {
                eventBus.send(
                    TxFolderDetailsEvent.ShowUiMessage(
                        UiText.StringResource(
                            if (isNewFolder.value) R.string.transaction_folder_created
                            else R.string.transaction_folder_updated
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

    sealed class TxFolderDetailsEvent {
        object NavigateUp : TxFolderDetailsEvent()
        data class ShowUiMessage(val uiText: UiText) : TxFolderDetailsEvent()
        object FolderDeleted : TxFolderDetailsEvent()
        data class NavigateUpWithFolderId(val folderId: Long) : TxFolderDetailsEvent()
    }
}

private const val EDIT_MODE_ACTIVE = "EDIT_MODE_ACTIVE"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val FOLDER_NAME_INPUT = "FOLDER_NAME_INPUT"
private const val IS_FOLDER_EXCLUDED = "IS_FOLDER_EXCLUDED"

const val RESULT_FOLDER_DELETED = "RESULT_FOLDER_DELETED"