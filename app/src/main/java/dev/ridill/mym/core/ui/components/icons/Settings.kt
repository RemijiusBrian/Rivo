package dev.ridill.mym.core.ui.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path

val Icons.Outlined.RivoSettings: ImageVector
    get() {
        if (_settings != null) {
            return _settings!!
        }
        _settings = materialIcon("Outlined.RivoSettings") {
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
                moveTo(12f, 8f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = false, 4f, 4f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 8f)
                close()
                moveToRelative(0f, 6f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 2f, -2f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 14f)
                close()
            }
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
                moveTo(21.294f, 13.9f)
                lineToRelative(-0.444f, -0.256f)
                arcToRelative(
                    9.1f,
                    9.1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -3.29f
                )
                lineToRelative(0.444f, -0.256f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, -3f, -5.2f)
                lineToRelative(-0.445f, 0.257f)
                arcTo(
                    8.977f,
                    8.977f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    15f,
                    3.513f
                )
                verticalLineTo(3f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9f, 3f)
                verticalLineToRelative(0.513f)
                arcTo(
                    8.977f,
                    8.977f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    6.152f,
                    5.159f
                )
                lineTo(5.705f, 4.9f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3f, 5.2f)
                lineToRelative(0.444f, 0.256f)
                arcToRelative(
                    9.1f,
                    9.1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    3.29f
                )
                lineToRelative(-0.444f, 0.256f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 3f, 5.2f)
                lineToRelative(0.445f, -0.257f)
                arcTo(
                    8.977f,
                    8.977f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    9f,
                    20.487f
                )
                verticalLineTo(21f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 0f)
                verticalLineToRelative(-0.513f)
                arcToRelative(
                    8.977f,
                    8.977f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    2.848f,
                    -1.646f
                )
                lineToRelative(0.447f, 0.258f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3f, -5.2f)
                close()
                moveToRelative(-2.548f, -3.776f)
                arcToRelative(
                    7.048f,
                    7.048f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0f,
                    3.75f
                )
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.464f,
                    1.133f
                )
                lineToRelative(1.084f, 0.626f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 1.733f)
                lineToRelative(-1.086f, -0.628f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.215f,
                    0.165f
                )
                arcToRelative(
                    6.984f,
                    6.984f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -3.243f,
                    1.875f
                )
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.751f,
                    0.969f
                )
                verticalLineTo(21f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 0f)
                verticalLineTo(19.748f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.751f,
                    -0.969f
                )
                arcTo(
                    6.984f,
                    6.984f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    7.006f,
                    16.9f
                )
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.215f,
                    -0.165f
                )
                lineToRelative(-1.084f, 0.627f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, -1f, -1.732f)
                lineToRelative(1.084f, -0.626f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.464f,
                    -1.133f
                )
                arcToRelative(
                    7.048f,
                    7.048f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0f,
                    -3.75f
                )
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.79f, 8.992f)
                lineTo(3.706f, 8.366f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -1.733f)
                lineToRelative(1.086f, 0.628f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.006f, 7.1f)
                arcToRelative(
                    6.984f,
                    6.984f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    3.243f,
                    -1.875f
                )
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11f, 4.252f)
                verticalLineTo(3f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 0f)
                verticalLineTo(4.252f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.751f,
                    0.969f
                )
                arcTo(
                    6.984f,
                    6.984f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    16.994f,
                    7.1f
                )
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.215f,
                    0.165f
                )
                lineToRelative(1.084f, -0.627f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 1f, 1.732f)
                lineToRelative(-1.084f, 0.626f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.746f, 10.125f)
                close()
            }
        }
        return _settings!!
    }

private var _settings: ImageVector? = null