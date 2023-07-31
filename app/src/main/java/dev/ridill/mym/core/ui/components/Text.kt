package dev.ridill.mym.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall

@Composable
fun PreferenceTitle(
    @StringRes resId: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = SpacingMedium,
        vertical = SpacingSmall
    )
) = Text(
    text = stringResource(resId),
    modifier = Modifier
        .padding(contentPadding)
        .then(modifier),
    style = style,
    fontWeight = fontWeight,
    color = color
)