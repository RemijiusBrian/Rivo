package dev.ridill.rivo.schedules.data.local.views

import androidx.room.DatabaseView
import java.time.LocalDateTime

@DatabaseView(
    value = """
        SELECT pln.id AS planId,
            pln.name AS planName,
            pln.color_code AS planColorCode,
            pln.created_timestamp AS planCreatedTimestamp,
            SUM(IFNULL(sch.amount, 0.0)) AS totalLinkedAmount,
            SUM(IFNULL(tx.amount, 0.0)) AS paidAmount
        FROM schedule_plan_table pln
        LEFT OUTER JOIN schedules_table sch ON pln.id = sch.plan_id
        LEFT OUTER JOIN transaction_table tx ON sch.id = tx.schedule_id
        GROUP BY planId
    """,
    viewName = "plan_and_amounts_view"
)
data class PlanAndAmountsView(
    val planId: Long,
    val planName: String,
    val planColorCode: Int,
    val planCreatedTimestamp: LocalDateTime,
    val totalLinkedAmount: Double,
    val paidAmount: Double
)
