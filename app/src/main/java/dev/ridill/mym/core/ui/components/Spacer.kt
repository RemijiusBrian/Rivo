package dev.ridill.mym.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge
import dev.ridill.mym.core.ui.theme.SpacingExtraSmall
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall

// Spacers For Columns
@Composable
fun ColumnScope.SpacerExtraSmall() = VerticalSpacer(SpacingExtraSmall)

@Composable
fun ColumnScope.SpacerSmall() = VerticalSpacer(SpacingSmall)

@Composable
fun ColumnScope.SpacerMedium() = VerticalSpacer(SpacingMedium)

@Composable
fun ColumnScope.SpacerLarge() = VerticalSpacer(SpacingLarge)

@Composable
fun ColumnScope.SpacerExtraLarge() = VerticalSpacer(SpacingExtraLarge)

@Composable
fun ColumnScope.Spacer(spacing: Dp) = VerticalSpacer(spacing)

@Composable
fun ColumnScope.Spacer(
    weight: Float
) = Spacer(modifier = Modifier.weight(weight))

@Composable
fun VerticalSpacer(
    spacing: Dp
) = Spacer(modifier = Modifier.height(spacing))

// Spacers For Rows
@Composable
fun RowScope.SpacerExtraSmall() = HorizontalSpacer(SpacingExtraSmall)

@Composable
fun RowScope.SpacerSmall() = HorizontalSpacer(SpacingSmall)

@Composable
fun RowScope.SpacerMedium() = HorizontalSpacer(SpacingMedium)

@Composable
fun RowScope.SpacerLarge() = HorizontalSpacer(SpacingLarge)

@Composable
fun RowScope.SpacerExtraLarge() = HorizontalSpacer(SpacingExtraLarge)

@Composable
fun RowScope.Spacer(spacing: Dp) = HorizontalSpacer(spacing)

@Composable
fun RowScope.Spacer(
    weight: Float
) = Spacer(modifier = Modifier.weight(weight))

@Composable
fun HorizontalSpacer(
    spacing: Dp
) = Spacer(modifier = Modifier.width(spacing))
