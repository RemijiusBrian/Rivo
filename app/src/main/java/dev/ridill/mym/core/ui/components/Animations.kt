package dev.ridill.mym.core.ui.components

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import kotlin.math.roundToLong

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
fun animateColorBetween(
    start: Color,
    end: Color,
    @FloatRange(0.0, 1.0) progress: Float
): Color {
    val animatable = remember(start, end) {
        TargetBasedAnimation(
            animationSpec = tween(),
            typeConverter = Color.VectorConverter(ColorSpaces.Srgb),
            initialValue = start,
            targetValue = end,
            initialVelocityVector = null
        )
    }

    return animatable.getValueFromNanos(
        (animatable.durationNanos * progress).roundToLong()
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