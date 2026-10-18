package com.today.app.core.domain.usecase

import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.core.domain.repository.IDiaryRepository

class DiaryInteractor(private val diaryRepository: IDiaryRepository) : DiaryUseCase {

    override fun getDiaryEntries() = diaryRepository.getDiaryEntries()

    override fun getDiaryEntriesForTitle(titleId: Int, mediaType: MediaType) =
        diaryRepository.getDiaryEntriesForTitle(titleId, mediaType)

    override fun getEntriesByStatus(status: WatchStatus) =
        diaryRepository.getEntriesByStatus(status)

    override suspend fun getDiaryEntry(id: Long) = diaryRepository.getDiaryEntry(id)

    override suspend fun getLatestEntryForTitle(titleId: Int, mediaType: MediaType) =
        diaryRepository.getLatestEntryForTitle(titleId, mediaType)

    override suspend fun saveDiaryEntry(entry: DiaryEntry): Long =
        diaryRepository.saveDiaryEntry(entry)

    override suspend fun deleteDiaryEntry(id: Long) =
        diaryRepository.deleteDiaryEntry(id)

    override fun getWatchlist() = diaryRepository.getWatchlist()

    override fun isInWatchlist(titleId: Int, mediaType: MediaType) =
        diaryRepository.isInWatchlist(titleId, mediaType)

    override suspend fun addToWatchlist(item: WatchlistItem) =
        diaryRepository.addToWatchlist(item)

    override suspend fun removeFromWatchlist(titleId: Int, mediaType: MediaType) =
        diaryRepository.removeFromWatchlist(titleId, mediaType)

    override fun getCustomLists() = diaryRepository.getCustomLists()

    override fun getCustomListItems(listId: Long) = diaryRepository.getCustomListItems(listId)

    override suspend fun createCustomList(name: String, description: String): Long =
        diaryRepository.createCustomList(name, description)

    override suspend fun deleteCustomList(listId: Long) =
        diaryRepository.deleteCustomList(listId)

    override suspend fun addTitleToCustomList(listId: Long, item: CustomListItem) =
        diaryRepository.addTitleToCustomList(listId, item)

    override suspend fun removeTitleFromCustomList(
        listId: Long,
        titleId: Int,
        mediaType: MediaType
    ) = diaryRepository.removeTitleFromCustomList(listId, titleId, mediaType)

    override suspend fun getCustomListIdsForTitle(
        titleId: Int,
        mediaType: MediaType
    ) = diaryRepository.getCustomListIdsForTitle(titleId, mediaType)

    override fun getStats() = diaryRepository.getStats()

    override fun getRecapYears() = diaryRepository.getRecapYears()

    override fun getRecap(year: Int) = diaryRepository.getRecap(year)

    override suspend fun exportDiaryCsv(): String = diaryRepository.exportDiaryCsv()

    override suspend fun importDiaryCsv(csv: String): Int = diaryRepository.importDiaryCsv(csv)
}
