package com.today.app.core.data

import com.today.app.core.data.source.local.DiaryLocalDataSource
import com.today.app.core.data.source.local.entity.CustomListEntity
import com.today.app.core.domain.model.CustomList
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Stats
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.core.domain.model.YearlyRecap
import com.today.app.core.domain.repository.IDiaryRepository
import com.today.app.core.utils.DiaryMapper
import com.today.app.core.utils.DiaryCsvCodec
import com.today.app.core.utils.RecapCalculator
import com.today.app.core.utils.StatsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiaryRepository(
    private val localDataSource: DiaryLocalDataSource
) : IDiaryRepository {

    override fun getDiaryEntries(): Flow<List<DiaryEntry>> =
        localDataSource.getDiaryEntries().map { entries ->
            entries.map { DiaryMapper.toDomain(it) }
        }

    override fun getDiaryEntriesForTitle(
        titleId: Int,
        mediaType: MediaType
    ): Flow<List<DiaryEntry>> =
        localDataSource.getDiaryEntriesForTitle(titleId, mediaType.value).map { entries ->
            entries.map { DiaryMapper.toDomain(it) }
        }

    override fun getEntriesByStatus(status: WatchStatus): Flow<List<DiaryEntry>> =
        localDataSource.getEntriesByStatus(status.name).map { entries ->
            entries.map { DiaryMapper.toDomain(it) }
        }

    override suspend fun getDiaryEntry(id: Long): DiaryEntry? =
        localDataSource.getDiaryEntry(id)?.let { DiaryMapper.toDomain(it) }

    override suspend fun getLatestEntryForTitle(
        titleId: Int,
        mediaType: MediaType
    ): DiaryEntry? =
        localDataSource.getLatestEntryForTitle(titleId, mediaType.value)
            ?.let { DiaryMapper.toDomain(it) }

    override suspend fun saveDiaryEntry(entry: DiaryEntry): Long {
        val id = localDataSource.saveDiaryEntry(DiaryMapper.toEntity(entry))
        localDataSource.removeFromWatchlist(entry.titleId, entry.mediaType.value)
        return id
    }

    override suspend fun deleteDiaryEntry(id: Long) =
        localDataSource.deleteDiaryEntry(id)

    override fun getWatchlist(): Flow<List<WatchlistItem>> =
        localDataSource.getWatchlist().map { items ->
            items.map { DiaryMapper.watchlistToDomain(it) }
        }

    override fun isInWatchlist(titleId: Int, mediaType: MediaType): Flow<Boolean> =
        localDataSource.isInWatchlist(titleId, mediaType.value)

    override suspend fun addToWatchlist(item: WatchlistItem) =
        localDataSource.addToWatchlist(DiaryMapper.watchlistToEntity(item))

    override suspend fun removeFromWatchlist(titleId: Int, mediaType: MediaType) =
        localDataSource.removeFromWatchlist(titleId, mediaType.value)

    override fun getCustomLists(): Flow<List<CustomList>> =
        localDataSource.getCustomLists().map { lists ->
            lists.map { DiaryMapper.listToDomain(it) }
        }

    override fun getCustomListItems(listId: Long): Flow<List<CustomListItem>> =
        localDataSource.getCustomListItems(listId).map { items ->
            items.map { DiaryMapper.listItemToDomain(it) }
        }

    override suspend fun createCustomList(name: String, description: String): Long =
        localDataSource.createList(
            CustomListEntity(
                name = name,
                description = description,
                createdAt = System.currentTimeMillis()
            )
        )

    override suspend fun deleteCustomList(listId: Long) =
        localDataSource.deleteList(listId)

    override suspend fun addTitleToCustomList(listId: Long, item: CustomListItem) =
        localDataSource.addTitleToList(
            DiaryMapper.listItemToEntity(listId, item, System.currentTimeMillis())
        )

    override suspend fun removeTitleFromCustomList(
        listId: Long,
        titleId: Int,
        mediaType: MediaType
    ) = localDataSource.removeTitleFromList(listId, titleId, mediaType.value)

    override suspend fun getCustomListIdsForTitle(
        titleId: Int,
        mediaType: MediaType
    ): List<Long> = localDataSource.getListIdsForTitle(titleId, mediaType.value)

    override fun getStats(): Flow<Stats> =
        localDataSource.getDiaryEntries().map { StatsCalculator.calculate(it) }

    override fun getRecapYears(): Flow<List<Int>> =
        localDataSource.getDiaryEntries().map { RecapCalculator.availableYears(it) }

    override fun getRecap(year: Int): Flow<YearlyRecap> =
        localDataSource.getDiaryEntries().map { RecapCalculator.calculate(it, year) }

    override suspend fun exportDiaryCsv(): String {
        val entries = localDataSource.getDiaryEntriesOnce().map { DiaryMapper.toDomain(it) }
        return DiaryCsvCodec.export(entries)
    }

    override suspend fun importDiaryCsv(csv: String): Int {
        val result = DiaryCsvCodec.parse(csv)
        result.entries.forEach { entry ->
            localDataSource.saveDiaryEntry(DiaryMapper.toEntity(entry.copy(id = 0)))
        }
        return result.entries.size
    }
}
