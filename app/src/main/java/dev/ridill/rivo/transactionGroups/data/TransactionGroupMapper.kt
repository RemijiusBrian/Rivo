package dev.ridill.rivo.transactionGroups.data

import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndAggregateAmount
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupDetails

fun GroupAndAggregateAmount.toTxGroupDetails(): TxGroupDetails = TxGroupDetails(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = excluded,
    aggregateAmount = aggregateAmount
)