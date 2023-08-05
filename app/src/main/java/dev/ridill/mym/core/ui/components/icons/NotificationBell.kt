package dev.ridill.mym.core.ui.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import dev.ridill.mym.core.ui.util.MaterialIconDimension

val Icons.Outlined.NotificationBell: ImageVector
    get() {
        if (_notificationBell != null) {
            return _notificationBell!!
        }
        _notificationBell = ImageVector.Builder(
            name = "Outlined.NotificationBell",
            defaultWidth = MaterialIconDimension.dp,
            defaultHeight = MaterialIconDimension.dp,
            viewportWidth = MaterialIconDimension,
            viewportHeight = MaterialIconDimension
        ).apply {
            group {
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
                    moveTo(23.259f, 16.2f)
                    lineToRelative(-2.6f, -9.371f)
                    arcTo(
                        9.321f,
                        9.321f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        2.576f,
                        7.3f
                    )
                    lineTo(0.565f, 16.35f)
                    arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.493f, 20f)
                    horizontalLineTo(7.1f)
                    arcToRelative(
                        5f,
                        5f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        9.8f,
                        0f
                    )
                    horizontalLineToRelative(3.47f)
                    arcToRelative(
                        3f,
                        3f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        2.89f,
                        -3.8f
                    )
                    close()
                    moveTo(12f, 22f)
                    arcToRelative(
                        3f,
                        3f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -2.816f,
                        -2f
                    )
                    horizontalLineToRelative(5.632f)
                    arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
                    close()
                    moveToRelative(9.165f, -4.395f)
                    arcToRelative(
                        0.993f,
                        0.993f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -0.8f,
                        0.395f
                    )
                    horizontalLineTo(3.493f)
                    arcToRelative(
                        1f,
                        1f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -0.976f,
                        -1.217f
                    )
                    lineToRelative(2.011f, -9.05f)
                    arcToRelative(
                        7.321f,
                        7.321f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        14.2f,
                        -0.372f
                    )
                    lineToRelative(2.6f, 9.371f)
                    arcTo(
                        0.993f,
                        0.993f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        21.165f,
                        17.605f
                    )
                    close()
                }
            }
        }.build()
        return _notificationBell!!
    }

private var _notificationBell: ImageVector? = null