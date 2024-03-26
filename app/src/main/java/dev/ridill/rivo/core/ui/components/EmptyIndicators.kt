package dev.ridill.rivo.core.ui.components

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
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
    @RawRes rawResId: Int,
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
            resId = rawResId,
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

@Composable
fun LazyItemScope.ListEmptyIndicatorItem(
    @RawRes rawResId: Int,
    modifier: Modifier = Modifier,
    @StringRes messageRes: Int? = null,
    heightFraction: Float = LIST_EMPTY_INDICATOR_CONTAINER_HEIGHT_FRACTION,
    size: Dp = DefaultSize
) {
    Box(
        modifier = Modifier
            .fillParentMaxWidth()
            .fillParentMaxHeight(heightFraction)
            .animateItemPlacement()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        EmptyListIndicator(
            rawResId = rawResId,
            messageRes = messageRes,
            size = size
        )
    }
}

private const val LIST_EMPTY_INDICATOR_CONTAINER_HEIGHT_FRACTION = 0.5f