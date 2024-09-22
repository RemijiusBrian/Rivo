package dev.ridill.rivo.tags.presentation.addEditTag

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTagSheetSpec
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTagViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: TagsRepository,
    private val eventBus: EventBus<AddEditTagEvent>
) : ViewModel(), AddEditTagActions {

    private val tagIdArg = AddEditTagSheetSpec.getTagIdFromSavedStateHandle(savedStateHandle)
    private val _isLoading = MutableStateFlow(false)
    val isLoading get() = _isLoading.asStateFlow()

    val tagInput = savedStateHandle.getStateFlow<Tag>(TAG_INPUT, Tag.NEW)
    val tagInputError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    val showTagDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TAG_CONFIRMATION, false)

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        val tag = repo.getTagById(tagIdArg) ?: Tag.NEW
        savedStateHandle[TAG_INPUT] = tag
    }

    override fun onNameChange(value: String) {
        savedStateHandle[TAG_INPUT] = tagInput.value.copy(name = value)
        savedStateHandle[NEW_TAG_ERROR] = null
    }

    override fun onColorSelect(color: Color) {
        savedStateHandle[TAG_INPUT] = tagInput.value.copy(colorCode = color.toArgb())
    }

    override fun onExclusionChange(excluded: Boolean) {
        savedStateHandle[TAG_INPUT] = tagInput.value.copy(excluded = excluded)
    }

    override fun onConfirm() {
        viewModelScope.launch {
            val tagInput = tagInput.value
            val name = tagInput.name.trim()
            if (name.isEmpty()) {
                savedStateHandle[NEW_TAG_ERROR] = UiText.StringResource(
                    R.string.error_invalid_tag_name,
                    isErrorText = true
                )
                return@launch
            }
            _isLoading.update { true }
            val colorCode = tagInput.colorCode
            val savedId = repo.saveTag(
                name = name,
                colorCode = colorCode,
                id = tagInput.id,
                timestamp = tagInput.createdTimestamp,
                excluded = tagInput.excluded
            )
            _isLoading.update { false }
            eventBus.send(AddEditTagEvent.TagSaved(savedId))
        }
    }

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = true
    }

    override fun onDeleteTagDismiss() {
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
    }

    override fun onDeleteTagConfirm() {
        viewModelScope.launch {
            repo.deleteTagById(tagIdArg)
            savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
            eventBus.send(AddEditTagEvent.TagDeleted)
        }
    }

    sealed interface AddEditTagEvent {
        data class TagSaved(val tagId: Long) : AddEditTagEvent
        data object TagDeleted : AddEditTagEvent
    }
}

private const val TAG_INPUT = "TAG_INPUT"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"
private const val SHOW_DELETE_TAG_CONFIRMATION = "SHOW_DELETE_TAG_CONFIRMATION"