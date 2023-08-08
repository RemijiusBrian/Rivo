package dev.ridill.mym.core.ui.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import dev.ridill.mym.core.ui.util.MaterialIconDimension

val Icons.Rounded.Feedback: ImageVector
    get() {
        if (_feedback != null) {
            return _feedback!!
        }
        _feedback = ImageVector.Builder(
            name = "Rounded.Feedback",
            defaultWidth = MaterialIconDimension.dp,
            defaultHeight = MaterialIconDimension.dp,
            viewportWidth = MaterialIconDimension,
            viewportHeight = MaterialIconDimension
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20f, 0f)
                horizontalLineTo(4f)
                curveTo(1.79f, 0f, 0f, 1.79f, 0f, 4f)
                verticalLineToRelative(12f)
                curveToRelative(0f, 2.21f, 1.79f, 4f, 4f, 4f)
                horizontalLineToRelative(2.92f)
                lineToRelative(3.75f, 3.16f)
                curveToRelative(0.38f, 0.34f, 0.86f, 0.51f, 1.34f, 0.51f)
                reflectiveCurveToRelative(0.93f, -0.16f, 1.29f, -0.49f)
                lineToRelative(3.85f, -3.18f)
                horizontalLineToRelative(2.85f)
                curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f)
                verticalLineTo(4f)
                curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f)
                close()
                moveToRelative(-2.83f, 9.62f)
                lineToRelative(-2.17f, 1.77f)
                lineToRelative(0.9f, 2.73f)
                curveToRelative(0.12f, 0.37f, 0f, 0.78f, -0.31f, 1.01f)
                curveToRelative(-0.31f, 0.24f, -0.73f, 0.25f, -1.06f, 0.04f)
                lineToRelative(-2.52f, -1.64f)
                lineToRelative(-2.48f, 1.66f)
                curveToRelative(-0.15f, 0.1f, -0.33f, 0.15f, -0.51f, 0.15f)
                curveToRelative(-0.19f, 0f, -0.39f, -0.06f, -0.55f, -0.18f)
                curveToRelative(-0.31f, -0.23f, -0.44f, -0.64f, -0.32f, -1.01f)
                lineToRelative(0.86f, -2.76f)
                lineToRelative(-2.18f, -1.77f)
                curveToRelative(-0.29f, -0.25f, -0.4f, -0.65f, -0.27f, -1.01f)
                curveToRelative(0.13f, -0.36f, 0.48f, -0.6f, 0.86f, -0.6f)
                horizontalLineToRelative(2.75f)
                lineToRelative(0.97f, -2.61f)
                curveToRelative(0.13f, -0.36f, 0.48f, -0.6f, 0.86f, -0.6f)
                reflectiveCurveToRelative(0.73f, 0.24f, 0.86f, 0.6f)
                lineToRelative(0.97f, 2.61f)
                horizontalLineToRelative(2.75f)
                curveToRelative(0.38f, 0f, 0.73f, 0.24f, 0.86f, 0.6f)
                curveToRelative(0.13f, 0.36f, 0.02f, 0.77f, -0.27f, 1.02f)
                close()
            }
        }.build()
        return _feedback!!
    }

private var _feedback: ImageVector? = null