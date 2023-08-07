package dev.ridill.mym.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LargeTitle(
    title: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        text = title,
        style = MaterialTheme.typography.displayMedium,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}