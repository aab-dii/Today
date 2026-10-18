package com.today.app.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CustomListDetailViewModel(private val diaryUseCase: DiaryUseCase) : ViewModel() {

    private val listId = MutableStateFlow(0L)

    val items: StateFlow<List<CustomListItem>> = listId
        .flatMapLatest { id ->
            if (id == 0L) flowOf(emptyList()) else diaryUseCase.getCustomListItems(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val name: StateFlow<String> = diaryUseCase.getCustomLists()
        .map { lists -> lists.firstOrNull { it.id == listId.value }?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun load(id: Long) {
        listId.value = id
    }

    fun remove(item: CustomListItem) {
        val id = listId.value
        if (id == 0L) return
        viewModelScope.launch {
            diaryUseCase.removeTitleFromCustomList(id, item.titleId, item.mediaType)
        }
    }
}
