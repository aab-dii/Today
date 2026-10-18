package com.today.app.core.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.today.app.core.data.source.local.entity.CustomListEntryEntity
import com.today.app.core.data.source.local.entity.CustomListEntity
import com.today.app.core.data.source.local.entity.CustomListWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListDao {

    @Query(
        "SELECT c.id AS id, c.name AS name, c.description AS description, " +
            "c.created_at AS created_at, " +
            "(SELECT COUNT(*) FROM custom_list_entry e WHERE e.list_id = c.id) AS item_count " +
            "FROM custom_list c ORDER BY c.created_at DESC"
    )
    fun getLists(): Flow<List<CustomListWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: CustomListEntity): Long

    @Query("DELETE FROM custom_list WHERE id = :listId")
    suspend fun deleteList(listId: Long)

    @Query("SELECT * FROM custom_list_entry WHERE list_id = :listId ORDER BY added_at DESC")
    fun getItems(listId: Long): Flow<List<CustomListEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CustomListEntryEntity)

    @Query(
        "DELETE FROM custom_list_entry " +
            "WHERE list_id = :listId AND title_id = :titleId AND media_type = :mediaType"
    )
    suspend fun deleteItem(listId: Long, titleId: Int, mediaType: String)

    @Query(
        "SELECT list_id FROM custom_list_entry " +
            "WHERE title_id = :titleId AND media_type = :mediaType"
    )
    suspend fun getListIdsForTitle(titleId: Int, mediaType: String): List<Long>
}
