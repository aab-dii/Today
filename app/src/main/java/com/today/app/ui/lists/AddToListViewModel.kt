package com.today.app.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.CustomList
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddToListViewModel(private val diaryUseCase: DiaryUseCase) : ViewModel() {

    val lists: StateFlow<List<CustomList>> = diaryUseCase.getCustomLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selected = MutableStateFlow<Set<Long>>(emptySet())
    val selected: StateFlow<Set<Long>> = _selected.asStateFlow()

    fun load(titleId: Int, mediaType: com.today.app.core.domain.model.MediaType) {
        viewModelScope.launch {
            _selected.value = diaryUseCase.getCustomListIdsForTitle(titleId, mediaType).toSet()
        }
    }

    fun toggle(listId: Long) {
        _selected.value = if (_selected.value.contains(listId)) {
            _selected.value - listId
        } else {
            _selected.value + listId
        }
    }

    fun confirm(item: CustomListItem, newListName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val current = lists.value
            val selectedIds = _selected.value
            current.forEach { list ->
                if (selectedIds.contains(list.id)) {
                    diaryUseCase.addTitleToCustomList(list.id, item)
                } else {
                    diaryUseCase.removeTitleFromCustomList(list.id, item.titleId, item.mediaType)
                }
            }
            if (newListName.isNotBlank()) {
                val newId = diaryUseCase.createCustomList(newListName, "")
                diaryUseCase.addTitleToCustomList(newId, item)
            }
            onDone()
        }
    }
}
