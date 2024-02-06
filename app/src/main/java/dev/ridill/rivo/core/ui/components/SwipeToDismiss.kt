package dev.ridill.rivo.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay

@Composable
fun <T> SwipeToDismissContainer(
    item: T,
    onDismiss: (T) -> Unit,
    modifier: Modifier = Modifier,
    animationDuration: Int = DEFAULT_ANIM_DURATION,
    directions: Set<DismissDirection> = setOf(
        DismissDirection.EndToStart,
        DismissDirection.StartToEnd
    ),
    background: @Composable RowScope.(DismissState) -> Unit = {},
    content: @Composable RowScope.(T) -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val state = rememberDismissState(
        confirmValueChange = { value ->
            when (value) {
                DismissValue.Default -> false
                DismissValue.DismissedToEnd -> {
                    if (directions.contains(DismissDirection.StartToEnd)) {
                        isRemoved = true
                        true
                    } else {
                        false
                    }
                }

                DismissValue.DismissedToStart -> {
                    if (directions.contains(DismissDirection.EndToStart)) {
                        isRemoved = true
                        true
                    } else {
                        false
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        state.reset()
    }

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onDismiss(item)
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut(),
        modifier = modifier
    ) {
        SwipeToDismiss(
            state = state,
            background = { background(state) },
            dismissContent = { content(item) },
            directions = directions
        )
    }
}

private const val DEFAULT_ANIM_DURATION = 500

@Composable
fun DismissBackground(
    swipeDismissState: DismissState,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer,
    contentColor: Color = contentColorFor(containerColor)
) {
    val color = if (swipeDismissState.dismissDirection != null) {
        containerColor
    } else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor
        )
    }
}