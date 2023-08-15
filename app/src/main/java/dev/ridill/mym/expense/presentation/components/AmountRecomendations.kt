package dev.ridill.mym.expense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.ridill.mym.core.domain.util.TextFormatUtil
import dev.ridill.mym.core.ui.theme.SpacingSmall

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
                label = { Text(TextFormatUtil.currency(amount)) }
            )
        }
    }
}

@Composable
fun BudgetRecommendationsRow(
    recommendations: List<Long>,
    onRecommendationClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(SpacingSmall)
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        items(recommendations) { amount ->
            SuggestionChip(
                onClick = { onRecommendationClick(amount) },
                label = { Text(TextFormatUtil.currency(amount)) },
                modifier = Modifier
                    .animateItemPlacement()
            )
        }
    }
}