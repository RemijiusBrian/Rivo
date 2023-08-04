package dev.ridill.mym.expense.presentation.allExpenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Month
import javax.inject.Inject

@HiltViewModel
class AllExpensesViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val tagsRepo: TagsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), AllExpensesActions {

    private val selectedMonth = savedStateHandle.getStateFlow(SELECTED_MONTH, Month.AUGUST)
    private val selectedYear = savedStateHandle.getStateFlow(SELECTED_YEAR, "2023")

    private val selectedMonthAndYear = combine(selectedMonth, selectedYear) { month, year ->
        "${month.value.toString().padStart(2, '0')}-$year"
    }
        .onEach { println("AppDebug: Date $it") }
        .distinctUntilChanged()

    private val totalExpenditure = selectedMonthAndYear.flatMapLatest { date ->
        expenseRepo.getTotalExpenditureForDate(date)
    }.distinctUntilChanged()

    private val tagsWithExpenditures = combineTuple(
        selectedMonthAndYear,
        totalExpenditure
    ).flatMapLatest { (date, expenditure) ->
        tagsRepo.getTagsWithExpenditures(
            monthAndYearString = date,
            totalExpenditure = expenditure
        )
    }.onEach { println("AppDebug: Tags Info - $it") }

    val state = combineTuple(
        totalExpenditure,
        tagsWithExpenditures,
        selectedMonth,
        selectedYear
    ).map { (
                totalExpenditure,
                tagsWithExpenditures,
                selectedMonth,
                selectedYear
            ) ->
        AllExpensesState(
            totalExpenditure = totalExpenditure,
            tagsWithExpenditures = tagsWithExpenditures,
            selectedMonth = selectedMonth,
            yearsList = listOf("2023"),
            selectedYear = selectedYear
        )
    }.asStateFlow(viewModelScope, AllExpensesState())

    override fun onMonthSelect(month: Month) {
        savedStateHandle[SELECTED_MONTH] = month
    }

    override fun onYearSelect(year: String) {
        savedStateHandle[SELECTED_YEAR] = year
    }
}

private const val SELECTED_MONTH = "SELECTED_MONTH"
private const val SELECTED_YEAR = "SELECTED_YEAR"
