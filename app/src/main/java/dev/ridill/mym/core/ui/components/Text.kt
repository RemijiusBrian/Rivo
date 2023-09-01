package dev.ridill.mym.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.ridill.mym.core.domain.util.One

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

@Composable
fun ListLabel(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.SemiBold,
    maxLines: Int = Int.One,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow
    )
}