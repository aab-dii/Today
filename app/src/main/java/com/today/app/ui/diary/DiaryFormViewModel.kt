package com.today.app.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.usecase.CatalogUseCase
import com.today.app.core.domain.usecase.DiaryUseCase
import com.today.app.core.utils.GenreCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiaryFormState(
    val id: Long = 0,
    val titleId: Int = 0,
    val mediaType: MediaType = MediaType.MOVIE,
    val titleName: String = "",
    val posterPath: String? = null,
    val watchedOn: Long = System.currentTimeMillis(),
    val rating: Double = 0.0,
    val mood: Mood? = null,
    val note: String = "",
    val watchedWith: String = "",
    val status: WatchStatus = WatchStatus.COMPLETED,
    val seasonNumber: String = "",
    val episodeNumber: String = "",
    val rewatchOf: Long? = null,
    val genreIds: String = "",
    val country: String = "",
    val runtimeMinutes: Int? = null,
    val isEditing: Boolean = false,
    val isLoaded: Boolean = false
)

class DiaryFormViewModel(
    private val diaryUseCase: DiaryUseCase,
    private val catalogUseCase: CatalogUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DiaryFormState())
    val state: StateFlow<DiaryFormState> = _state.asStateFlow()

    private var started = false

    fun start(title: Title?, diary: DiaryEntry?, rewatchOf: Long?) {
        if (started) return
        started = true

        _state.value = when {
            diary != null -> {
                val isRewatch = rewatchOf != null
                DiaryFormState(
                    id = if (isRewatch) 0 else diary.id,
                    titleId = diary.titleId,
                    mediaType = diary.mediaType,
                    titleName = diary.titleName,
                    posterPath = diary.posterPath,
                    watchedOn = if (isRewatch) System.currentTimeMillis() else diary.watchedOn,
                    rating = if (isRewatch) 0.0 else diary.rating,
                    mood = if (isRewatch) null else diary.mood,
                    note = if (isRewatch) "" else diary.note,
                    watchedWith = if (isRewatch) "" else diary.watchedWith.orEmpty(),
                    status = if (isRewatch) WatchStatus.COMPLETED else diary.status,
                    seasonNumber = diary.seasonNumber?.toString().orEmpty(),
                    episodeNumber = diary.episodeNumber?.toString().orEmpty(),
                    rewatchOf = rewatchOf,
                    genreIds = diary.genreIds,
                    country = diary.country,
                    runtimeMinutes = diary.runtimeMinutes,
                    isEditing = !isRewatch,
                    isLoaded = true
                )
            }

            title != null -> DiaryFormState(
                titleId = title.id,
                mediaType = title.mediaType,
                titleName = title.name,
                posterPath = title.posterPath,
                genreIds = title.genre,
                country = title.country,
                isLoaded = true
            )

            else -> DiaryFormState(isLoaded = true)
        }
    }

    fun setRating(rating: Double) = update { it.copy(rating = rating) }

    fun setMood(mood: Mood?) = update { it.copy(mood = mood) }

    fun setStatus(status: WatchStatus) = update { it.copy(status = status) }

    fun setNote(note: String) = update { it.copy(note = note) }

    fun setWatchedWith(value: String) = update { it.copy(watchedWith = value) }

    fun setWatchedOn(millis: Long) = update { it.copy(watchedOn = millis) }

    fun setSeason(value: String) = update { it.copy(seasonNumber = value.filter(Char::isDigit)) }

    fun setEpisode(value: String) = update { it.copy(episodeNumber = value.filter(Char::isDigit)) }

    fun save(onDone: () -> Unit) {
        val current = _state.value
        if (current.titleId == 0 && !current.isEditing) return
        viewModelScope.launch {
            val enriched = enrich(current)
            diaryUseCase.saveDiaryEntry(
                DiaryEntry(
                    id = enriched.id,
                    titleId = enriched.titleId,
                    mediaType = enriched.mediaType,
                    titleName = enriched.titleName,
                    posterPath = enriched.posterPath,
                    watchedOn = enriched.watchedOn,
                    rating = enriched.rating,
                    mood = enriched.mood,
                    note = enriched.note,
                    watchedWith = enriched.watchedWith.ifBlank { null },
                    status = enriched.status,
                    seasonNumber = enriched.seasonNumber.toIntOrNull(),
                    episodeNumber = enriched.episodeNumber.toIntOrNull(),
                    rewatchOf = enriched.rewatchOf,
                    genreIds = enriched.genreIds,
                    country = enriched.country,
                    runtimeMinutes = enriched.runtimeMinutes
                )
            )
            onDone()
        }
    }

    private suspend fun enrich(state: DiaryFormState): DiaryFormState {
        if (state.titleId == 0) return state
        if (state.runtimeMinutes != null && state.genreIds.isNotBlank()) return state
        return try {
            val detail = catalogUseCase.getTitleDetail(state.mediaType, state.titleId)
            state.copy(
                runtimeMinutes = state.runtimeMinutes ?: detail.runtimeMinutes,
                genreIds = state.genreIds.ifBlank { GenreCatalog.idsCsvFromNames(detail.genreNames) }
            )
        } catch (e: Exception) {
            state
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = _state.value.id
        if (id == 0L) {
            onDone()
            return
        }
        viewModelScope.launch {
            diaryUseCase.deleteDiaryEntry(id)
            onDone()
        }
    }

    private inline fun update(transform: (DiaryFormState) -> DiaryFormState) {
        _state.value = transform(_state.value)
    }
}
