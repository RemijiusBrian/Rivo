package dev.ridill.rivo.core.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.ui.theme.SpacingSmall

@Composable
fun EmptyListIndicator(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    size: Dp = DefaultSize
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        InfiniteLottieAnim(
            resId = resId,
            modifier = Modifier
                .size(size)
        )
    }
}

private val DefaultSize = 80.dp