package dev.ridill.mym.core.ui.navigation.destinations

import dev.ridill.mym.R

object SettingsDestination : BottomNavDestination {

    override val iconRes: Int = R.drawable.ic_settings

    override val route: String = "settings"

    override val labelRes: Int = R.string.destination_settings
}