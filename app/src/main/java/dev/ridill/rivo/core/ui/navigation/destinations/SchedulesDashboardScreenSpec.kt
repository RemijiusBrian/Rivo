package dev.ridill.rivo.core.ui.navigation.destinations

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.schedules.presentation.scheduleDashboard.SchedulesDashboardScreen
import dev.ridill.rivo.schedules.presentation.scheduleDashboard.SchedulesDashboardViewModel

data object SchedulesDashboardScreenSpec : ScreenSpec {
    override val route: String = "schedules_dashboard"
    override val labelRes: Int = R.string.destination_schedules_and_plans_list

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = VIEW_DEEPLINK
            action = Intent.ACTION_VIEW
        }
    )

    fun getViewDeeplinkUri(): Uri = VIEW_DEEPLINK.toUri()

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: SchedulesDashboardViewModel = hiltViewModel(navBackStackEntry)
        val schedulesList = viewModel.scheduledTransactions.collectAsLazyPagingItems()
        val plansList = viewModel.plansList.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val planInput = viewModel.planInput.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        CollectFlowEffect(
            flow = viewModel.events,
            snackbarController,
            context
        ) { event ->
            when (event) {
                is SchedulesDashboardViewModel.SchedulesAndPlansListEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText.asString(context))
                }
            }
        }

        SchedulesDashboardScreen(
            context = context,
            snackbarController = snackbarController,
            schedules = schedulesList,
            plansList = plansList,
            state = state,
            planInputName = { planInput.value?.name.orEmpty() },
            planInputColorCode = { planInput.value?.colorCode },
            isNewPlan = planInput.value?.isNew,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToAddEditSchedule = {
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        transactionId = it,
                        isScheduleTxMode = true
                    )
                )
            }
        )
    }
}

private const val VIEW_DEEPLINK = "$DEEP_LINK_URI/view_schedules_dashboard"