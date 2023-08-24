package dev.ridill.mym.expense.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.navigation.destinations.ARG_EXPENSE_ID
import dev.ridill.mym.di.ApplicationScope
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeleteExpenseActionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: ExpenseRepository

    @Inject
    lateinit var notificationHelper: AutoAddExpenseNotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ARG_EXPENSE_ID, -1L)
        if (id < Long.Zero) return

        applicationScope.launch {
            repo.deleteExpense(id)
            notificationHelper.updateNotificationToExpenseDeleted(id.toInt())
        }
    }
}