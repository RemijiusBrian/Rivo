package dev.ridill.rivo.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

private val CornerRadiusExtraSmall = 4.dp
private val CornerRadiusSmall = 8.dp
private val CornerRadiusMedium = 12.dp
private val CornerRadiusLarge = 16.dp
private val CornerRadiusExtraLarge = 24.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(CornerRadiusExtraSmall),
    small = RoundedCornerShape(CornerRadiusSmall),
    medium = RoundedCornerShape(CornerRadiusMedium),
    large = RoundedCornerShape(CornerRadiusLarge),
    extraLarge = RoundedCornerShape(CornerRadiusExtraLarge),
)