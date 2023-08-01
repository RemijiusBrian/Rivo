package dev.ridill.mym.dashboard.domain.model

import dev.ridill.mym.expense.domain.model.ExpenseTag
import java.time.LocalDate

data class RecentSpend(
    val id: Long,
    val note: String,
    val amount: String,
    val date: LocalDate,
    val tag: ExpenseTag?
)