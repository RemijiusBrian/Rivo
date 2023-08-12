package dev.ridill.mym.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.core.domain.service.GoogleSignInService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repo: DashboardRepository,
    private val signInService: GoogleSignInService
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

    private val signedInUsername = MutableStateFlow<String?>(null)

    val state = combineTuple(
        monthlyLimit,
        spentAmount,
        balance,
        recentSpends,
        signedInUsername
    ).map { (
                monthlyLimit,
                spentAmount,
                balance,
                recentSpends,
                signedInUsername
            ) ->
        DashboardState(
            balance = balance,
            spentAmount = spentAmount,
            monthlyLimit = monthlyLimit,
            recentSpends = recentSpends,
            signedInUsername = signedInUsername
        )
    }.asStateFlow(viewModelScope, DashboardState())

    init {
        signedInUsername.update {
            signInService.getSignedInAccount()?.displayName
        }
    }
}

const val DASHBOARD_ACTION_RESULT = "DASHBOARD_ACTION_RESULT"