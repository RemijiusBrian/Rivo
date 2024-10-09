package dev.ridill.rivo.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTxResult
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repo: DashboardRepository,
    authRepo: AuthRepository,
    private val notificationHelper: NotificationHelper<Transaction>,
    private val eventBus: EventBus<DashboardEvent>
) : ViewModel() {
    private val monthlyBudget = repo.getCurrentBudget()

    private val debitAmount = repo.getTotalDebitsForCurrentMonth()
    private val creditAmount = repo.getTotalCreditsForCurrentMonth()
    private val budgetInclCredits = combineTuple(
        monthlyBudget,
        creditAmount
    ).map { (budget, credit) ->
        budget + credit
    }.distinctUntilChanged()

    private val balance = combineTuple(
        budgetInclCredits,
        debitAmount
    ).map { (budgetInclCredits, debits) ->
        budgetInclCredits - debits
    }.distinctUntilChanged()

    private val activeSchedules = repo.getSchedulesActiveThisMonth()

    val recentSpendsPagingData = repo.getRecentSpends()

    private val signedInUsername = authRepo.getAuthState().map { state ->
        when (state) {
            is AuthState.Authenticated -> state.account.displayName
            AuthState.UnAuthenticated -> null
        }
    }.distinctUntilChanged()

    val state = combineTuple(
        budgetInclCredits,
        debitAmount,
        creditAmount,
        balance,
        activeSchedules,
        signedInUsername
    ).map { (
                budgetInclCredits,
                spentAmount,
                creditAmount,
                balance,
                activeSchedules,
                signedInUsername
            ) ->
        DashboardState(
            balance = balance,
            spentAmount = spentAmount,
            creditAmount = creditAmount,
            monthlyBudgetInclCredits = budgetInclCredits,
            activeSchedules = activeSchedules,
            signedInUsername = signedInUsername
        )
    }.asStateFlow(viewModelScope, DashboardState())

    val events = eventBus.eventFlow

    init {
        cancelNotifications()
    }

    fun onNavResult(result: AddEditTxResult) = viewModelScope.launch {
        val event = when (result) {
            AddEditTxResult.TRANSACTION_DELETED ->
                DashboardEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_deleted))

            AddEditTxResult.TRANSACTION_SAVED ->
                DashboardEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_saved))

            AddEditTxResult.SCHEDULE_SAVED -> DashboardEvent.ScheduleSaved
        }

        eventBus.send(event)
    }

    private fun cancelNotifications() {
        notificationHelper.dismissAllNotifications()
    }

    sealed interface DashboardEvent {
        data class ShowUiMessage(val uiText: UiText) : DashboardEvent
        data object ScheduleSaved : DashboardEvent
    }
}