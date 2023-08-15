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

val Icons.Rounded.ArithmeticSymbols: ImageVector
    get() {
        if (_arithmeticSymbols != null) {
            return _arithmeticSymbols!!
        }
        _arithmeticSymbols = materialIcon("Rounded.ArithmeticSymbols") {
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
                moveTo(24f, 12f)
                curveToRelative(0f, 0.553f, -0.448f, 1f, -1f, 1f)
                horizontalLineToRelative(-10f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 0.553f, -0.448f, 1f, -1f, 1f)
                reflectiveCurveToRelative(-1f, -0.447f, -1f, -1f)
                verticalLineToRelative(-10f)
                horizontalLineTo(1f)
                curveToRelative(-0.552f, 0f, -1f, -0.447f, -1f, -1f)
                reflectiveCurveToRelative(0.448f, -1f, 1f, -1f)
                horizontalLineToRelative(10f)
                verticalLineTo(1f)
                curveToRelative(0f, -0.553f, 0.448f, -1f, 1f, -1f)
                reflectiveCurveToRelative(1f, 0.447f, 1f, 1f)
                verticalLineToRelative(10f)
                horizontalLineToRelative(10f)
                curveToRelative(0.552f, 0f, 1f, 0.447f, 1f, 1f)
                close()
                moveToRelative(-1f, 4f)
                horizontalLineToRelative(-6f)
                curveToRelative(-0.552f, 0f, -1f, 0.447f, -1f, 1f)
                reflectiveCurveToRelative(0.448f, 1f, 1f, 1f)
                horizontalLineToRelative(6f)
                curveToRelative(0.552f, 0f, 1f, -0.447f, 1f, -1f)
                reflectiveCurveToRelative(-0.448f, -1f, -1f, -1f)
                close()
                moveToRelative(0f, 4f)
                horizontalLineToRelative(-6f)
                curveToRelative(-0.552f, 0f, -1f, 0.447f, -1f, 1f)
                reflectiveCurveToRelative(0.448f, 1f, 1f, 1f)
                horizontalLineToRelative(6f)
                curveToRelative(0.552f, 0f, 1f, -0.447f, 1f, -1f)
                reflectiveCurveToRelative(-0.448f, -1f, -1f, -1f)
                close()
                moveTo(1f, 6f)
                horizontalLineToRelative(6f)
                curveToRelative(0.552f, 0f, 1f, -0.447f, 1f, -1f)
                reflectiveCurveToRelative(-0.448f, -1f, -1f, -1f)
                horizontalLineTo(1f)
                curveToRelative(-0.552f, 0f, -1f, 0.447f, -1f, 1f)
                reflectiveCurveToRelative(0.448f, 1f, 1f, 1f)
                close()
                moveToRelative(16f, 0f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                curveToRelative(0f, 0.553f, 0.448f, 1f, 1f, 1f)
                reflectiveCurveToRelative(1f, -0.447f, 1f, -1f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                curveToRelative(0.552f, 0f, 1f, -0.447f, 1f, -1f)
                reflectiveCurveToRelative(-0.448f, -1f, -1f, -1f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-2f)
                curveToRelative(0f, -0.553f, -0.448f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.447f, -1f, 1f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(-2f)
                curveToRelative(-0.552f, 0f, -1f, 0.447f, -1f, 1f)
                reflectiveCurveToRelative(0.448f, 1f, 1f, 1f)
                close()
                moveToRelative(-9.293f, 10.293f)
                curveToRelative(-0.391f, -0.391f, -1.023f, -0.391f, -1.414f, 0f)
                lineToRelative(-1.793f, 1.793f)
                lineToRelative(-1.793f, -1.793f)
                curveToRelative(-0.391f, -0.391f, -1.023f, -0.391f, -1.414f, 0f)
                reflectiveCurveToRelative(-0.391f, 1.023f, 0f, 1.414f)
                lineToRelative(1.793f, 1.793f)
                lineToRelative(-1.793f, 1.793f)
                curveToRelative(-0.391f, 0.391f, -0.391f, 1.023f, 0f, 1.414f)
                curveToRelative(0.195f, 0.195f, 0.451f, 0.293f, 0.707f, 0.293f)
                reflectiveCurveToRelative(0.512f, -0.098f, 0.707f, -0.293f)
                lineToRelative(1.793f, -1.793f)
                lineToRelative(1.793f, 1.793f)
                curveToRelative(0.195f, 0.195f, 0.451f, 0.293f, 0.707f, 0.293f)
                reflectiveCurveToRelative(0.512f, -0.098f, 0.707f, -0.293f)
                curveToRelative(0.391f, -0.391f, 0.391f, -1.023f, 0f, -1.414f)
                lineToRelative(-1.793f, -1.793f)
                lineToRelative(1.793f, -1.793f)
                curveToRelative(0.391f, -0.391f, 0.391f, -1.023f, 0f, -1.414f)
                close()
            }
        }
        return _arithmeticSymbols!!
    }

private var _arithmeticSymbols: ImageVector? = null