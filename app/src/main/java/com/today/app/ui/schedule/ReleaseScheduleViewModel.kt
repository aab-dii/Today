package com.today.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.UpcomingRelease
import com.today.app.core.domain.usecase.ReleaseScheduleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class ReleaseScheduleUiState(
    val loading: Boolean = true,
    val releases: List<UpcomingRelease> = emptyList(),
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseScheduleViewModel(
    private val useCase: ReleaseScheduleUseCase
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)
    private val _state = MutableStateFlow(ReleaseScheduleUiState())
    val state: StateFlow<ReleaseScheduleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            refreshTrigger
                .flatMapLatest { useCase.getUpcomingReleases() }
                .catch { e ->
                    _state.value = ReleaseScheduleUiState(
                        loading = false,
                        error = e.localizedMessage ?: "Gagal memuat jadwal rilis."
                    )
                }
                .collect { releases ->
                    _state.value = ReleaseScheduleUiState(loading = false, releases = releases)
                }
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        refreshTrigger.value++
    }
}
