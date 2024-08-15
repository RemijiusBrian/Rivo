package dev.ridill.rivo.tags.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.ExcludedIcon
import dev.ridill.rivo.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.rivo.core.ui.components.icons.Tags
import dev.ridill.rivo.core.ui.theme.IconSizeMedium
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer

@Composable
fun TagListItem(
    name: String,
    color: Color,
    excluded: Boolean,
    createdTimestamp: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = {
            Text(
                text = stringResource(
                    R.string.created_colon_timestamp_value,
                    createdTimestamp
                )
            )
        },
        leadingContent = {
            ListItemLeadingContentContainer {
                Icon(
                    imageVector = Icons.Rounded.Tags,
                    contentDescription = null,
                    tint = color
                )
            }
        },
        trailingContent = {
            if (excluded) {
                ExcludedIcon(
                    modifier = Modifier
                        .size(IconSizeMedium)
                )
            }
        },
        modifier = modifier
            .exclusionGraphicsLayer(excluded),
    )
}