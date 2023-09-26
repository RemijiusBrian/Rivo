package dev.ridill.rivo.settings.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.HorizontalSpacer
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall

@Composable
fun SimpleSettingsPreference(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) = BasicPreference(
    titleContent = { Text(text = stringResource(titleRes)) },
    summaryContent = summary?.let {
        { Text(text = it) }
    },
    leadingIcon = leadingIcon?.let {
        {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier
                    .size(PreferenceIconSize)
            )
        }
    },
    modifier = Modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) Modifier
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                )
            else Modifier
        )
        .then(modifier),
    contentPadding = contentPadding
)

@Composable
fun SimplePreference(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) = BasicPreference(
    titleContent = { Text(text = stringResource(titleRes)) },
    summaryContent = summary?.let {
        { Text(text = it) }
    },
    leadingIcon = leadingIcon,
    modifier = Modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) Modifier
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                )
            else Modifier
        )
        .then(modifier),
    contentPadding = contentPadding
)

@Composable
fun SwitchPreference(
    @StringRes titleRes: Int,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) {
    BasicPreference(
        titleContent = { Text(text = stringResource(titleRes)) },
        summaryContent = summary?.let {
            { Text(text = it) }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(PreferenceIconSize)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = value,
                role = Role.Switch,
                onValueChange = onValueChange
            )
            .then(modifier),
        contentPadding = contentPadding,
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = onValueChange,
                modifier = Modifier
                    .clearAndSetSemantics {}
            )
        }
    )
}

@Composable
fun BasicPreference(
    titleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    summaryContent: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PreferenceContentPadding,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
    summaryTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    contentColor: Color = LocalContentColor.current,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    Row(
        modifier = modifier
            .padding(contentPadding),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        leadingIcon?.invoke()
        Column(
            modifier = Modifier
                .weight(Float.One)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides titleTextStyle,
                LocalContentColor provides contentColor
            ) {
                titleContent()
            }
            summaryContent?.let { content ->
                CompositionLocalProvider(
                    LocalTextStyle provides summaryTextStyle,
                    LocalContentColor provides contentColor.copy(alpha = 0.64f)
                ) {
                    content()
                }
            }
        }
        trailingContent?.invoke()
    }
}

@Composable
fun EmptyIconSpacer() = HorizontalSpacer(spacing = PreferenceIconSize)

val PreferenceIconSize = 24.dp
val PreferenceContentPadding = PaddingValues(
    horizontal = SpacingMedium,
    vertical = SpacingSmall
)