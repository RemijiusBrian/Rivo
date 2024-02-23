package dev.ridill.rivo.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.transactions.domain.notification.AutoAddTransactionNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repo: DashboardRepository,
    private val signInService: GoogleSignInService,
    private val autoAddTransactionNotificationHelper: AutoAddTransactionNotificationHelper
) : ViewModel() {

    private val currency = repo.getCurrencyPreference()

    private val monthlyBudget = repo.getCurrentBudget()

    private val spentAmount = repo.getExpenditureForCurrentMonth()

    private val balance = combineTuple(
        monthlyBudget,
        spentAmount
    ).map { (limit, exp) ->
        limit - exp
    }.distinctUntilChanged()

    private val recentSpends = repo.getRecentSpends()

    private val signedInUsername = MutableStateFlow<String?>(null)

    val state = combineTuple(
        currency,
        monthlyBudget,
        spentAmount,
        balance,
        recentSpends,
        signedInUsername
    ).map { (
                currency,
                monthlyBudget,
                spentAmount,
                balance,
                recentSpends,
                signedInUsername
            ) ->
        DashboardState(
            currency = currency,
            balance = balance,
            spentAmount = spentAmount,
            monthlyBudget = monthlyBudget,
            recentSpends = recentSpends,
            signedInUsername = signedInUsername
        )
    }.asStateFlow(viewModelScope, DashboardState())

    init {
        getSignedInUserName()
        cancelNotifications()
    }

    private fun getSignedInUserName() {
        signedInUsername.update {
            signInService.getSignedInAccount()?.displayName
        }
    }

    private fun cancelNotifications() {
        autoAddTransactionNotificationHelper.cancelAllNotifications()
    }
}

const val DASHBOARD_ACTION_RESULT = "DASHBOARD_ACTION_RESULT"