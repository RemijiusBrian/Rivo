package dev.ridill.rivo.transactions.domain.model

sealed class TransactionDirection(
    val value: String
) {
    object Incoming : TransactionDirection("INCOMING") {
        const val NAME = "INCOMING"
    }

    object Outgoing : TransactionDirection("OUTGOING") {
        const val NAME = "OUTGOING"
    }

    companion object {
        fun ofValue(name: String): TransactionDirection? = when (name) {
            Incoming.value -> Incoming
            Outgoing.value -> Outgoing
            else -> null
        }
    }
}