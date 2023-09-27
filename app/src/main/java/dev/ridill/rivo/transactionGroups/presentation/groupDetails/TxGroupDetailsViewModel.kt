package dev.ridill.rivo.transactionGroups.presentation.groupDetails

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
import dev.ridill.rivo.core.ui.navigation.destinations.ARG_INVALID_ID_LONG
import dev.ridill.rivo.core.ui.navigation.destinations.TransactionGroupDetailsScreenSpec
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupDetails
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupDetailsRepository
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
class TxGroupDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val txGroupRepo: TxGroupDetailsRepository,
    settingsRepo: SettingsRepository,
    private val eventBus: EventBus<TxGroupDetailsEvent>
) : ViewModel(), TxGroupDetailsActions {

    private val groupIdArg = TransactionGroupDetailsScreenSpec
        .getGroupIdArgFromSavedStateHandle(savedStateHandle)

    private val isNewGroup = groupIdArg == ARG_INVALID_ID_LONG

    private val groupIdFlow = MutableStateFlow(groupIdArg)
    private val groupDetails = groupIdFlow.flatMapLatest {
        txGroupRepo.getGroupDetailsFlowById(it)
    }

    val groupNameInput = savedStateHandle.getStateFlow(GROUP_NAME_INPUT, "")
    private val groupCreatedTimestamp = groupDetails.map {
        it?.createdTimestamp ?: DateUtil.now()
    }.distinctUntilChanged()
    private val isGroupExcluded = savedStateHandle.getStateFlow(IS_GROUP_EXCLUDED, false)

    private val currency = settingsRepo.getCurrencyPreference()

    private val aggregateAmount = groupDetails.map { it?.aggregateAmount.orZero() }
        .distinctUntilChanged()

    private val aggregateDirection = groupDetails.map { it?.aggregateType }
        .distinctUntilChanged()

    private val editModeActive = savedStateHandle.getStateFlow(EDIT_MODE_ACTIVE, false)

    private val transactions = groupIdFlow.flatMapLatest {
        txGroupRepo.getTransactionsForGroup(it)
    }

    val state = combineTuple(
        groupIdFlow,
        editModeActive,
        groupCreatedTimestamp,
        isGroupExcluded,
        currency,
        aggregateAmount,
        aggregateDirection,
        transactions
    ).map { (
                groupId,
                editModeActive,
                groupCreatedTimestamp,
                isGroupExcluded,
                currency,
                aggregateAmount,
                aggregateDirection,
                transactions
            ) ->
        TxGroupDetailsState(
            groupId = groupId,
            editModeActive = editModeActive,
            createdTimestamp = groupCreatedTimestamp,
            isExcluded = isGroupExcluded,
            currency = currency,
            aggregateAmount = aggregateAmount,
            aggregateDirection = aggregateDirection,
            transactions = transactions
        )
    }.asStateFlow(viewModelScope, TxGroupDetailsState())

    val events = eventBus.eventFlow

    init {
        onInit()
        collectGroupDetails()
    }

    private fun onInit() {
        savedStateHandle[EDIT_MODE_ACTIVE] = isNewGroup
    }

    private fun collectGroupDetails() = viewModelScope.launch {
        groupDetails.collectLatest { updateInputsFromGroupDetails(it) }
    }

    private fun updateInputsFromGroupDetails(details: TxGroupDetails?) {
        savedStateHandle[GROUP_NAME_INPUT] = details?.name.orEmpty()
        savedStateHandle[IS_GROUP_EXCLUDED] = details?.excluded.orFalse()
    }

    override fun onEditClick() {
        savedStateHandle[EDIT_MODE_ACTIVE] = true
    }

    override fun onEditDismiss() {
        viewModelScope.launch {
            if (isNewGroup) {
                eventBus.send(TxGroupDetailsEvent.NavigateUp)
                return@launch
            }
            val groupDetails = groupDetails.first()
            updateInputsFromGroupDetails(groupDetails)
            savedStateHandle[EDIT_MODE_ACTIVE] = false
        }
    }

    override fun onEditConfirm() {
        viewModelScope.launch {
            val name = groupNameInput.value.trim()
            if (name.isEmpty()) {
                eventBus.send(
                    TxGroupDetailsEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_transaction_group_name,
                            true
                        )
                    )
                )
                return@launch
            }
            val createdTimestamp = groupCreatedTimestamp.first()
            val excluded = isGroupExcluded.value
            val id = groupIdFlow.value.coerceAtLeast(RivoDatabase.DEFAULT_ID_LONG)
            val insertedId = txGroupRepo.saveGroup(
                id = id,
                name = name,
                createdTimestamp = createdTimestamp,
                excluded = excluded
            )
            groupIdFlow.update { insertedId }
            savedStateHandle[EDIT_MODE_ACTIVE] = false
            eventBus.send(
                TxGroupDetailsEvent.ShowUiMessage(
                    UiText.StringResource(
                        if (isNewGroup) R.string.transaction_group_created
                        else R.string.transaction_group_updated
                    )
                )
            )
        }
    }

    override fun onNameChange(value: String) {
        savedStateHandle[GROUP_NAME_INPUT] = value
    }

    override fun onExclusionToggle(excluded: Boolean) {
        savedStateHandle[IS_GROUP_EXCLUDED] = excluded
    }

    sealed class TxGroupDetailsEvent {
        object NavigateUp : TxGroupDetailsEvent()
        data class ShowUiMessage(val uiText: UiText) : TxGroupDetailsEvent()
    }
}

private const val EDIT_MODE_ACTIVE = "EDIT_MODE_ACTIVE"
private const val GROUP_NAME_INPUT = "GROUP_NAME_INPUT"
private const val IS_GROUP_EXCLUDED = "IS_GROUP_EXCLUDED"