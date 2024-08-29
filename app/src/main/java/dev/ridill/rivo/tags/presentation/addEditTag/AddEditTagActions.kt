package dev.ridill.rivo.tags.presentation.addEditTag

import androidx.compose.ui.graphics.Color

interface AddEditTagActions {
    fun onNameChange(value: String)
    fun onColorSelect(color: Color)
    fun onExclusionChange(excluded: Boolean)
    fun onConfirm()
    fun onDeleteClick()
}