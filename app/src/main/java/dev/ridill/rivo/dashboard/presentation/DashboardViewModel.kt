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
    private val creditAmount = repo.getTotalCreditsForCurrentMonth()
    private val budgetInclCredits = combineTuple(
        monthlyBudget,
        creditAmount
    ).map { (budget, credit) ->
        budget + credit
    }.distinctUntilChanged()

    private val balance = combineTuple(
        budgetInclCredits,
        spentAmount
    ).map { (budgetInclCredits, debits) ->
        budgetInclCredits - debits
    }.distinctUntilChanged()

    private val recentSpends = repo.getRecentSpends()

    private val signedInUsername = MutableStateFlow<String?>(null)

    val state = combineTuple(
        currency,
        budgetInclCredits,
        spentAmount,
        balance,
        recentSpends,
        signedInUsername
    ).map { (
                currency,
                budgetInclCredits,
                spentAmount,
                balance,
                recentSpends,
                signedInUsername
            ) ->
        DashboardState(
            currency = currency,
            balance = balance,
            spentAmount = spentAmount,
            monthlyBudgetInclCredits = budgetInclCredits,
            recentSpends = recentSpends,
            signedInUsername = signedInUsername
        )
    }.asStateFlow(viewModelScope, DashboardState())

    init {
        updateSignedInUsername()
        cancelNotifications()
    }

    fun updateSignedInUsername() {
        signedInUsername.update {
            signInService.getSignedInAccount()?.displayName
        }
    }

    private fun cancelNotifications() {
        autoAddTransactionNotificationHelper.cancelAllNotifications()
    }
}

const val DASHBOARD_ACTION_RESULT = "DASHBOARD_ACTION_RESULT"