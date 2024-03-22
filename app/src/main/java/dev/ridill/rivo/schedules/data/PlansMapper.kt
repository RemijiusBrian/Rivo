package dev.ridill.rivo.schedules.data

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.schedules.data.local.entity.SchedulePlanEntity
import dev.ridill.rivo.schedules.data.local.views.PlanAndAmountsView
import dev.ridill.rivo.schedules.domain.model.PlanInput
import dev.ridill.rivo.schedules.domain.model.PlanListItem

fun PlanAndAmountsView.toPlan(): PlanListItem = PlanListItem(
    id = planId,
    name = planName,
    color = Color(planColorCode),
    createdTimestamp = planCreatedTimestamp,
    totalAmount = totalLinkedAmount,
    paidAmount = paidAmount
)

fun PlanInput.toEntity(): SchedulePlanEntity = SchedulePlanEntity(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp
)