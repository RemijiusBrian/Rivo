package dev.ridill.mym.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repo: DashboardRepository
) : ViewModel() {

    private val monthlyLimit = repo.getMonthlyLimit()

    private val spentAmount = repo.getExpenditureForCurrentMonth()

    private val balance = combineTuple(
        monthlyLimit,
        spentAmount
    ).map { (limit, exp) ->
        limit - exp
    }.distinctUntilChanged()

    private val recentSpends = repo.getRecentSpends()

    val state = combineTuple(
        monthlyLimit,
        spentAmount,
        balance,
        recentSpends
    ).map { (
                monthlyLimit,
                spentAmount,
                balance,
                recentSpends
            ) ->
        DashboardState(
            balance = balance,
            spentAmount = spentAmount,
            monthlyLimit = monthlyLimit,
            recentSpends = recentSpends
        )
    }.asStateFlow(viewModelScope, DashboardState())
}

const val DASHBOARD_ACTION_RESULT = "DASHBOARD_ACTION_RESULT"