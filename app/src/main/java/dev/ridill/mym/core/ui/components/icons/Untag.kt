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

val Icons.Rounded.Untag: ImageVector
    get() {
        if (_untag != null) {
            return _untag!!
        }
        _untag = materialIcon("Rounded.Untag") {
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
                moveTo(19f, 2f)
                horizontalLineToRelative(-9.044f)
                arcToRelative(
                    4.966f,
                    4.966f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -3.946f,
                    1.931f
                )
                lineToRelative(-5.8f, 7.455f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 1.228f)
                lineToRelative(5.8f, 7.455f)
                arcToRelative(
                    4.966f,
                    4.966f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    3.946f,
                    1.931f
                )
                horizontalLineToRelative(9.044f)
                arcToRelative(
                    5.006f,
                    5.006f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    5f,
                    -5f
                )
                verticalLineToRelative(-10f)
                arcToRelative(
                    5.006f,
                    5.006f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -5f,
                    -5f
                )
                close()
                moveToRelative(-1.293f, 12.293f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -1.414f,
                    1.414f
                )
                lineToRelative(-2.293f, -2.293f)
                lineToRelative(-2.293f, 2.293f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -1.414f,
                    -1.414f
                )
                lineToRelative(2.293f, -2.293f)
                lineToRelative(-2.293f, -2.293f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.414f,
                    -1.414f
                )
                lineToRelative(2.293f, 2.293f)
                lineToRelative(2.293f, -2.293f)
                arcToRelative(
                    1f,
                    1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.414f,
                    1.414f
                )
                lineToRelative(-2.293f, 2.293f)
                close()
            }
        }
        return _untag!!
    }

private var _untag: ImageVector? = null