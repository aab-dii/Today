package com.today.app.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.CustomList
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomListsViewModel(private val diaryUseCase: DiaryUseCase) : ViewModel() {

    val lists: StateFlow<List<CustomList>> = diaryUseCase.getCustomLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch { diaryUseCase.createCustomList(name, description) }
    }

    fun delete(listId: Long) {
        viewModelScope.launch { diaryUseCase.deleteCustomList(listId) }
    }
}
