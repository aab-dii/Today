package com.today.app.ui.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.YearlyRecap
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class RecapViewModel(diaryUseCase: DiaryUseCase) : ViewModel() {

    val years: StateFlow<List<Int>> = diaryUseCase.getRecapYears()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    val recap: StateFlow<YearlyRecap?> = combine(years, _selectedYear) { available, chosen ->
        chosen?.takeIf { it in available } ?: available.firstOrNull()
    }
        .distinctUntilChanged()
        .flatMapLatest { year ->
            if (year == null) flowOf(null) else diaryUseCase.getRecap(year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }
}
