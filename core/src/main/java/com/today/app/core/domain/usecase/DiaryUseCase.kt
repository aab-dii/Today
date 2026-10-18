package com.today.app.core.domain.usecase

import com.today.app.core.domain.model.CustomList
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Stats
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.core.domain.model.YearlyRecap
import kotlinx.coroutines.flow.Flow

interface DiaryUseCase {
    fun getDiaryEntries(): Flow<List<DiaryEntry>>

    fun getDiaryEntriesForTitle(titleId: Int, mediaType: MediaType): Flow<List<DiaryEntry>>

    fun getEntriesByStatus(status: WatchStatus): Flow<List<DiaryEntry>>

    suspend fun getDiaryEntry(id: Long): DiaryEntry?

    suspend fun getLatestEntryForTitle(titleId: Int, mediaType: MediaType): DiaryEntry?

    suspend fun saveDiaryEntry(entry: DiaryEntry): Long

    suspend fun deleteDiaryEntry(id: Long)

    fun getWatchlist(): Flow<List<WatchlistItem>>

    fun isInWatchlist(titleId: Int, mediaType: MediaType): Flow<Boolean>

    suspend fun addToWatchlist(item: WatchlistItem)

    suspend fun removeFromWatchlist(titleId: Int, mediaType: MediaType)

    fun getCustomLists(): Flow<List<CustomList>>

    fun getCustomListItems(listId: Long): Flow<List<CustomListItem>>

    suspend fun createCustomList(name: String, description: String): Long

    suspend fun deleteCustomList(listId: Long)

    suspend fun addTitleToCustomList(listId: Long, item: CustomListItem)

    suspend fun removeTitleFromCustomList(listId: Long, titleId: Int, mediaType: MediaType)

    suspend fun getCustomListIdsForTitle(titleId: Int, mediaType: MediaType): List<Long>

    fun getStats(): Flow<Stats>

    fun getRecapYears(): Flow<List<Int>>

    fun getRecap(year: Int): Flow<YearlyRecap>

    suspend fun exportDiaryCsv(): String

    suspend fun importDiaryCsv(csv: String): Int
}
