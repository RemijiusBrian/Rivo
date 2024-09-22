package dev.ridill.rivo.folders.presentation.addEditFolder

interface AddEditFolderActions {
    fun onNameChange(value: String)
    fun onExclusionChange(excluded: Boolean)
    fun onConfirm()
}