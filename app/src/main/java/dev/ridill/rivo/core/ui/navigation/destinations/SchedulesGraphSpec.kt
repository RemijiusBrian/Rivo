package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

data object SchedulesGraphSpec : NavGraphSpec, BottomNavDestination {
    override val iconRes: Int = R.drawable.ic_outline_schedule
    override val route: String = "schedules_graph"
    override val labelRes: Int = R.string.destination_schedules_graph
    override val children: List<NavDestination> = listOf(
        AllSchedulesScreenSpec
    )
}