package dev.ridill.rivo.schedules.presentation.scheduleDashboard

import android.content.Context
import android.icu.util.Currency
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.CancelButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.EmptyListIndicator
import dev.ridill.rivo.core.ui.components.HorizontalColorSelectionList
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.navigation.destinations.SchedulesDashboardScreenSpec
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.ElevationLevel1
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingExtraSmall
import dev.ridill.rivo.core.ui.theme.SpacingListEnd
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.SpacingSmall
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.schedules.domain.model.PlanListItem
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel

@Composable
fun SchedulesDashboardScreen(
    context: Context = LocalContext.current,
    snackbarController: SnackbarController,
    state: SchedulesDashboardState,
    schedules: LazyPagingItems<ScheduleListItemUiModel>,
    plansList: LazyPagingItems<PlanListItem>,
    planInputName: () -> String,
    planInputColorCode: () -> Int?,
    isNewPlan: Boolean?,
    actions: SchedulesDashboardActions,
    navigateUp: () -> Unit,
    navigateToAddEditSchedule: (Long?) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
                        else stringResource(SchedulesDashboardScreenSpec.labelRes)
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
                }
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddEditSchedule(null) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_schedule),
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
            item(
                key = "PlansList",
                contentType = "PlansList"
            ) {
                PlansList(
                    selectionModeActive = state.multiSelectionModeActive,
                    plansList = plansList,
                    currency = state.currency,
                    onPlanClick = actions::onPlanClick,
                    onNewPlanClick = actions::onNewPlanClick,
                    modifier = Modifier
                        .animateItemPlacement(),
                    lazyRowModifier = Modifier
                        .heightIn(min = PlansListMinHeight)
                        .fillParentMaxWidth()
                )
            }

            stickyHeader(
                key = "SchedulesListLabel",
                contentType = "SchedulesListLabel"
            ) {
                Column(
                    modifier = Modifier
                        .animateItemPlacement()
                ) {
                    ListLabel(
                        text = stringResource(R.string.schedules),
                        modifier = Modifier
                            .padding(horizontal = SpacingMedium)
                    )
                    SpacerSmall()
                    HorizontalDivider()
                }
            }

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

        if (state.showPlanInput) {
            PlanInputSheet(
                isNewPlan = isNewPlan,
                name = planInputName,
                onNameChange = actions::onPlanInputNameChange,
                colorCode = planInputColorCode,
                onColorSelect = actions::onPlanInputColorChange,
                onDismiss = actions::onPlanInputDismiss,
                onConfirm = actions::onPlanInputConfirm,
                onDelete = actions::onDeleteActivePlanClick
            )
        }

        if (state.showDeletePlanConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_plan_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeletePlanConfirm,
                onDismiss = actions::onDeletePlanDismiss
            )
        }

        if (state.showDeleteSelectedSchedulesConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_selected_schedules_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteSelectedSchedulesConfirm,
                onDismiss = actions::onDeleteSelectedSchedulesDismiss
            )
        }
    }
}

@Composable
private fun PlansList(
    currency: Currency,
    selectionModeActive: Boolean,
    plansList: LazyPagingItems<PlanListItem>,
    onPlanClick: (PlanListItem) -> Unit,
    onNewPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
    lazyRowModifier: Modifier = Modifier
) {
    val isListEmpty by remember(plansList) {
        derivedStateOf { plansList.isEmpty() }
    }
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ListLabel(
                text = stringResource(R.string.plans),
                modifier = Modifier
                    .padding(horizontal = SpacingMedium),
            )
            SpacerSmall()
            TextButton(onClick = onNewPlanClick) {
                Text(stringResource(R.string.create_new_plan))
                Spacer(spacing = ButtonDefaults.IconSpacing)
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                )
            }
        }

        Box(
            modifier = lazyRowModifier,
            contentAlignment = Alignment.Center
        ) {
            if (isListEmpty) {
                Text(
                    text = stringResource(R.string.plans_list_empty_message),
                    color = LocalContentColor.current
                        .copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = SpacingSmall,
                    bottom = SpacingSmall,
                    start = SpacingMedium,
                    end = SpacingListEnd
                ),
                modifier = Modifier
                    .matchParentSize(),
                verticalArrangement = Arrangement.spacedBy(SpacingSmall),
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                items(
                    count = plansList.itemCount,
                    key = plansList.itemKey { it.id },
                    contentType = plansList.itemContentType { "PlanCard" }
                ) { index ->
                    plansList[index]?.let { plan ->
                        PlanListItemCard(
                            name = plan.name,
                            color = plan.color,
                            onClick = { onPlanClick(plan) },
                            linkedAmount = plan.totalAmountFormatted(currency),
                            paymentProgress = plan.completionPercent,
                            modifier = Modifier
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = selectionModeActive) {
            Text(
                text = stringResource(R.string.tap_plan_to_assign_selected_schedules),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = SpacingMedium),
                color = LocalContentColor.current
                    .copy(alpha = 0.80f)
            )
        }
    }
}

private val PlansListMinHeight = 200.dp

@Composable
private fun PlanListItemCard(
    name: String,
    color: Color,
    linkedAmount: String,
    paymentProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {

    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = contentModifier
                .fillMaxHeight()
                .widthIn(min = PlanCardMinWidth)
                .padding(
                    vertical = SpacingSmall,
                    horizontal = SpacingMedium
                )
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = linkedAmount,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(weight = 1f)

            Column {
                if (!paymentProgress.isNaN()) {
                    Text(
                        text = stringResource(
                            R.string.percent_complete,
                            TextFormat.percent(
                                value = paymentProgress,
                                maxFractionDigits = Int.Zero
                            )
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current
                            .copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                }
                LinearProgressIndicator(
                    progress = { paymentProgress },
                    modifier = Modifier
                        .width(PlanCardMinWidth),
                    strokeCap = StrokeCap.Round,
                    trackColor = color,
                    color = color
                )
            }
        }
    }
}

private val PlanCardMinWidth = 140.dp

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

@Composable
private fun PlanInputSheet(
    isNewPlan: Boolean?,
    name: () -> String,
    onNameChange: (String) -> Unit,
    colorCode: () -> Int?,
    onColorSelect: (Color) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextFieldSheet(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            ) {
                Text(
                    text = stringResource(
                        id = if (isNewPlan != false) R.string.new_plan
                        else R.string.edit_plan
                    )
                )

                if (isNewPlan == false) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.cd_delete_plan)
                        )
                    }
                }
            }
        },
        inputValue = name,
        onValueChange = onNameChange,
        onDismiss = onDismiss,
        actionButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        contentAfterTextField = {
            HorizontalColorSelectionList(
                onColorSelect = onColorSelect,
                selectedColorCode = colorCode
            )
        },
        modifier = modifier,
        label = stringResource(R.string.plan_name_label),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        )
    )
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
            canMarkPaid = true,
            selectionModeActive = false,
            onLongPress = {},
            onSelectionToggle = {},
            selected = true
        )
    }
}