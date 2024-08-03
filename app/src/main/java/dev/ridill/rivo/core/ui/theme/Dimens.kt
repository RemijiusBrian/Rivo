package dev.ridill.rivo.core.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.domain.util.Zero

data class Spacing(
    val default: Dp = Dp.Zero,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 32.dp,
    val extraLarge: Dp = 64.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

val SpacingListEnd = 80.dp

val BorderWidthStandard = 1.dp

val IconSizeSmall = 12.dp
val IconSizeMedium = 16.dp

val ElevationLevel0 = 0.0.dp
val ElevationLevel1 = 1.0.dp
val ElevationLevel2 = 3.0.dp
val ElevationLevel3 = 6.0.dp
val ElevationLevel4 = 8.0.dp
val ElevationLevel5 = 12.0.dp