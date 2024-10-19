package dev.ridill.rivo.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.CornerRadiusSmall
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun RivoRangeSlider(
    valueRange: ClosedFloatingPointRange<Float>,
    value: ClosedFloatingPointRange<Float>,
    startThumbValue: () -> String?,
    endThumbValue: () -> String?,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = Int.Zero,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    RangeSlider(
        valueRange = valueRange,
        value = value,
        steps = steps,
        onValueChange = onValueChange,
        modifier = modifier,
        startThumb = {},
        endThumb = {},
        track = { state ->
            RivoSliderTrack(
                state = state,
                startThumbValue = startThumbValue,
                endThumbValue = endThumbValue,
                startInteractionSource = startInteractionSource,
                endInteractionSource = endInteractionSource
            )
        },
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource
    )
}

@Composable
private fun RivoSliderTrack(
    state: RangeSliderState,
    startThumbValue: () -> String?,
    endThumbValue: () -> String?,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var width by remember { mutableIntStateOf(0) }
    Box(
        Modifier
            .clearAndSetSemantics { }
            .fillMaxWidth()
            .onSizeChanged { width = it.width },
    ) {
        SliderDefaults.Track(
            rangeSliderState = state,
        )
        RivoSliderThumb(
            value = { startThumbValue().orEmpty() },
            color = colors.thumbColor,
            totalWidth = width,
            fraction = ((state.activeRangeStart - state.valueRange.start) / (state.valueRange.endInclusive - state.valueRange.start)),
            interactionSource = startInteractionSource,
            startRounded = true
        )

        RivoSliderThumb(
            value = { endThumbValue().orEmpty() },
            color = colors.thumbColor,
            totalWidth = width,
            fraction = ((state.activeRangeEnd - state.valueRange.start) / (state.valueRange.endInclusive - state.valueRange.start)),
            interactionSource = endInteractionSource,
            startRounded = false
        )
    }
}

@Composable
private fun RivoSliderThumb(
    value: () -> String,
    color: Color,
    totalWidth: Int,
    fraction: Float,
    startRounded: Boolean,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val localDensity = LocalDensity.current
    val isDragged by interactionSource.collectIsDraggedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isDragged) 36.dp else Dp.Zero,
        label = "ThumbOffset"
    )
    val scaleFactor by animateFloatAsState(
        targetValue = if (isDragged) 1.40f else 1f,
        label = "ThumbScaleFactor"
    )

    val cornerRadiusPx = with(localDensity) { CornerRadiusSmall.toPx() }
    val endCornerRadius by animateFloatAsState(
        targetValue = if (isDragged) cornerRadiusPx else 50f,
        label = "EndCornerRadius"
    )
    var thumbWidth by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .zIndex(10f)
            .offset {
                IntOffset(
                    x = androidx.compose.ui.util.lerp(
                        start = -(thumbWidth / 2),
                        stop = totalWidth - (thumbWidth / 2),
                        fraction = fraction
                    ),
                    y = offsetY
                        .roundToPx()
                        .unaryMinus(),
                )
            }
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .drawBehind {
                val roundRect = if (startRounded) RoundRect(
                    rect = Rect(Offset(0f, 0f), size),
                    topLeft = CornerRadius(endCornerRadius),
                    topRight = CornerRadius(cornerRadiusPx),
                    bottomLeft = CornerRadius(endCornerRadius),
                    bottomRight = CornerRadius(cornerRadiusPx)
                ) else RoundRect(
                    rect = Rect(Offset(0f, 0f), size),
                    topLeft = CornerRadius(cornerRadiusPx),
                    topRight = CornerRadius(endCornerRadius),
                    bottomLeft = CornerRadius(cornerRadiusPx),
                    bottomRight = CornerRadius(endCornerRadius)
                )

                RivoSliderUtil.thumbPath.addRoundRect(roundRect)
                drawPath(path = RivoSliderUtil.thumbPath, color = color)
                drawPath(
                    path = RivoSliderUtil.thumbPath,
                    color = color
                        .contentColor()
                        .copy(alpha = ContentAlpha.PERCENT_32),
                    style = Stroke(BorderWidthStandard.toPx())
                )
                RivoSliderUtil.thumbPath.rewind()
            }
            .padding(MaterialTheme.spacing.small)
            .onSizeChanged { thumbWidth = it.width }
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value(),
            style = MaterialTheme.typography.labelSmall,
            color = color.contentColor()
        )
    }
}

object RivoSliderUtil {
    val thumbPath = Path()
}