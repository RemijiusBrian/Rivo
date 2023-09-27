package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactionGroups.presentation.groupDetails.TxGroupDetailsScreen
import dev.ridill.rivo.transactionGroups.presentation.groupDetails.TxGroupDetailsViewModel

object TransactionGroupDetailsScreenSpec : ScreenSpec {
    override val route: String = "transaction_group_details/{$ARG_TX_GROUP_ID}"

    override val labelRes: Int = R.string.destination_tx_groups_details

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_TX_GROUP_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = ARG_INVALID_ID_LONG
        }
    )

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutVertically { it } }

    fun routeWithArg(transactionGroupId: Long?): String =
        route.replace("{$ARG_TX_GROUP_ID}", (transactionGroupId ?: ARG_INVALID_ID_LONG).toString())

    fun getGroupIdArgFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_TX_GROUP_ID) ?: ARG_INVALID_ID_LONG

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TxGroupDetailsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val nameInput = viewModel.groupNameInput.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        LaunchedEffect(context, snackbarController, viewModel) {
            viewModel.events.collect { event ->
                when (event) {
                    TxGroupDetailsViewModel.TxGroupDetailsEvent.NavigateUp -> {
                        navController.navigateUp()
                    }

                    is TxGroupDetailsViewModel.TxGroupDetailsEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        TxGroupDetailsScreen(
            snackbarController = snackbarController,
            state = state,
            groupName = { nameInput.value },
            actions = viewModel,
            navigateToAddEditTransaction = { transactionId ->
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        transactionId = transactionId,
                        transactionGroupId = state.groupId
                    )
                )
            },
            navigateUp = navController::navigateUp
        )
    }
}

private const val ARG_TX_GROUP_ID = "ARG_TX_GROUP_ID"