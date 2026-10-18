package com.today.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportExportUiState(
    val entryCount: Int = 0,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ImportExportViewModel(
    private val diaryUseCase: DiaryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ImportExportUiState())
    val state: StateFlow<ImportExportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            diaryUseCase.getDiaryEntries().collect { entries ->
                _state.update { it.copy(entryCount = entries.size) }
            }
        }
    }

    suspend fun exportCsv(): String = diaryUseCase.exportDiaryCsv()

    fun importCsv(csv: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null, error = null) }
            runCatching { diaryUseCase.importDiaryCsv(csv) }
                .onSuccess { count ->
                    _state.update {
                        it.copy(
                            busy = false,
                            message = "$count catatan diimpor" +
                                if (count == 0) " (tidak ada baris valid)" else ""
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(busy = false, error = friendly(e)) }
                }
        }
    }

    fun onExported(count: Int) {
        _state.update { it.copy(message = "$count catatan diekspor") }
    }

    fun onError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun clearMessage() = _state.update { it.copy(message = null, error = null) }

    private fun friendly(e: Throwable): String =
        e.localizedMessage ?: e.message ?: "Terjadi kesalahan. Coba lagi."
}
