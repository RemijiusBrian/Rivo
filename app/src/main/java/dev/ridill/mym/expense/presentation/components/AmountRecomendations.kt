package dev.ridill.mym.expense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.ridill.mym.core.ui.util.TextFormat

@Composable
fun AmountRecommendationsRow(
    recommendations: List<Long>,
    onRecommendationClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        recommendations.forEach { amount ->
            SuggestionChip(
                onClick = { onRecommendationClick(amount) },
                label = { Text(TextFormat.currency(amount)) }
            )
        }
    }
}