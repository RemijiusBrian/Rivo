package dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails

interface TxFolderDetailsActions {
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteFolderOnlyClick()
    fun onDeleteFolderAndTransactionsClick()
    fun onEditClick()
    fun onEditDismiss()
    fun onEditConfirm()
    fun onNameChange(value: String)
    fun onExclusionToggle(excluded: Boolean)
}