package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

data object BudgetPlansGraphSpec: NavGraphSpec, BottomNavDestination {
    override val iconRes: Int = R.drawable.ic_outline_budget
    override val route: String = "budget_plans_graph"
    override val labelRes: Int = R.string.destination_budget_plans_graph
    override val children: List<NavDestination> = listOf(

    )
}