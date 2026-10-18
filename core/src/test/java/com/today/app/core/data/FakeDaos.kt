package com.today.app.core.data

import com.today.app.core.data.source.local.entity.CustomListEntryEntity
import com.today.app.core.data.source.local.entity.CustomListEntity
import com.today.app.core.data.source.local.entity.CustomListWithCount
import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.data.source.local.entity.WatchlistItemEntity
import com.today.app.core.data.source.local.room.CustomListDao
import com.today.app.core.data.source.local.room.DiaryDao
import com.today.app.core.data.source.local.room.WatchlistDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDiaryDao : DiaryDao {

    private val state = MutableStateFlow<List<DiaryEntryEntity>>(emptyList())
    private var nextId = 1L

    override fun getAll(): Flow<List<DiaryEntryEntity>> = state

    override suspend fun getAllOnce(): List<DiaryEntryEntity> = state.value

    override fun getForTitle(titleId: Int, mediaType: String): Flow<List<DiaryEntryEntity>> =
        state.map { list -> list.filter { it.titleId == titleId && it.mediaType == mediaType } }

    override fun getByStatus(status: String): Flow<List<DiaryEntryEntity>> =
        state.map { list -> list.filter { it.status == status } }

    override suspend fun getById(id: Long): DiaryEntryEntity? =
        state.value.firstOrNull { it.id == id }

    override suspend fun getLatestForTitle(titleId: Int, mediaType: String): DiaryEntryEntity? =
        state.value
            .filter { it.titleId == titleId && it.mediaType == mediaType }
            .maxByOrNull { it.watchedOn }

    override suspend fun insert(entry: DiaryEntryEntity): Long {
        val id = if (entry.id == 0L) nextId++ else entry.id
        state.value = state.value.filterNot { it.id == id } + entry.copy(id = id)
        return id
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun deleteForTitle(titleId: Int, mediaType: String) {
        state.value = state.value.filterNot { it.titleId == titleId && it.mediaType == mediaType }
    }
}

class FakeWatchlistDao : WatchlistDao {

    private val state = MutableStateFlow<List<WatchlistItemEntity>>(emptyList())

    override fun getAll(): Flow<List<WatchlistItemEntity>> = state

    override fun exists(titleId: Int, mediaType: String): Flow<Boolean> =
        state.map { list -> list.any { it.titleId == titleId && it.mediaType == mediaType } }

    override suspend fun insert(item: WatchlistItemEntity) {
        state.value = state.value
            .filterNot { it.titleId == item.titleId && it.mediaType == item.mediaType } + item
    }

    override suspend fun delete(titleId: Int, mediaType: String) {
        state.value = state.value.filterNot { it.titleId == titleId && it.mediaType == mediaType }
    }
}

class FakeCustomListDao : CustomListDao {

    private val lists = MutableStateFlow<List<CustomListEntity>>(emptyList())
    private val items = MutableStateFlow<List<CustomListEntryEntity>>(emptyList())
    private var nextListId = 1L

    override fun getLists(): Flow<List<CustomListWithCount>> =
        lists.map { list ->
            list.map { l ->
                CustomListWithCount(
                    id = l.id,
                    name = l.name,
                    description = l.description,
                    createdAt = l.createdAt,
                    itemCount = items.value.count { it.listId == l.id }
                )
            }
        }

    override suspend fun insertList(list: CustomListEntity): Long {
        val id = if (list.id == 0L) nextListId++ else list.id
        lists.value = lists.value.filterNot { it.id == id } + list.copy(id = id)
        return id
    }

    override suspend fun deleteList(listId: Long) {
        lists.value = lists.value.filterNot { it.id == listId }
        items.value = items.value.filterNot { it.listId == listId }
    }

    override fun getItems(listId: Long): Flow<List<CustomListEntryEntity>> =
        items.map { list -> list.filter { it.listId == listId } }

    override suspend fun insertItem(item: CustomListEntryEntity) {
        items.value = items.value.filterNot {
            it.listId == item.listId && it.titleId == item.titleId && it.mediaType == item.mediaType
        } + item
    }

    override suspend fun deleteItem(listId: Long, titleId: Int, mediaType: String) {
        items.value = items.value.filterNot {
            it.listId == listId && it.titleId == titleId && it.mediaType == mediaType
        }
    }

    override suspend fun getListIdsForTitle(titleId: Int, mediaType: String): List<Long> =
        items.value
            .filter { it.titleId == titleId && it.mediaType == mediaType }
            .map { it.listId }
}
