package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.icons.RivoSettings

object SettingsGraphSpec : NavGraphSpec, BottomNavDestination {

    override val icon: ImageVector = Icons.Outlined.RivoSettings

    override val route: String = "settings_graph"

    override val labelRes: Int = R.string.destination_settings

    override val children: List<NavDestination> = listOf(
        SettingsScreenSpec,
        BackupSettingsScreenSpec
    )
}