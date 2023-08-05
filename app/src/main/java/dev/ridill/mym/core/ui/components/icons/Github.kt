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

val Icons.Filled.Github: ImageVector
    get() {
        if (_github != null) {
            return _github!!
        }
        _github = ImageVector.Builder(
            name = "Filled.Github",
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
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(12f, 0.296f)
                    curveToRelative(-6.627f, 0f, -12f, 5.372f, -12f, 12f)
                    curveToRelative(0f, 5.302f, 3.438f, 9.8f, 8.206f, 11.387f)
                    curveToRelative(0.6f, 0.111f, 0.82f, -0.26f, 0.82f, -0.577f)
                    curveToRelative(0f, -0.286f, -0.011f, -1.231f, -0.016f, -2.234f)
                    curveToRelative(-3.338f, 0.726f, -4.043f, -1.416f, -4.043f, -1.416f)
                    curveTo(4.421f, 18.069f, 3.635f, 17.7f, 3.635f, 17.7f)
                    curveToRelative(-1.089f, -0.745f, 0.082f, -0.729f, 0.082f, -0.729f)
                    curveToRelative(1.205f, 0.085f, 1.839f, 1.237f, 1.839f, 1.237f)
                    curveToRelative(1.07f, 1.834f, 2.807f, 1.304f, 3.492f, 0.997f)
                    curveTo(9.156f, 18.429f, 9.467f, 17.9f, 9.81f, 17.6f)
                    curveToRelative(-2.665f, -0.303f, -5.467f, -1.332f, -5.467f, -5.93f)
                    curveToRelative(0f, -1.31f, 0.469f, -2.381f, 1.237f, -3.221f)
                    curveTo(5.455f, 8.146f, 5.044f, 6.926f, 5.696f, 5.273f)
                    curveToRelative(0f, 0f, 1.008f, -0.322f, 3.301f, 1.23f)
                    curveTo(9.954f, 6.237f, 10.98f, 6.104f, 12f, 6.099f)
                    curveToRelative(1.02f, 0.005f, 2.047f, 0.138f, 3.006f, 0.404f)
                    curveToRelative(2.29f, -1.553f, 3.297f, -1.23f, 3.297f, -1.23f)
                    curveToRelative(0.653f, 1.653f, 0.242f, 2.873f, 0.118f, 3.176f)
                    curveToRelative(0.769f, 0.84f, 1.235f, 1.911f, 1.235f, 3.221f)
                    curveToRelative(0f, 4.609f, -2.807f, 5.624f, -5.479f, 5.921f)
                    curveToRelative(0.43f, 0.372f, 0.814f, 1.103f, 0.814f, 2.222f)
                    curveToRelative(0f, 1.606f, -0.014f, 2.898f, -0.014f, 3.293f)
                    curveToRelative(0f, 0.319f, 0.216f, 0.694f, 0.824f, 0.576f)
                    curveToRelative(4.766f, -1.589f, 8.2f, -6.085f, 8.2f, -11.385f)
                    curveTo(24f, 5.669f, 18.627f, 0.296f, 12f, 0.296f)
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
                    moveTo(4.545f, 17.526f)
                    curveToRelative(-0.026f, 0.06f, -0.12f, 0.078f, -0.206f, 0.037f)
                    curveToRelative(-0.087f, -0.039f, -0.136f, -0.121f, -0.108f, -0.18f)
                    curveToRelative(0.026f, -0.061f, 0.12f, -0.078f, 0.207f, -0.037f)
                    curveTo(4.525f, 17.384f, 4.575f, 17.466f, 4.545f, 17.526f)
                    lineTo(4.545f, 17.526f)
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
                    moveTo(5.031f, 18.068f)
                    curveToRelative(-0.057f, 0.053f, -0.169f, 0.028f, -0.245f, -0.055f)
                    curveToRelative(-0.079f, -0.084f, -0.093f, -0.196f, -0.035f, -0.249f)
                    curveToRelative(0.059f, -0.053f, 0.167f, -0.028f, 0.246f, 0.056f)
                    curveTo(5.076f, 17.903f, 5.091f, 18.014f, 5.031f, 18.068f)
                    lineTo(5.031f, 18.068f)
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
                    moveTo(5.504f, 18.759f)
                    curveToRelative(-0.074f, 0.051f, -0.194f, 0.003f, -0.268f, -0.103f)
                    curveToRelative(-0.074f, -0.107f, -0.074f, -0.235f, 0.002f, -0.286f)
                    curveToRelative(0.074f, -0.051f, 0.193f, -0.005f, 0.268f, 0.101f)
                    curveTo(5.579f, 18.579f, 5.579f, 18.707f, 5.504f, 18.759f)
                    lineTo(5.504f, 18.759f)
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
                    moveTo(6.152f, 19.427f)
                    curveToRelative(-0.066f, 0.073f, -0.206f, 0.053f, -0.308f, -0.046f)
                    curveToRelative(-0.105f, -0.097f, -0.134f, -0.234f, -0.068f, -0.307f)
                    curveToRelative(0.067f, -0.073f, 0.208f, -0.052f, 0.311f, 0.046f)
                    curveTo(6.191f, 19.217f, 6.222f, 19.355f, 6.152f, 19.427f)
                    lineTo(6.152f, 19.427f)
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
                    moveTo(7.047f, 19.814f)
                    curveToRelative(-0.029f, 0.094f, -0.164f, 0.137f, -0.3f, 0.097f)
                    curveTo(6.611f, 19.87f, 6.522f, 19.76f, 6.55f, 19.665f)
                    curveToRelative(0.028f, -0.095f, 0.164f, -0.139f, 0.301f, -0.096f)
                    curveTo(6.986f, 19.609f, 7.075f, 19.719f, 7.047f, 19.814f)
                    lineTo(7.047f, 19.814f)
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
                    moveTo(8.029f, 19.886f)
                    curveToRelative(0.003f, 0.099f, -0.112f, 0.181f, -0.255f, 0.183f)
                    curveToRelative(-0.143f, 0.003f, -0.26f, -0.077f, -0.261f, -0.174f)
                    curveToRelative(0f, -0.1f, 0.113f, -0.181f, 0.256f, -0.184f)
                    curveTo(7.912f, 19.708f, 8.029f, 19.788f, 8.029f, 19.886f)
                    lineTo(8.029f, 19.886f)
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
                    moveTo(8.943f, 19.731f)
                    curveToRelative(0.017f, 0.096f, -0.082f, 0.196f, -0.224f, 0.222f)
                    curveToRelative(-0.139f, 0.026f, -0.268f, -0.034f, -0.286f, -0.13f)
                    curveToRelative(-0.017f, -0.099f, 0.084f, -0.198f, 0.223f, -0.224f)
                    curveTo(8.797f, 19.574f, 8.925f, 19.632f, 8.943f, 19.731f)
                    lineTo(8.943f, 19.731f)
                    close()
                }
            }
        }.build()
        return _github!!
    }

private var _github: ImageVector? = null