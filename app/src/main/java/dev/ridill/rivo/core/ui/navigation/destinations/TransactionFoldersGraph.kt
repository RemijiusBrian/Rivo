package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

object TransactionFoldersGraph : NavGraphSpec, BottomNavDestination {
    override val route: String = "transaction_folders_graph"

    override val labelRes: Int = R.string.destination_tx_folders

    override val iconRes: Int = R.drawable.ic_outline_folder

    override val children: List<NavDestination> = listOf(
        TxFoldersListScreenSpec,
        TransactionFolderDetailsScreenSpec
    )
}