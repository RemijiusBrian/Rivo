package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R

data object ScheduledTransactionsListScreenSpec : ScreenSpec {
    override val route: String = "scheduled_transactions_list"
    override val labelRes: Int = R.string.destination_scheduled_transactions_list

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
    }
}