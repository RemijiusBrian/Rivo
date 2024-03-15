package dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NextPlan
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.SchedulesAndPlansListScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleListItemUiModel

@Composable
fun SchedulesAndPlansListScreen(
    context: Context = LocalContext.current,
    snackbarController: SnackbarController,
    schedules: LazyPagingItems<ScheduleListItemUiModel>,
    actions: SchedulesAndPlansActions,
    navigateUp: () -> Unit,
    navigateToAddEditSchedule: (Long?) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        // TODO: Navigate to create plan
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NextPlan,
                        contentDescription = stringResource(R.string.cd_new_plan_fab)
                    )
                }
                FloatingActionButton(onClick = { navigateToAddEditSchedule(null) }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outline_schedule),
                        contentDescription = stringResource(R.string.cd_new_schedule_fab)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            if (schedules.isEmpty()) {
                item(
                    key = "EmptyListIndicator",
                    contentType = "EmptyListIndicator"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyListIndicator(
                            rawResId = R.raw.lottie_empty_list_ghost,
                            messageRes = R.string.schedules_empty_message
                        )
                    }
                }
            }
            repeat(schedules.itemCount) { index ->
                schedules[index]?.let { item ->
                    when (item) {
                        is ScheduleListItemUiModel.TypeSeparator -> {
                            stickyHeader(
                                key = item.label.asString(context),
                                contentType = "TypeSeparator"
                            ) {
                                ListSeparator(
                                    label = item.label.asString(),
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
                                    nextReminderDate = item.scheduleItem.nextReminderDateFormatted,
                                    lastPaymentTimestamp = item.scheduleItem.lastPaymentDateFormatted,
                                    onClick = { navigateToAddEditSchedule(item.scheduleItem.id) },
                                    onMarkPaidClick = { actions.onMarkSchedulePaidClick(item.scheduleItem.id) },
                                    canMarkPaid = item.scheduleItem.canMarkPaid,
                                    modifier = Modifier
                                        .padding(horizontal = SpacingSmall)
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
    nextReminderDate: String?,
    lastPaymentTimestamp: String?,
    onClick: () -> Unit,
    canMarkPaid: Boolean,
    onMarkPaidClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    /*if (isRetired) {
                        Text(stringResource(R.string.retired))
                    }*/
                }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(horizontal = SpacingSmall)
            ) {
                lastPaymentTimestamp?.let {
                    Text(
                        text = stringResource(R.string.last_paid_date, it),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(SpacingExtraSmall)
                            .weight(Float.One)
                    )
                }
                if (canMarkPaid) {
                    TextButton(onClick = onMarkPaidClick) {
                        Text(stringResource(R.string.mark_paid))
                    }
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
//            status = TxScheduleStatus.DUE,
            onMarkPaidClick = {},
            modifier = Modifier
                .fillMaxWidth(),
            nextReminderDate = "Tomorrow",
            onClick = {},
            lastPaymentTimestamp = null,
            canMarkPaid = true
        )
    }
}