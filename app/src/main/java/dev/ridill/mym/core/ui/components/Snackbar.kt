package dev.ridill.mym.core.ui.components

import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun MYMSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { snackbarData ->
        MYMSnackbar(
            snackbarData = snackbarData,
            onSwipeDismiss = { dismissValue ->
                if (dismissValue == DismissValue.DismissedToEnd) {
                    snackbarData.dismiss()
                }
            }
        )
    }
) = SnackbarHost(
    hostState = hostState,
    modifier = modifier,
    snackbar = snackbar
)

@Composable
fun MYMSnackbar(
    snackbarData: SnackbarData,
    onSwipeDismiss: (DismissValue) -> Unit,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    actionContentColor: Color = SnackbarDefaults.actionContentColor,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor
) {
    val visuals = snackbarData.visuals as MYMSnackbarVisuals
    val isError = visuals.isError
    val dismissState = rememberDismissState(
        confirmValueChange = {
            onSwipeDismiss(it)
            true
        }
    )

    SwipeToDismiss(
        state = dismissState,
        background = {},
        dismissContent = {
            Snackbar(
                snackbarData = snackbarData,
                actionOnNewLine = actionOnNewLine,
                shape = shape,
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
                else containerColor,
                contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
                else contentColor,
                actionColor = actionColor,
                actionContentColor = actionContentColor,
                dismissActionContentColor = dismissActionContentColor
            )
        },
        directions = setOf(DismissDirection.StartToEnd),
        modifier = modifier
    )
}

class MYMSnackbarVisuals(
    val isError: Boolean,
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean
) : SnackbarVisuals

class SnackbarController(
    val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {
    private var snackbarJob: Job? = null

    private fun cancelCurrentJob() {
        snackbarJob?.cancel()
    }

    init {
        cancelCurrentJob()
    }

    fun showSnackbar(
        message: String,
        isError: Boolean = false,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short
        else SnackbarDuration.Indefinite,
        onSnackbarResult: ((SnackbarResult) -> Unit)? = null
    ) {
        cancelCurrentJob()
        snackbarJob = coroutineScope.launch {
            val visuals = MYMSnackbarVisuals(
                isError = isError,
                actionLabel = actionLabel,
                duration = duration,
                message = message,
                withDismissAction = withDismissAction
            )

            val snackbarResult = snackbarHostState.showSnackbar(visuals)
            onSnackbarResult?.invoke(snackbarResult)
        }
    }
}

@Composable
fun rememberSnackbarController(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): SnackbarController = remember(snackbarHostState, coroutineScope) {
    SnackbarController(
        snackbarHostState = snackbarHostState,
        coroutineScope = coroutineScope
    )
}