package dev.ridill.rivo.core.ui.navigation.destinations

import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList.SchedulesAndPlansListEvent
import dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList.SchedulesAndPlansListScreen
import dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList.SchedulesAndPlansListViewModel

data object SchedulesAndPlansListScreenSpec : ScreenSpec {
    override val route: String = "schedules_and_plans_list"
    override val labelRes: Int = R.string.destination_schedules_and_plans_list

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = DEEP_LINK_URI }
    )

    fun getViewDeeplinkUri(): Uri = VIEW_DEEPLINK.toUri()

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: SchedulesAndPlansListViewModel = hiltViewModel(navBackStackEntry)
        val schedulesList = viewModel.scheduledTransactions.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        CollectFlowEffect(
            flow = viewModel.events,
            snackbarController,
            context
        ) { event ->
            when (event) {
                is SchedulesAndPlansListEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText.asString(context))
                }
            }
        }

        SchedulesAndPlansListScreen(
            context = context,
            snackbarController = snackbarController,
            schedules = schedulesList,
            onScheduleClick = {
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        transactionId = it,
                        isScheduleTxMode = true
                    )
                )
            },
            navigateUp = navController::navigateUp,
            actions = viewModel
        )
    }
}

private const val VIEW_DEEPLINK = "$DEEP_LINK_URI/schedules_and_plans_view"