package dev.ridill.mym.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle

@Composable
fun ListLabel(
    @StringRes resId: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = LocalContentColor.current
) = Text(
    text = stringResource(resId),
    modifier = modifier,
    style = style,
    color = color
)