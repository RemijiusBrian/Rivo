package dev.ridill.mym.expense.presentation.allExpenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Month
import javax.inject.Inject

@HiltViewModel
class AllExpensesViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val tagsRepo: TagsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), AllExpensesActions {

    private val selectedDate = savedStateHandle
        .getStateFlow(SELECTED_DATE, DateUtil.now().toLocalDate())

    private val totalExpenditure = selectedDate.flatMapLatest { date ->
        expenseRepo.getTotalExpenditureForDate(date)
    }.distinctUntilChanged()

    private val tagsWithExpenditures = combineTuple(
        selectedDate,
        totalExpenditure
    ).flatMapLatest { (date, expenditure) ->
        tagsRepo.getTagsWithExpenditures(
            date = date,
            totalExpenditure = expenditure
        )
    }

    private val selectedTag = savedStateHandle.getStateFlow<String?>(SELECTED_TAG, null)

    val state = combineTuple(
        selectedDate,
        totalExpenditure,
        tagsWithExpenditures,
        selectedTag
    ).map { (
                selectedDate,
                totalExpenditure,
                tagsWithExpenditures,
                selectedTag
            ) ->
        AllExpensesState(
            selectedDate = selectedDate,
            yearsList = listOf(2023),
            totalExpenditure = totalExpenditure,
            tagsWithExpenditures = tagsWithExpenditures,
            selectedTag = selectedTag
        )
    }.asStateFlow(viewModelScope, AllExpensesState())

    override fun onMonthSelect(month: Month) {
        savedStateHandle[SELECTED_DATE] = selectedDate.value.withMonth(month.value)
    }

    override fun onYearSelect(year: Int) {
        savedStateHandle[SELECTED_DATE] = selectedDate.value.withYear(year)
    }

    override fun onTagClick(tag: String) {
        savedStateHandle[SELECTED_TAG] = tag
            .takeIf { it != selectedTag.value }
    }

    override fun onNewTagClick() {
    }
}

private const val SELECTED_DATE = "SELECTED_DATE"
private const val SELECTED_TAG = "SELECTED_TAG_NAME"