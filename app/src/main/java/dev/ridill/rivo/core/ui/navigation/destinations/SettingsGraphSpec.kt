package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

object SettingsGraphSpec : NavGraphSpec, BottomNavDestination {

    override val route: String = "settings_graph"

    override val labelRes: Int = R.string.destination_settings

    override val iconRes: Int = R.drawable.ic_outline_settings

    override val children: List<NavDestination> = listOf(
        SettingsScreenSpec,
        BackupSettingsScreenSpec
    )
}