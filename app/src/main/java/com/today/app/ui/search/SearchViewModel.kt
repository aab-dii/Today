package com.today.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.usecase.CatalogUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class Success(val titles: List<Title>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val catalogUseCase: CatalogUseCase) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<SearchUiState> = _query
        .debounce(DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .mapLatest { raw ->
            val keyword = raw.trim()
            if (keyword.length < MIN_QUERY_LENGTH) {
                SearchUiState.Idle
            } else {
                try {
                    SearchUiState.Success(catalogUseCase.searchTitles(keyword))
                } catch (e: Exception) {
                    SearchUiState.Error(e.localizedMessage ?: "Pencarian gagal")
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Idle
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
        private const val DEBOUNCE_MILLIS = 350L
    }
}
