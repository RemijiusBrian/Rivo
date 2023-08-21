package dev.ridill.mym.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.ridill.mym.core.domain.util.Zero

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
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut()
                    )
            } else {
                (slideInVertically { height -> -height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> height } + fadeOut()
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

fun simpleFadeIn(
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    initialAlpha: Float = Float.Zero
): EnterTransition = fadeIn(animationSpec, initialAlpha)

fun simpleFadeOut(
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    targetAlpha: Float = Float.Zero
): ExitTransition = fadeOut(animationSpec, targetAlpha)

fun slideInHorizontallyWithFadeIn(
    duration: Int = AnimationConstants.DefaultDurationMillis,
    initialOffsetX: (fullWidth: Int) -> Int = { it }
): EnterTransition = slideInHorizontally(
    animationSpec = tween(durationMillis = duration),
    initialOffsetX = initialOffsetX
) + simpleFadeIn(
    animationSpec = tween(durationMillis = duration)
)

fun slideOutHorizontallyWithFadeOut(
    duration: Int = AnimationConstants.DefaultDurationMillis,
    targetOffsetX: (fullWidth: Int) -> Int = { it }
): ExitTransition = slideOutHorizontally(
    animationSpec = tween(durationMillis = duration),
    targetOffsetX = targetOffsetX
) + simpleFadeOut(
    animationSpec = tween(durationMillis = duration)
)

fun slideInVerticallyWithFadeIn(
    duration: Int = AnimationConstants.DefaultDurationMillis,
    initialOffsetY: (fullWidth: Int) -> Int = { it }
): EnterTransition = slideInVertically(
    animationSpec = tween(durationMillis = duration),
    initialOffsetY = initialOffsetY
) + simpleFadeIn(
    animationSpec = tween(durationMillis = duration)
)

fun slideOutVerticallyWithFadeOut(
    duration: Int = AnimationConstants.DefaultDurationMillis,
    targetOffsetY: (fullWidth: Int) -> Int = { it }
): ExitTransition = slideOutVertically(
    animationSpec = tween(durationMillis = duration),
    targetOffsetY = targetOffsetY
) + simpleFadeOut(
    animationSpec = tween(durationMillis = duration)
)