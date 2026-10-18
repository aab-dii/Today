package com.today.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.CatalogRow
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.core.domain.usecase.CatalogUseCase
import com.today.app.core.domain.usecase.DiaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DAY_MILLIS = 86_400_000L

data class ContinueItem(
    val entry: DiaryEntry,
    val totalEpisodes: Int?
)

data class WeeklySummary(
    val titleCount: Int,
    val entryCount: Int,
    val hours: Int
)

class HomeViewModel(
    private val diaryUseCase: DiaryUseCase,
    private val catalogUseCase: CatalogUseCase
) : ViewModel() {

    val entries: StateFlow<List<DiaryEntry>> = diaryUseCase.getDiaryEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val watching: StateFlow<List<DiaryEntry>> =
        diaryUseCase.getEntriesByStatus(WatchStatus.WATCHING)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val watchlist: StateFlow<List<WatchlistItem>> = diaryUseCase.getWatchlist()
        .map { it.take(12) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weekly: StateFlow<WeeklySummary> = entries
        .map { list ->
            val weekAgo = System.currentTimeMillis() - 7L * DAY_MILLIS
            val recent = list.filter { it.watchedOn >= weekAgo }
            val titles = recent.map { "${it.mediaType.name}-${it.titleId}" }.distinct().size
            val minutes = recent.sumOf { it.runtimeMinutes ?: 100 }
            WeeklySummary(
                titleCount = titles,
                entryCount = recent.size,
                hours = (minutes + 30) / 60
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklySummary(0, 0, 0))

    private val totals = MutableStateFlow<Map<Int, Int?>>(emptyMap())

    val continueWatching: StateFlow<List<ContinueItem>> =
        combine(watching, totals) { list, totalsMap ->
            list.map { ContinueItem(it, totalsMap[it.titleId]) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val throwback: StateFlow<DiaryEntry?> = entries
        .map { list ->
            val now = System.currentTimeMillis()
            list
                .filter { entry ->
                    val age = now - entry.watchedOn
                    age in 330L * DAY_MILLIS..430L * DAY_MILLIS
                }
                .maxByOrNull { it.rating }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _trending = MutableStateFlow<List<Title>>(emptyList())
    val trending: StateFlow<List<Title>> = _trending.asStateFlow()

    private val _recommendations = MutableStateFlow<List<Title>>(emptyList())
    val recommendations: StateFlow<List<Title>> = _recommendations.asStateFlow()

    init {
        loadRow(CatalogRow.TRENDING) { _trending.value = it }

        viewModelScope.launch {
            watching.collect { list ->
                list.forEach { entry ->
                    if (!totals.value.containsKey(entry.titleId)) {
                        val total = runCatching {
                            catalogUseCase.getTitleDetail(entry.mediaType, entry.titleId).totalEpisodes
                        }.getOrNull()
                        totals.value = totals.value + (entry.titleId to total)
                    }
                }
            }
        }

        viewModelScope.launch {
            var loaded = false
            entries.collect { list ->
                if (loaded) return@collect
                if (list.isEmpty()) return@collect
                loaded = true
                val row = recommendationRow(list)
                val titles = runCatching { catalogUseCase.getCatalogRow(row) }.getOrDefault(emptyList())
                _recommendations.value = titles
            }
        }
    }

    private fun loadRow(row: CatalogRow, onLoaded: (List<Title>) -> Unit) {
        viewModelScope.launch {
            onLoaded(runCatching { catalogUseCase.getCatalogRow(row) }.getOrDefault(emptyList()))
        }
    }

    private fun recommendationRow(list: List<DiaryEntry>): CatalogRow {
        val topCountry = list
            .map { it.country.trim() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return when (topCountry) {
                "KR", "South Korea" -> CatalogRow.KOREAN_DRAMA
                "JP", "Japan" -> CatalogRow.ANIME
                "ID", "Indonesia" -> CatalogRow.INDONESIAN_MOVIE
                else -> CatalogRow.TOP_RATED
            }
    }

    fun bumpEpisode(entry: DiaryEntry) {
        viewModelScope.launch {
            val next = (entry.episodeNumber ?: 0) + 1
            diaryUseCase.saveDiaryEntry(entry.copy(episodeNumber = next))
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { diaryUseCase.deleteDiaryEntry(id) }
    }
}
