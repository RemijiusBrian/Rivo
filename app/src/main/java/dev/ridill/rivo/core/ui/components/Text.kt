package dev.ridill.rivo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.util.Currency

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
fun TitleMediumText(
    title: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textDecoration: TextDecoration? = null,
    color: Color = Color.Unspecified,
) = Text(
    text = title,
    style = MaterialTheme.typography.titleMedium,
    modifier = modifier,
    maxLines = maxLines,
    overflow = overflow,
    fontWeight = fontWeight,
    textDecoration = textDecoration,
    color = color
)

@Composable
fun BodyLargeText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = LocalContentColor.current
) = Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    maxLines = maxLines,
    overflow = overflow,
    modifier = modifier,
    color = color
)

@Composable
fun BodyMediumText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    textDecoration: TextDecoration? = null
) = Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    maxLines = maxLines,
    overflow = overflow,
    modifier = modifier,
    color = color,
    textAlign = textAlign,
    textDecoration = textDecoration
)

@Composable
fun LabelLargeText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    textDecoration: TextDecoration? = null
) = Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    maxLines = maxLines,
    overflow = overflow,
    modifier = modifier,
    color = color,
    textAlign = textAlign,
    textDecoration = textDecoration
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

@Composable
fun AmountWithArrow(
    value: Double,
    currency: Currency,
    modifier: Modifier = Modifier,
    showTypeIndicator: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium
) {
    val type = remember(value) {
        when {
            value > 0.0 -> TransactionType.DEBIT
            value < 0.0 -> TransactionType.CREDIT
            else -> null
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = TextFormat.currency(value, currency),
            style = textStyle,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
        )
        if (showTypeIndicator && type != null) {
            Icon(
                imageVector = ImageVector.vectorResource(type.iconRes),
                contentDescription = stringResource(type.labelRes),
                modifier = Modifier
                    .size(TypeIndicatorSize)
            )
        }
    }
}

@Composable
fun AmountWithArrow(
    value: String,
    type: TransactionType?,
    modifier: Modifier = Modifier,
    showTypeIndicator: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
        )
        if (showTypeIndicator && type != null) {
            Icon(
                imageVector = ImageVector.vectorResource(type.iconRes),
                contentDescription = stringResource(type.labelRes),
                modifier = Modifier
                    .size(TypeIndicatorSize)
            )
        }
    }
}

private val TypeIndicatorSize = 16.dp