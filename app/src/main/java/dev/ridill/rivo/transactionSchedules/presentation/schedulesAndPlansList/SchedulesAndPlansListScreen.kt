package dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.SchedulesAndPlansListScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleListItemUiModel
import dev.ridill.rivo.transactionSchedules.domain.model.TxScheduleStatus

@Composable
fun SchedulesAndPlansListScreen(
    snackbarController: SnackbarController,
    schedules: LazyPagingItems<ScheduleListItemUiModel>,
    onScheduleClick: (Long?) -> Unit,
    navigateUp: () -> Unit,
    actions: SchedulesAndPlansActions
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SchedulesAndPlansListScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            repeat(schedules.itemCount) { index ->
                schedules[index]?.let { item ->
                    when (item) {
                        is ScheduleListItemUiModel.DateSeparator -> {
                            stickyHeader(
                                key = item.date,
                                contentType = "DateSeparator"
                            ) {
                                ListSeparator(
                                    label = item.date.format(DateUtil.Formatters.MMMM_yyyy_spaceSep),
                                    modifier = Modifier
                                        .animateItemPlacement()
                                )
                            }
                        }

                        is ScheduleListItemUiModel.ScheduleItem -> {
                            item(
                                key = item.scheduleItem.id,
                                contentType = "ScheduleListItem"
                            ) {
                                ScheduleListItemCard(
                                    amount = item.scheduleItem.amountFormatted(LocaleUtil.defaultCurrency),
                                    note = item.scheduleItem.note,
                                    status = item.scheduleItem.status,
                                    nextReminderDate = item.scheduleItem.nextReminderDate?.toString(),
                                    onClick = { onScheduleClick(item.scheduleItem.id) },
                                    onMarkPaidClick = { actions.onMarkSchedulePaidClick(item.scheduleItem.id) },
                                    modifier = Modifier
                                        .padding(horizontal = SpacingSmall)
                                        .animateItemPlacement()
                                )
                            }
                        }

                        ScheduleListItemUiModel.RetiredSeparator -> {
                            stickyHeader(
                                key = TxScheduleStatus.RETIRED,
                                contentType = TxScheduleStatus.RETIRED
                            ) {
                                ListSeparator(
                                    label = stringResource(R.string.retired),
                                    modifier = Modifier
                                        .animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleListItemCard(
    amount: String,
    note: String?,
    status: TxScheduleStatus,
    nextReminderDate: String?,
    onClick: () -> Unit,
    onMarkPaidClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScheduleRetired by remember(status) {
        derivedStateOf { status == TxScheduleStatus.RETIRED }
    }

    val contentDescriptionText = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextReminderDate.orEmpty()
    )

    OutlinedCard(
        modifier = modifier
            .mergedContentDescription(contentDescriptionText),
        onClick = onClick
    ) {
        Column {
            ListItem(
                headlineContent = {
                    val isNoteNullOrEmpty = remember(note) { note.isNullOrEmpty() }
                    Text(
                        text = note.orEmpty()
                            .ifEmpty { stringResource(R.string.generic_schedule_title) },
                        fontStyle = if (isNoteNullOrEmpty) FontStyle.Italic
                        else null,
                        color = LocalContentColor.current
                            .copy(
                                alpha = if (isNoteNullOrEmpty) ContentAlpha.SUB_CONTENT
                                else Float.One
                            )
                    )
                },
                trailingContent = {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    nextReminderDate?.let { date ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Crossfade(targetState = date, label = "ReminderDate") {
                                    Text(text = it)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                },
                tonalElevation = ElevationLevel1,
                modifier = Modifier
                    .clip(CardDefaults.shape),
                overlineContent = {
                    if (isScheduleRetired) {
                        Text(stringResource(R.string.retired))
                    }
                }
            )
            if (!isScheduleRetired) {
                TextButton(
                    onClick = onMarkPaidClick,
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    Text(stringResource(R.string.mark_paid))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewScheduleListItemCard() {
    RivoTheme {
        ScheduleListItemCard(
            amount = "100",
            note = "Test",
            status = TxScheduleStatus.DUE,
            onMarkPaidClick = {},
            modifier = Modifier
                .fillMaxWidth(),
            nextReminderDate = "Tomorrow",
            onClick = {}
        )
    }
}