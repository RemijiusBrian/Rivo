package dev.ridill.rivo.transactionGroups.data

import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndAggregateAmount
import dev.ridill.rivo.transactionGroups.domain.model.TxGroup
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupListItem

fun GroupAndAggregateAmount.toTxGroupListItem(): TxGroupListItem = TxGroupListItem(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    aggregateAmount = aggregateAmount
)

fun TransactionGroupEntity.toTxGroup(): TxGroup = TxGroup(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)