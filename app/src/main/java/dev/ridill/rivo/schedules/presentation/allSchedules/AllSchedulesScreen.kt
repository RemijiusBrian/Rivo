package dev.ridill.rivo.schedules.presentation.allSchedules

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.CancelButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.PermissionRationaleDialog
import dev.ridill.rivo.core.ui.components.PermissionState
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.AllSchedulesScreenSpec
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel

@Composable
fun AllSchedulesScreen(
    context: Context = LocalContext.current,
    snackbarController: SnackbarController,
    notificationPermissionState: PermissionState?,
    state: AllSchedulesState,
    allSchedulesPagingItems: LazyPagingItems<ScheduleListItemUiModel>,
    actions: AllSchedulesActions,
    navigateUp: () -> Unit,
    navigateToAddEditSchedule: (Long?) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isNotificationPermissionGranted by remember(notificationPermissionState) {
        derivedStateOf { notificationPermissionState?.isGranted != false }
    }

    BackHandler(
        enabled = state.multiSelectionModeActive,
        onBack = actions::onMultiSelectionModeDismiss
    )

    RivoScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.multiSelectionModeActive) stringResource(
                            R.string.count_selected,
                            state.selectedScheduleIds.size
                        )
                        else stringResource(AllSchedulesScreenSpec.labelRes)
                    )
                },
                navigationIcon = {
                    if (state.multiSelectionModeActive) {
                        CancelButton(onClick = actions::onMultiSelectionModeDismiss)
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
                actions = {
                    if (state.multiSelectionModeActive) {
                        IconButton(onClick = actions::onDeleteSelectedSchedulesClick) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_selected_schedules)
                            )
                        }
                    }
                    if (!state.multiSelectionModeActive && !isNotificationPermissionGranted) {
                        NotificationPermissionWarning(
                            onClick = actions::onNotificationWarningClick
                        )
                    }
                }
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddEditSchedule(null) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_new_schedule_fab)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            if (allSchedulesPagingItems.isEmpty()) {
                item(
                    key = "EmptyListIndicator",
                    contentType = "EmptyListIndicator"
                ) {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyListIndicator(
                            rawResId = R.raw.lottie_empty_list_ghost,
                            messageRes = R.string.schedules_empty_message
                        )
                    }
                }
            }
            repeat(allSchedulesPagingItems.itemCount) { index ->
                allSchedulesPagingItems[index]?.let { item ->
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
                                val selected by remember(state.selectedScheduleIds) {
                                    derivedStateOf { item.scheduleItem.id in state.selectedScheduleIds }
                                }
                                ScheduleListItemCard(
                                    amount = item.scheduleItem.amountFormatted(state.currency),
                                    note = item.scheduleItem.note,
                                    nextReminderDate = item.scheduleItem.nextReminderDateFormatted,
                                    lastPaymentTimestamp = item.scheduleItem.lastPaymentDateFormatted,
                                    onClick = { navigateToAddEditSchedule(item.scheduleItem.id) },
                                    onMarkPaidClick = { actions.onMarkSchedulePaidClick(item.scheduleItem.id) },
                                    canMarkPaid = item.scheduleItem.canMarkPaid,
                                    modifier = Modifier
                                        .padding(horizontal = SpacingSmall)
                                        .animateItemPlacement(),
                                    selectionModeActive = state.multiSelectionModeActive,
                                    selected = selected,
                                    onLongPress = { actions.onScheduleLongPress(item.scheduleItem.id) },
                                    onSelectionToggle = { actions.onScheduleSelectionToggle(item.scheduleItem.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.showDeleteSelectedSchedulesConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_selected_schedules_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteSelectedSchedulesConfirm,
                onDismiss = actions::onDeleteSelectedSchedulesDismiss
            )
        }

        if (state.showNotificationRationale) {
            PermissionRationaleDialog(
                icon = Icons.Outlined.Notifications,
                rationaleText = stringResource(
                    R.string.permission_rationale_notification,
                    stringResource(R.string.app_name)
                ),
                onDismiss = actions::onNotificationRationaleDismiss,
                onSettingsClick = actions::onNotificationRationaleAgree
            )
        }
    }
}

@Composable
private fun NotificationPermissionWarning(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Rounded.NotificationsOff,
            contentDescription = stringResource(R.string.cd_notification_off_warning),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ScheduleListItemCard(
    selectionModeActive: Boolean,
    selected: Boolean,
    amount: String,
    note: String?,
    nextReminderDate: String?,
    lastPaymentTimestamp: String?,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onSelectionToggle: () -> Unit,
    canMarkPaid: Boolean,
    onMarkPaidClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clickModifier = remember(selectionModeActive) {
        if (selectionModeActive) Modifier
            .clickable(
                onClick = onSelectionToggle
            )
        else Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    }

    val contentDescriptionText = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextReminderDate.orEmpty()
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface
            .copy(alpha = ContentAlpha.PERCENT_32)
        else Color.Transparent,
        label = "CardBorderColor"
    )

    Card(
        modifier = modifier
            .mergedContentDescription(contentDescriptionText)
            .then(clickModifier),
        border = BorderStroke(
            width = BorderWidthStandard,
            color = borderColor
        )
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
                modifier = Modifier
                    .clip(CardDefaults.shape),
                tonalElevation = ElevationLevel1
            )
            HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
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

                if (!selectionModeActive && canMarkPaid) {
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
            onMarkPaidClick = {},
            modifier = Modifier
                .fillMaxWidth(),
            nextReminderDate = "Tomorrow",
            onClick = {},
            lastPaymentTimestamp = null,
            canMarkPaid = true,
            selectionModeActive = false,
            onLongPress = {},
            onSelectionToggle = {},
            selected = true
        )
    }
}