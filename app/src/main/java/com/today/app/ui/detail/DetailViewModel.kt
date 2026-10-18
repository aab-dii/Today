package com.today.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.TitleDetail
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.core.domain.usecase.CatalogUseCase
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Ready(val detail: TitleDetail) : DetailUiState
    data class Failed(val message: String) : DetailUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    private val catalogUseCase: CatalogUseCase,
    private val diaryUseCase: DiaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val titleFlow = MutableStateFlow<Title?>(null)
    private var loadedKey: String? = null

    val history: StateFlow<List<DiaryEntry>> = titleFlow
        .flatMapLatest { title ->
            if (title == null) flowOf(emptyList())
            else diaryUseCase.getDiaryEntriesForTitle(title.id, title.mediaType)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val inWatchlist: StateFlow<Boolean> = titleFlow
        .flatMapLatest { title ->
            if (title == null) flowOf(false)
            else diaryUseCase.isInWatchlist(title.id, title.mediaType)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun bind(title: Title) {
        titleFlow.value = title
        load(title.mediaType, title.id)
    }

    private fun load(mediaType: MediaType, id: Int) {
        val key = "${mediaType.value}-$id"
        if (loadedKey == key) return
        loadedKey = key

        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                DetailUiState.Ready(catalogUseCase.getTitleDetail(mediaType, id))
            } catch (e: Exception) {
                DetailUiState.Failed(e.localizedMessage ?: "Gagal memuat detail")
            }
        }
    }

    fun toggleWatchlist(title: Title, currentlyIn: Boolean) {
        viewModelScope.launch {
            if (currentlyIn) {
                diaryUseCase.removeFromWatchlist(title.id, title.mediaType)
            } else {
                diaryUseCase.addToWatchlist(
                    WatchlistItem(
                        titleId = title.id,
                        mediaType = title.mediaType,
                        titleName = title.name,
                        posterPath = title.posterPath,
                        addedOn = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun saveProgress(title: Title, season: Int?, episode: Int?) {
        viewModelScope.launch {
            val detailRuntime = (_uiState.value as? DetailUiState.Ready)?.detail?.runtimeMinutes
            val existing = diaryUseCase.getLatestEntryForTitle(title.id, title.mediaType)
                ?.takeIf { it.status == WatchStatus.WATCHING }
            if (existing != null) {
                diaryUseCase.saveDiaryEntry(
                    existing.copy(
                        seasonNumber = season,
                        episodeNumber = episode,
                        runtimeMinutes = existing.runtimeMinutes ?: detailRuntime
                    )
                )
            } else {
                diaryUseCase.saveDiaryEntry(
                    DiaryEntry(
                        titleId = title.id,
                        mediaType = title.mediaType,
                        titleName = title.name,
                        posterPath = title.posterPath,
                        watchedOn = System.currentTimeMillis(),
                        status = WatchStatus.WATCHING,
                        seasonNumber = season,
                        episodeNumber = episode,
                        genreIds = title.genre,
                        country = title.country,
                        runtimeMinutes = detailRuntime
                    )
                )
            }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { diaryUseCase.deleteDiaryEntry(id) }
    }
}
