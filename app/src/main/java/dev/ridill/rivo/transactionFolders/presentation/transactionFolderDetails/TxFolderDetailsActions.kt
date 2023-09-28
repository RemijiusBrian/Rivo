package dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails

interface TxFolderDetailsActions {
    fun onDeleteClick()
    fun onDeleteCancel()
    fun onDeleteFolderClick()
    fun onDeleteFolderAndTransactionsClick()
    fun onEditClick()
    fun onEditDismiss()
    fun onEditConfirm()
    fun onNameChange(value: String)
    fun onExclusionToggle(excluded: Boolean)
}