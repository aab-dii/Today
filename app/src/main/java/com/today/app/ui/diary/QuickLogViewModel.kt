package com.today.app.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.usecase.CatalogUseCase
import com.today.app.core.domain.usecase.DiaryUseCase
import com.today.app.core.utils.GenreCatalog
import kotlinx.coroutines.launch

class QuickLogViewModel(
    private val diaryUseCase: DiaryUseCase,
    private val catalogUseCase: CatalogUseCase
) : ViewModel() {

    fun save(title: Title, rating: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                titleId = title.id,
                mediaType = title.mediaType,
                titleName = title.name,
                posterPath = title.posterPath,
                watchedOn = System.currentTimeMillis(),
                rating = rating,
                status = WatchStatus.COMPLETED,
                genreIds = title.genre,
                country = title.country
            )
            val id = diaryUseCase.saveDiaryEntry(entry)
            onDone()
            enrich(id, entry, title)
        }
    }

    private suspend fun enrich(id: Long, entry: DiaryEntry, title: Title) {
        try {
            val detail = catalogUseCase.getTitleDetail(title.mediaType, title.id)
            val runtime = entry.runtimeMinutes ?: detail.runtimeMinutes
            val genre = entry.genreIds.ifBlank { GenreCatalog.idsCsvFromNames(detail.genreNames) }
            if (runtime != entry.runtimeMinutes || genre != entry.genreIds) {
                diaryUseCase.saveDiaryEntry(entry.copy(id = id, runtimeMinutes = runtime, genreIds = genre))
            }
        } catch (e: Exception) {
            // Perkaya bersifat best-effort; abaikan bila detail gagal dimuat (offline).
        }
    }
}
