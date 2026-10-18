package com.today.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.today.app.core.domain.model.CatalogRow
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.usecase.CatalogUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogSection(
    val row: CatalogRow,
    val titles: List<Title>
)

sealed interface CatalogRowsState {
    data object Loading : CatalogRowsState
    data class Ready(val sections: List<CatalogSection>) : CatalogRowsState
    data class Failed(val message: String) : CatalogRowsState
}

class CatalogViewModel(private val catalogUseCase: CatalogUseCase) : ViewModel() {

    val titles: Flow<PagingData<Title>> = catalogUseCase.getTitles().cachedIn(viewModelScope)

    private val _rowsState = MutableStateFlow<CatalogRowsState>(CatalogRowsState.Loading)
    val rowsState: StateFlow<CatalogRowsState> = _rowsState.asStateFlow()

    init {
        loadRows()
    }

    fun loadRows() {
        viewModelScope.launch {
            _rowsState.value = CatalogRowsState.Loading

            val sections = CatalogRow.entries
                .map { row ->
                    async { row to runCatching { catalogUseCase.getCatalogRow(row) } }
                }
                .awaitAll()
                .mapNotNull { (row, result) ->
                    result.getOrNull()
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { CatalogSection(row, it) }
                }

            _rowsState.value = if (sections.isEmpty()) {
                CatalogRowsState.Failed("Gagal memuat katalog")
            } else {
                CatalogRowsState.Ready(sections)
            }
        }
    }
}
