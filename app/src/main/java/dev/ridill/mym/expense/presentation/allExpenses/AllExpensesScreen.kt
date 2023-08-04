package dev.ridill.mym.expense.presentation.allExpenses

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.TextFormatter
import dev.ridill.mym.core.ui.components.BackArrowButton
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesDestination
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.SpacingListEnd
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.core.ui.util.contentColor
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import java.time.LocalDate
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            TagsInfoList(
                tags = state.tagsWithExpenditures,
                selectedTagId = state.selectedTag,
                onTagClick = actions::onTagClick,
                onNewTagClick = actions::onNewTagClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.20f)
            )

            DateFilter(
                selectedDate = state.selectedDate,
                onMonthSelect = actions::onMonthSelect,
                yearsList = state.yearsList,
                onYearSelect = actions::onYearSelect
            )
        }
    }
}

@Composable
private fun TagsInfoList(
    tags: List<TagWithExpenditure>,
    selectedTagId: String?,
    onTagClick: (String) -> Unit,
    onNewTagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = SpacingMedium,
            end = SpacingListEnd
        ),
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        items(items = tags) { item ->
            TagInfoCard(
                name = item.tag.name,
                color = item.tag.color,
                amount = TextFormatter.currency(item.expenditure),
                percentOfTotalExpenditure = item.percentOfTotalExpenditure,
                isSelected = item.tag.name == selectedTagId,
                onClick = { onTagClick(item.tag.name) },
                modifier = Modifier
                    .fillParentMaxHeight()
                    .animateItemPlacement()
            )
        }

        item(key = "NewTagCard") {
            NewTagCard(
                onClick = onNewTagClick,
                modifier = Modifier
                    .fillParentMaxHeight()
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
private fun TagInfoCard(
    name: String,
    color: Color,
    amount: String,
    percentOfTotalExpenditure: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isSelected, label = "IsSelectedTransition")
    val percent by animateFloatAsState(
        targetValue = percentOfTotalExpenditure,
        label = "AnimatedPercentOfTotal"
    )
    val widthFactor by transition.animateFloat(
        label = "TagInfoCardWidthFactor",
        targetValueByState = { if (it) 2f else 1f }
    )
    val textStyleAnimFactor by transition.animateFloat(
        label = "TextStyleAnimationFactor",
        targetValueByState = { if (it) 1f else 0f }
    )
    val selectedTextSize = MaterialTheme.typography.headlineMedium.fontSize
    val unselectedTextSize = MaterialTheme.typography.titleLarge.fontSize
    val titleTextSize by remember(textStyleAnimFactor) {
        derivedStateOf {
            lerp(unselectedTextSize, selectedTextSize, textStyleAnimFactor)
        }
    }
    val contentColor = remember(color) { color.contentColor() }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(TagInfoCardWidth * widthFactor)
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge
                    .copy(
                        fontSize = titleTextSize,
                        fontWeight = FontWeight.SemiBold
                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = stringResource(R.string.amount_worth_spent, amount),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = stringResource(R.string.percent_of_total, TextFormatter.percent(percent)),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }
    }
}

@Composable
private fun NewTagCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(TagInfoCardWidth)
            .clip(CardDefaults.shape)
            .clickable(
                role = Role.Button,
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_create_new_tag)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null
        )
    }
}

private val TagInfoCardWidth = 120.dp


@Composable
private fun DateFilter(
    selectedDate: LocalDate,
    onMonthSelect: (Month) -> Unit,
    yearsList: List<Int>,
    onYearSelect: (Int) -> Unit,
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
                date = selectedDate,
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
                        selected = year == selectedDate.year,
                        onClick = { onYearSelect(year) },
                        label = { Text(year.toString()) },
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
                    selected = month == selectedDate.month,
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
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_clock),
                contentDescription = "",
                modifier = Modifier
                    .size(FloatingActionButtonDefaults.LargeIconSize)
            )
            AnimatedContent(
                targetState = date,
                label = "AnimatedDateText"
            ) { date ->
                Column {
                    Text(
                        text = date.month.getDisplayName(
                            TextStyle.FULL_STANDALONE,
                            Locale.getDefault()
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = date.year.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}