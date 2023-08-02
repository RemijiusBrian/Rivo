package dev.ridill.mym.core.ui.navigation.destinations

import dev.ridill.mym.R

object AllExpensesDestination : BottomNavDestination {
    override val iconRes: Int = R.drawable.ic_all_expenses

    override val route: String = "all_expenses"

    override val labelRes: Int = R.string.destination_all_expenses
}