package dev.ridill.rivo.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import dev.ridill.rivo.core.domain.util.One

@Composable
fun MediumDisplayText(
    title: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    overflow: TextOverflow = TextOverflow.Ellipsis
) = Text(
    text = title,
    style = MaterialTheme.typography.displayMedium,
    modifier = modifier,
    maxLines = maxLines,
    overflow = overflow
)

@Composable
fun SmallDisplayText(
    title: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    overflow: TextOverflow = TextOverflow.Ellipsis
) = Text(
    text = title,
    style = MaterialTheme.typography.displaySmall,
    modifier = modifier,
    maxLines = maxLines,
    overflow = overflow
)

@Composable
fun TitleLargeText(
    title: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textDecoration: TextDecoration? = null,
    color: Color = Color.Unspecified,
) = Text(
    text = title,
    style = MaterialTheme.typography.titleLarge,
    modifier = modifier,
    maxLines = maxLines,
    overflow = overflow,
    fontWeight = fontWeight,
    textDecoration = textDecoration,
    color = color
)

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis
) = Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    maxLines = maxLines,
    overflow = overflow,
    modifier = modifier
)

@Composable
fun ListLabel(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.SemiBold,
    maxLines: Int = Int.One,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        color = color
    )
}