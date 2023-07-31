package dev.ridill.mym.dashboard.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: DashboardRepository,
    private val eventBus: EventBus<DashboardEvent>
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

    private val recentSpends = repo.getRecentSpends()
        .onEach { println("AppDebug: $it") }

    val state = combineTuple(
        monthlyLimit,
        spentAmount,
        balance,
        recentSpends,
        showLimitInput,
        isLimitInputError
    ).map { (
                monthlyLimit,
                spentAmount,
                balance,
                recentSpends,
                showLimitInput,
                isLimitInputError
            ) ->
        DashboardState(
            balance = balance,
            spentAmount = spentAmount,
            monthlyLimit = monthlyLimit,
            recentSpends = recentSpends,
            showLimitInput = showLimitInput,
            isLimitInputError = isLimitInputError
        )
    }.asStateFlow(viewModelScope, DashboardState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        isLimitInputError.update { false }
        savedStateHandle[SHOW_LIMIT_INPUT] = repo.isAppFirstLaunch()
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
            repo.disableAppFirstLaunch()
            savedStateHandle[SHOW_LIMIT_INPUT] = false
            eventBus.send(DashboardEvent.MonthlyLimitSet)
        }
    }

    sealed class DashboardEvent {
        object MonthlyLimitSet : DashboardEvent()
    }
}

const val DASHBOARD_ACTION_RESULT = "DASHBOARD_ACTION_RESULT"

private const val SHOW_LIMIT_INPUT = "SHOW_LIMIT_INPUT"