package dev.ridill.rivo.core.ui.navigation.destinations

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.NewLine
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactions.presentation.addEditTransaction.ACTION_ADD_EDIT_TX_OR_SCHEDULE
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionScreen
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_SCHEDULE_SAVED
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_DELETED
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_SAVED
import java.time.LocalDateTime
import java.util.Currency

data object AddEditTransactionScreenSpec : ScreenSpec {
    override val route: String = """
        add_edit_transaction/
        {$ARG_TRANSACTION_ID}
        ?$ARG_LINK_FOLDER_ID={$ARG_LINK_FOLDER_ID}
        &$ARG_IS_SCHEDULE_MODE_ACTIVE={$ARG_IS_SCHEDULE_MODE_ACTIVE}
        &$ARG_INITIAL_TIMESTAMP={$ARG_INITIAL_TIMESTAMP}
    """.trimIndent()
        .replace(String.NewLine, String.Empty)

    override val labelRes: Int = R.string.destination_add_edit_transaction

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_TRANSACTION_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = ARG_INVALID_ID_LONG
        },
        navArgument(ARG_LINK_FOLDER_ID) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(ARG_IS_SCHEDULE_MODE_ACTIVE) {
            type = NavType.BoolType
            nullable = false
            defaultValue = false
        },
        navArgument(ARG_INITIAL_TIMESTAMP) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = AUTO_DETECT_TRANSACTION_DEEPLINK_URI_PATTERN },
        navDeepLink { uriPattern = ADD_TRANSACTION_SHORTCUT_DEEPLINK_URI_PATTERN }
    )

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutVertically { it } }

    fun routeWithArg(
        transactionId: Long? = null,
        transactionFolderId: Long? = null,
        isScheduleTxMode: Boolean = false,
        initialDateTime: LocalDateTime? = null
    ): String = route
        .replace(
            oldValue = "{$ARG_TRANSACTION_ID}",
            newValue = (transactionId ?: ARG_INVALID_ID_LONG).toString()
        )
        .replace(
            oldValue = "{$ARG_LINK_FOLDER_ID}",
            newValue = transactionFolderId?.toString().orEmpty()
        )
        .replace(
            oldValue = "{$ARG_IS_SCHEDULE_MODE_ACTIVE}",
            newValue = isScheduleTxMode.toString()
        )
        .replace(
            oldValue = "{$ARG_INITIAL_TIMESTAMP}",
            newValue = initialDateTime?.toString().orEmpty()
        )

    fun getTransactionIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_TRANSACTION_ID) ?: ARG_INVALID_ID_LONG

    fun getFolderIdToLinkFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<String?>(ARG_LINK_FOLDER_ID)?.toLongOrNull()

    fun getIsScheduleModeFromSavedStateHandle(savedStateHandle: SavedStateHandle): Boolean =
        savedStateHandle.get<Boolean>(ARG_IS_SCHEDULE_MODE_ACTIVE) == true

    fun getInitialTimestampFromSavedStateHandle(savedStateHandle: SavedStateHandle): LocalDateTime? =
        savedStateHandle.get<String?>(ARG_INITIAL_TIMESTAMP)?.let {
            DateUtil.parseDateTimeOrNull(it)
        }

    private fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_TRANSACTION_ID) != ARG_INVALID_ID_LONG

    fun buildAutoDetectTransactionDeeplinkUri(id: Long): Uri =
        AUTO_DETECT_TRANSACTION_DEEPLINK_URI_PATTERN.replace("{$ARG_TRANSACTION_ID}", id.toString())
            .toUri()

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: AddEditTransactionViewModel = hiltViewModel(navBackStackEntry)
        val amount = viewModel.amountInput.collectAsStateWithLifecycle(initialValue = "")
        val note = viewModel.noteInput.collectAsStateWithLifecycle(initialValue = "")
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagInput = viewModel.tagInput.collectAsStateWithLifecycle()
        val tagsPagingItems = viewModel.tagsPagingData.collectAsLazyPagingItems()
        val folderSearchQuery = viewModel.folderSearchQuery.collectAsStateWithLifecycle()
        val folderList = viewModel.foldersList.collectAsLazyPagingItems()

        val isEditMode = isArgEditMode(navBackStackEntry)

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        DestinationResultEffect(
            key = ACTION_NEW_FOLDER_CREATE,
            navBackStackEntry = navBackStackEntry,
            onResult = viewModel::onCreateFolderResult
        )

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                AddEditTransactionViewModel.AddEditTransactionEvent.TransactionDeleted -> {
                    navController.navigateUpWithResult(
                        ACTION_ADD_EDIT_TX_OR_SCHEDULE,
                        RESULT_TRANSACTION_DELETED
                    )
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        message = event.uiText.asString(context),
                        isError = event.uiText.isErrorText
                    )
                }

                AddEditTransactionViewModel.AddEditTransactionEvent.NavigateToFolderDetailsForCreation -> {
                    navController.navigate(
                        FolderDetailsScreenSpec.routeWithArgs(
                            transactionFolderId = null,
                            exitAfterClear = true
                        )
                    )
                }

                AddEditTransactionViewModel.AddEditTransactionEvent.TransactionSaved -> {
                    navController.navigateUpWithResult(
                        ACTION_ADD_EDIT_TX_OR_SCHEDULE,
                        RESULT_TRANSACTION_SAVED
                    )
                }

                AddEditTransactionViewModel.AddEditTransactionEvent.ScheduleSaved -> {
                    navController.navigateUpWithResult(
                        ACTION_ADD_EDIT_TX_OR_SCHEDULE,
                        RESULT_SCHEDULE_SAVED
                    )
                }
            }
        }

        AddEditTransactionScreen(
            isEditMode = isEditMode,
            appCurrencyPreference = appCurrencyPreference,
            snackbarController = snackbarController,
            amountInput = { amount.value },
            noteInput = { note.value },
            tagNameInput = { tagInput.value?.name.orEmpty() },
            tagColorInput = { tagInput.value?.colorCode },
            tagExclusionInput = { tagInput.value?.excluded },
            tagsPagingItems = tagsPagingItems,
            folderSearchQuery = { folderSearchQuery.value },
            folderList = folderList,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}

const val ARG_TRANSACTION_ID = "ARG_TRANSACTION_ID"
private const val ARG_LINK_FOLDER_ID = "ARG_LINK_FOLDER_ID"
private const val ARG_IS_SCHEDULE_MODE_ACTIVE = "ARG_IS_SCHEDULE_MODE_ACTIVE"
private const val ARG_INITIAL_TIMESTAMP = "ARG_INITIAL_TIMESTAMP"

private const val AUTO_DETECT_TRANSACTION_DEEPLINK_URI_PATTERN =
    "$DEEP_LINK_URI/auto_detect_transaction/{$ARG_TRANSACTION_ID}"
private const val ADD_TRANSACTION_SHORTCUT_DEEPLINK_URI_PATTERN =
    "$DEEP_LINK_URI/add_transaction_shortcut"

const val ACTION_NEW_FOLDER_CREATE = "ACTION_NEW_FOLDER_CREATE"