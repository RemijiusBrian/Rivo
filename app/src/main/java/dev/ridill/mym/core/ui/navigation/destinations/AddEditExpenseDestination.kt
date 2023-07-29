package dev.ridill.mym.core.ui.navigation.destinations

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import dev.ridill.mym.R

object AddEditExpenseDestination : NavDestination {
    override val route: String = "add_edit_expense/{$ARG_EXPENSE_ID}"

    override val labelRes: Int = R.string.destination_dashboard

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_EXPENSE_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = ARG_INVALID_ID_LONG
        }
    )

    fun routeWithArg(expenseId: Long? = null): String =
        route.replace("{$ARG_EXPENSE_ID}", (expenseId ?: ARG_INVALID_ID_LONG).toString())

    fun getExpenseIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_EXPENSE_ID) ?: ARG_INVALID_ID_LONG

    fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_EXPENSE_ID) != ARG_INVALID_ID_LONG

    fun isEditMode(expenseId: Long?): Boolean = expenseId != ARG_INVALID_ID_LONG
}

private const val ARG_EXPENSE_ID = "ARG_EXPENSE_ID"