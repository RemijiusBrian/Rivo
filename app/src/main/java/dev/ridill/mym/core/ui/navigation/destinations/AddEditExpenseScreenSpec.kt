package dev.ridill.mym.core.ui.navigation.destinations

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
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.navigateUpWithResult
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.dashboard.presentation.DASHBOARD_ACTION_RESULT
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseScreen
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_ADDED
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_DELETED
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_UPDATED

object AddEditExpenseScreenSpec : ScreenSpec {
    override val route: String = "add_edit_expense/{$ARG_EXPENSE_ID}"

    override val labelRes: Int = R.string.destination_dashboard

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_EXPENSE_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = ARG_INVALID_ID_LONG
        }
    )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = "$DEEP_LINK_URI/expense/{$ARG_EXPENSE_ID}" }
    )

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutVertically { it } }

    fun routeWithArg(expenseId: Long? = null): String =
        route.replace("{$ARG_EXPENSE_ID}", (expenseId ?: ARG_INVALID_ID_LONG).toString())

    fun getExpenseIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_EXPENSE_ID) ?: ARG_INVALID_ID_LONG

    private fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_EXPENSE_ID) != ARG_INVALID_ID_LONG

    fun isEditMode(expenseId: Long?): Boolean = expenseId != ARG_INVALID_ID_LONG

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: AddEditExpenseViewModel = hiltViewModel(navBackStackEntry)
        val amount = viewModel.amountInput.collectAsStateWithLifecycle(initialValue = "")
        val note = viewModel.noteInput.collectAsStateWithLifecycle(initialValue = "")
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagInput = viewModel.tagInput.collectAsStateWithLifecycle()

        val isEditMode = isArgEditMode(navBackStackEntry)

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    AddEditExpenseViewModel.AddEditExpenseEvent.ExpenseAdded -> {
                        navController.navigateUpWithResult(
                            DASHBOARD_ACTION_RESULT,
                            RESULT_EXPENSE_ADDED
                        )
                    }

                    AddEditExpenseViewModel.AddEditExpenseEvent.ExpenseDeleted -> {
                        navController.navigateUpWithResult(
                            DASHBOARD_ACTION_RESULT,
                            RESULT_EXPENSE_DELETED
                        )
                    }

                    AddEditExpenseViewModel.AddEditExpenseEvent.ExpenseUpdated -> {
                        navController.navigateUpWithResult(
                            DASHBOARD_ACTION_RESULT,
                            RESULT_EXPENSE_UPDATED
                        )
                    }

                    is AddEditExpenseViewModel.AddEditExpenseEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            message = event.uiText.asString(context),
                            isError = event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        AddEditExpenseScreen(
            snackbarController = snackbarController,
            amountInput = { amount.value },
            noteInput = { note.value },
            isEditMode = isEditMode,
            tagNameInput = { tagInput.value?.name.orEmpty() },
            tagColorInput = { tagInput.value?.colorCode },
            tagExclusionInput = { tagInput.value?.excluded },
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}

const val ARG_EXPENSE_ID = "ARG_EXPENSE_ID"