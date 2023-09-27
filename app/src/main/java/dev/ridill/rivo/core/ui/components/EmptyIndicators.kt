package dev.ridill.rivo.core.ui.components

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.SpacingSmall

@Composable
fun EmptyListIndicator(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    @StringRes messageRes: Int? = null,
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
        messageRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current
                    .copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }
    }
}

private val DefaultSize = 80.dp