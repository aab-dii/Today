package com.today.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchlistViewModel(private val diaryUseCase: DiaryUseCase) : ViewModel() {

    val items: StateFlow<List<WatchlistItem>> = diaryUseCase.getWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(item: WatchlistItem) {
        viewModelScope.launch {
            diaryUseCase.removeFromWatchlist(item.titleId, item.mediaType)
        }
    }
}
