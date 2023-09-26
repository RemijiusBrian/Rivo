package dev.ridill.rivo.transactionGroups.presentation.groupDetails

interface TxGroupDetailsActions {
    fun onEditClick()
    fun onEditDismiss()
    fun onEditConfirm()
    fun onNameChange(value: String)
    fun onExclusionToggle(excluded: Boolean)
}