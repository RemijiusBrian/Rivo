package dev.ridill.rivo.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_SCHEDULE_SAVED
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_DELETED
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_SAVED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repo: DashboardRepository,
    private val signInService: GoogleSignInService,
    private val notificationHelper: NotificationHelper<Transaction>,
    private val eventBus: EventBus<DashboardEvent>
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

    private val upcomingSchedules = repo.getActiveSchedules()

    private val recentSpends = repo.getRecentSpends()

    private val signedInUsername = MutableStateFlow<String?>(null)

    val state = combineTuple(
        currency,
        budgetInclCredits,
        spentAmount,
        creditAmount,
        balance,
        upcomingSchedules,
        recentSpends,
        signedInUsername
    ).map { (
                currency,
                budgetInclCredits,
                spentAmount,
                creditAmount,
                balance,
                upcomingSchedules,
                recentSpends,
                signedInUsername
            ) ->
        DashboardState(
            currency = currency,
            balance = balance,
            spentAmount = spentAmount,
            creditAmount = creditAmount,
            monthlyBudgetInclCredits = budgetInclCredits,
            upcomingSchedules = upcomingSchedules,
            recentSpends = recentSpends,
            signedInUsername = signedInUsername
        )
    }.asStateFlow(viewModelScope, DashboardState())

    val events = eventBus.eventFlow

    init {
        updateSignedInUsername()
        cancelNotifications()
    }

    fun updateSignedInUsername() {
        signedInUsername.update {
            signInService.getSignedInAccount()?.displayName
        }
    }

    fun onNavResult(result: String) = viewModelScope.launch {
        when (result) {
            RESULT_TRANSACTION_DELETED ->
                DashboardEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_deleted))

            RESULT_TRANSACTION_SAVED ->
                DashboardEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_saved))

            RESULT_SCHEDULE_SAVED -> DashboardEvent.ScheduleSaved
            else -> null
        }?.let { eventBus.send(it) }
    }

    private fun cancelNotifications() {
        notificationHelper.dismissAllNotifications()
    }

    sealed interface DashboardEvent {
        data class ShowUiMessage(val uiText: UiText) : DashboardEvent
        data object ScheduleSaved : DashboardEvent
    }
}