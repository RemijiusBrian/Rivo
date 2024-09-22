package dev.ridill.rivo.folders.presentation.addEditFolder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditFolderSheetSpec
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.repository.AddEditFolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditFolderViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: AddEditFolderRepository,
    private val eventBus: EventBus<AddEditFolderEvent>
) : ViewModel(), AddEditFolderActions {

    private val folderId = AddEditFolderSheetSpec.getFolderIdFromSavedStateHandle(savedStateHandle)
    private val _isLoading = MutableStateFlow(false)
    val isLoading get() = _isLoading.asStateFlow()

    val folderInput = savedStateHandle.getStateFlow(FOLDER_INPUT, Folder.NEW)
    val errorMessage = savedStateHandle.getStateFlow<UiText?>(ERROR_MESSAGE, null)

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        val folder = repo.getFolderDetails(folderId) ?: Folder.NEW
        savedStateHandle[FOLDER_INPUT] = folder
    }

    override fun onNameChange(value: String) {
        savedStateHandle[FOLDER_INPUT] = folderInput.value.copy(name = value)
        savedStateHandle[ERROR_MESSAGE] = null
    }

    override fun onExclusionChange(excluded: Boolean) {
        savedStateHandle[FOLDER_INPUT] = folderInput.value.copy(excluded = excluded)
    }

    override fun onConfirm() {
        viewModelScope.launch {
            val input = folderInput.value
            val name = input.name.trim()
            if (name.isEmpty()) {
                savedStateHandle[ERROR_MESSAGE] = UiText.StringResource(
                    R.string.error_invalid_folder_name,
                    isErrorText = true
                )
                return@launch
            }
            _isLoading.update { true }
            val savedId = repo.saveFolder(
                name = name,
                id = input.id,
                timestamp = input.createdTimestamp,
                excluded = input.excluded
            )
            _isLoading.update { false }
            eventBus.send(AddEditFolderEvent.FolderSaved(savedId))
        }
    }

    sealed interface AddEditFolderEvent {
        data class FolderSaved(val tagId: Long) : AddEditFolderEvent
    }
}

private const val FOLDER_INPUT = "FOLDER_INPUT"
private const val ERROR_MESSAGE = "ERROR_MESSAGE"