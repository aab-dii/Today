package com.today.app.core.data.source.local

import com.today.app.core.data.source.local.entity.CustomListEntryEntity
import com.today.app.core.data.source.local.entity.CustomListEntity
import com.today.app.core.data.source.local.entity.CustomListWithCount
import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.data.source.local.entity.WatchlistItemEntity
import com.today.app.core.data.source.local.room.CustomListDao
import com.today.app.core.data.source.local.room.DiaryDao
import com.today.app.core.data.source.local.room.WatchlistDao
import kotlinx.coroutines.flow.Flow

class DiaryLocalDataSource(
    private val diaryDao: DiaryDao,
    private val watchlistDao: WatchlistDao,
    private val customListDao: CustomListDao
) {

    // Diary
    fun getDiaryEntries(): Flow<List<DiaryEntryEntity>> = diaryDao.getAll()

    suspend fun getDiaryEntriesOnce(): List<DiaryEntryEntity> = diaryDao.getAllOnce()

    fun getDiaryEntriesForTitle(titleId: Int, mediaType: String): Flow<List<DiaryEntryEntity>> =
        diaryDao.getForTitle(titleId, mediaType)

    fun getEntriesByStatus(status: String): Flow<List<DiaryEntryEntity>> =
        diaryDao.getByStatus(status)

    suspend fun getDiaryEntry(id: Long): DiaryEntryEntity? = diaryDao.getById(id)

    suspend fun getLatestEntryForTitle(titleId: Int, mediaType: String): DiaryEntryEntity? =
        diaryDao.getLatestForTitle(titleId, mediaType)

    suspend fun saveDiaryEntry(entry: DiaryEntryEntity): Long = diaryDao.insert(entry)

    suspend fun deleteDiaryEntry(id: Long) = diaryDao.delete(id)

    suspend fun deleteDiaryForTitle(titleId: Int, mediaType: String) =
        diaryDao.deleteForTitle(titleId, mediaType)

    // Watchlist
    fun getWatchlist(): Flow<List<WatchlistItemEntity>> = watchlistDao.getAll()

    fun isInWatchlist(titleId: Int, mediaType: String): Flow<Boolean> =
        watchlistDao.exists(titleId, mediaType)

    suspend fun addToWatchlist(item: WatchlistItemEntity) = watchlistDao.insert(item)

    suspend fun removeFromWatchlist(titleId: Int, mediaType: String) =
        watchlistDao.delete(titleId, mediaType)

    // Custom lists
    fun getCustomLists(): Flow<List<CustomListWithCount>> = customListDao.getLists()

    fun getCustomListItems(listId: Long): Flow<List<CustomListEntryEntity>> =
        customListDao.getItems(listId)

    suspend fun createList(list: CustomListEntity): Long = customListDao.insertList(list)

    suspend fun deleteList(listId: Long) = customListDao.deleteList(listId)

    suspend fun addTitleToList(item: CustomListEntryEntity) = customListDao.insertItem(item)

    suspend fun removeTitleFromList(listId: Long, titleId: Int, mediaType: String) =
        customListDao.deleteItem(listId, titleId, mediaType)

    suspend fun getListIdsForTitle(titleId: Int, mediaType: String): List<Long> =
        customListDao.getListIdsForTitle(titleId, mediaType)
}
