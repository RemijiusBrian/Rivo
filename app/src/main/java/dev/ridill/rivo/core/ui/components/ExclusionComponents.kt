package dev.ridill.rivo.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun ExclusionIcon(
    excluded: Boolean,
    modifier: Modifier = Modifier
) = Icon(
    imageVector = ImageVector.vectorResource(
        id = if (excluded) R.drawable.ic_rounded_exclude
        else R.drawable.ic_rounded_include
    ),
    contentDescription = null,
    modifier = modifier
)

@Composable
fun ExcludedIcon(
    modifier: Modifier = Modifier,
    size: Dp = DefaultIndicatorSize,
    tint: Color = LocalContentColor.current
) = Icon(
    imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_exclude),
    contentDescription = stringResource(R.string.cd_excluded),
    modifier = Modifier
        .size(size)
        .then(modifier),
    tint = tint
)

private val DefaultIndicatorSize = 16.dp

@Composable
fun MarkExcludedSwitch(
    excluded: Boolean,
    onToggle: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val toggleableModifier = if (onToggle != null) Modifier
        .toggleable(
            value = excluded,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = true,
            role = Role.Switch,
            onValueChange = onToggle
        )
    else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = toggleableModifier
            .then(modifier)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_exclude),
            contentDescription = null,
            modifier = Modifier
                .size(SwitchDefaults.IconSize)
        )
        Text(
            text = stringResource(R.string.mark_excluded_question),
            style = style
        )
        Switch(
            checked = excluded,
            onCheckedChange = null
        )
    }
}