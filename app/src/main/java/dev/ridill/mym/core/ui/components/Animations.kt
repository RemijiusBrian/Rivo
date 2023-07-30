package dev.ridill.mym.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <T> VerticalNumberSpinnerContent(
    number: T,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "VerticalNumberSpinnerContent",
    content: @Composable AnimatedVisibilityScope.(targetState: T) -> Unit
) where T : Number, T : Comparable<T> {
    AnimatedContent(
        targetState = number,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith(
                        slideOutVertically { height -> -height } + fadeOut()
                    )
            } else {
                (slideInVertically { height -> -height } + fadeIn())
                    .togetherWith(
                        slideOutVertically { height -> height } + fadeOut()
                    )
            }.using(SizeTransform(clip = false))
        },
        modifier = modifier,
        contentAlignment = contentAlignment,
        label = label,
        content = content
    )
}

@Composable
fun FadedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    label: String = "FadedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit
) = AnimatedVisibility(
    visible = visible,
    modifier = modifier,
    enter = fadeIn(),
    exit = fadeOut(),
    label = label,
    content = content
)