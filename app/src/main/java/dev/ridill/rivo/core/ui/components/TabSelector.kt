package dev.ridill.rivo.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.ContentAlpha

@Composable
fun <T> TabSelector(
    values: () -> List<T>,
    selectedItem: () -> T?,
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    shape: CornerBasedShape = CircleShape,
    divider: @Composable () -> Unit = {},
    itemContent: @Composable (T) -> Unit
) {
    val updatedValues by rememberUpdatedState(newValue = values)
    val updatedSelectedItem by rememberUpdatedState(newValue = selectedItem)
    val selectedIndex by remember(updatedValues, updatedSelectedItem) {
        derivedStateOf { values().indexOf(updatedSelectedItem()) }
    }
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .clip(shape)
            .border(
                width = BorderWidthStandard,
                color = indicatorColor,
                shape = shape
            ),
        indicator = { tabPositions ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedIndex])
                    .fillMaxSize()
                    .drawWithCache {
                        val color = indicatorColor
                            .copy(alpha = ContentAlpha.PERCENT_32)
                        onDrawBehind {
                            drawRoundRect(color)
                        }
                    }
            )
        },
        divider = divider,
        tabs = { values().forEach { itemContent(it) } }
    )
}

@Composable
fun TabSelectorItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = Tab(
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    text = text,
    icon = icon,
    selectedContentColor = selectedContentColor,
    unselectedContentColor = unselectedContentColor,
    interactionSource = interactionSource
)