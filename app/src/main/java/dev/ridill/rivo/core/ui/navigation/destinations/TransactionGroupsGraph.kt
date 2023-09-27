package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

object TransactionGroupsGraph : NavGraphSpec, BottomNavDestination {
    override val route: String = "transaction_group_graph"

    override val labelRes: Int = R.string.destination_tx_groups

    override val iconRes: Int = R.drawable.ic_folder

    override val children: List<NavDestination> = listOf(
        TxGroupsListScreenSpec,
        TransactionGroupDetailsScreenSpec
    )
}