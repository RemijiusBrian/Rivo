package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

data object ScheduledTransactionsGraphSpec : NavGraphSpec, BottomNavDestination {
    override val iconRes: Int = R.drawable.ic_outline_calendar_money
    override val route: String = "scheduled_transactions_graph"
    override val labelRes: Int = R.string.destination_schedules_graph
    override val children: List<NavDestination> = listOf(
        SchedulesDashboardScreenSpec
    )
}