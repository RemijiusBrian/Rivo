package dev.ridill.rivo.core.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnim(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(resId))
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun ManualLottieAnim(
    @RawRes resId: Int,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(resId))
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        contentScale = contentScale,
        progress = progress
    )
}

@Composable
fun InfiniteLottieAnim(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        contentScale = contentScale
    )
}