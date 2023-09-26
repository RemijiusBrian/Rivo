package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.icons.ObjectGroup

object TransactionGroupsGraph : NavGraphSpec, BottomNavDestination {
    override val route: String = "transaction_group_graph"

    override val labelRes: Int = R.string.destination_tx_groups

    override val icon: ImageVector = Icons.Outlined.ObjectGroup

    override val children: List<NavDestination> = listOf(
        TxGroupsListScreenSpec
    )
}