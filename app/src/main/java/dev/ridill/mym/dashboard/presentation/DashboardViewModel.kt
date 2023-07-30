package dev.ridill.mym.dashboard.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: DashboardRepository
) : ViewModel(), DashboardActions {

    private val monthlyLimit = repo.getMonthlyLimit()

    private val spentAmount = repo.getExpenditureForCurrentMonth()

    private val showLimitInput = savedStateHandle.getStateFlow(SHOW_LIMIT_INPUT, false)

    private val isLimitInputError = MutableStateFlow(false)

    private val balance = combineTuple(
        monthlyLimit,
        spentAmount
    ).map { (limit, exp) ->
        limit - exp
    }.distinctUntilChanged()

    private val recentTransactions = repo.getRecentTransactions()

    val state = combineTuple(
        monthlyLimit,
        spentAmount,
        balance,
        recentTransactions,
        showLimitInput,
        isLimitInputError
    ).map { (
                monthlyLimit,
                spentAmount,
                balance,
                recentTransactions,
                showLimitInput,
                isLimitInputError
            ) ->
        DashboardState(
            balance = balance,
            spentAmount = spentAmount,
            monthlyLimit = monthlyLimit,
            recentTransactions = recentTransactions,
            showLimitInput = showLimitInput,
            isLimitInputError = isLimitInputError
        )
    }.asStateFlow(viewModelScope, DashboardState())

    private val eventsChannel = Channel<DashboardEvent>()
    val events get() = eventsChannel.receiveAsFlow()

    override fun onSetLimitClick() {
        isLimitInputError.update { false }
        savedStateHandle[SHOW_LIMIT_INPUT] = true
    }

    override fun onSetLimitDismiss() {
        savedStateHandle[SHOW_LIMIT_INPUT] = false
    }

    override fun onSetLimitConfirm(value: String) {
        viewModelScope.launch {
            val longValue = value.toLongOrNull() ?: -1L
            isLimitInputError.update { longValue <= -1L }
            if (isLimitInputError.value) return@launch

            repo.updateMonthlyLimit(longValue)
            savedStateHandle[SHOW_LIMIT_INPUT] = false
            eventsChannel.send(DashboardEvent.MonthlyLimitSet)
        }
    }

    sealed class DashboardEvent {
        object MonthlyLimitSet : DashboardEvent()
    }
}

const val DASHBOARD_ACTION_RESULT = "DASHBOARD_ACTION_RESULT"

private const val SHOW_LIMIT_INPUT = "SHOW_LIMIT_INPUT"