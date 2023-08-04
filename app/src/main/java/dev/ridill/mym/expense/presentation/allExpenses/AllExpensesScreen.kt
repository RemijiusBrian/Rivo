package dev.ridill.mym.expense.presentation.allExpenses

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesDestination
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.util.contentColor
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AllExpensesScreen(
    snackbarHostState: SnackbarHostState,
    state: AllExpensesState,
    actions: AllExpensesActions,
    navigateUp: () -> Unit
) {
    MYMScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(AllExpensesDestination.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TagsInfoList(
                tags = state.tagsWithExpenditures,
                modifier = Modifier
                    .fillMaxWidth()
            )

            DateFilter(
                selectedMonth = state.selectedMonth,
                onMonthSelect = actions::onMonthSelect,
                yearsList = state.yearsList,
                selectedYear = state.selectedYear,
                onYearSelect = actions::onYearSelect
            )
        }
    }
}

@Composable
private fun TagsInfoList(
    tags: List<TagWithExpenditure>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
    ) {
        items(items = tags) { item ->
            TagWithExpenditureCard(
                name = item.tag.name,
                color = item.tag.color,
                percentOfTotalExpenditure = item.percentOfTotalExpenditure,
                isSelected = false,
                onClick = {},
                modifier = Modifier
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
private fun TagWithExpenditureCard(
    name: String,
    color: Color,
    percentOfTotalExpenditure: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percent by animateFloatAsState(
        targetValue = percentOfTotalExpenditure,
        label = "AnimatedPercentOfTotal"
    )
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = color.contentColor()
        )
    ) {
        Column {
            Text(text = name)
            Text(text = percent.toString())
        }
    }
}

@Composable
private fun DateFilter(
    selectedMonth: Month,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<String>,
    selectedYear: String,
    onYearSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthsList = remember { Month.values() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateIndicator(
                month = selectedMonth,
                year = selectedYear,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
                contentPadding = PaddingValues(
                    start = SpacingMedium,
                    end = SpacingListEnd
                )
            ) {
                items(items = yearsList, key = { it }) { year ->
                    ElevatedFilterChip(
                        selected = selectedYear == year,
                        onClick = { onYearSelect(year) },
                        label = { Text(year) },
                        modifier = Modifier
                            .animateItemPlacement()
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
            contentPadding = PaddingValues(
                start = SpacingMedium,
                end = SpacingListEnd
            )
        ) {
            items(items = monthsList, key = { it.value }) { month ->
                ElevatedFilterChip(
                    selected = month == selectedMonth,
                    onClick = { onMonthSelect(month) },
                    label = {
                        Text(
                            text = month.getDisplayName(
                                TextStyle.FULL_STANDALONE,
                                Locale.getDefault()
                            ),
                            modifier = Modifier
                                .animateItemPlacement()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DateIndicator(
    month: Month,
    year: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_clock),
            contentDescription = ""
        )
        Column {
            Text(
                text = month.getDisplayName(
                    TextStyle.FULL_STANDALONE,
                    Locale.getDefault()
                ),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = year,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}