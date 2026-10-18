package com.today.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.Stats
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(diaryUseCase: DiaryUseCase) : ViewModel() {

    val stats: StateFlow<Stats> = diaryUseCase.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Stats())
}
