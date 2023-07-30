package dev.ridill.mym.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun MYMSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { MYMSnackbar(it) }
) = SnackbarHost(
    hostState = hostState,
    modifier = modifier,
    snackbar = snackbar
)

@Composable
fun MYMSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    actionContentColor: Color = SnackbarDefaults.actionContentColor,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
) {
    val visuals = snackbarData.visuals as MYMSnackbarVisuals
    val isError = visuals.isError
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
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
}

class MYMSnackbarVisuals(
    val isError: Boolean,
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean
) : SnackbarVisuals

@Composable
fun rememberSnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

suspend fun SnackbarHostState.showMymSnackbar(
    message: String,
    isError: Boolean = false,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
) = showSnackbar(
    MYMSnackbarVisuals(
        isError = isError,
        actionLabel = actionLabel,
        duration = duration,
        message = message,
        withDismissAction = withDismissAction
    )
)