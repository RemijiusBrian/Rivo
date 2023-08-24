package dev.ridill.mym.core.ui.navigation.destinations

import dev.ridill.mym.R

object SettingsGraphSpec : NavGraphSpec, BottomNavDestination {

    override val iconRes: Int = R.drawable.ic_settings

    override val route: String = "settings_graph"

    override val labelRes: Int = R.string.destination_settings

    override val children: List<NavDestination> = listOf(
        SettingsDestinationSpec,
        BackupSettingsDestinationSpec
    )
}