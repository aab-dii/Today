package com.today.app.core.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entry ORDER BY watched_on DESC, id DESC")
    fun getAll(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entry ORDER BY watched_on DESC, id DESC")
    suspend fun getAllOnce(): List<DiaryEntryEntity>

    @Query(
        "SELECT * FROM diary_entry WHERE title_id = :titleId AND media_type = :mediaType " +
            "ORDER BY watched_on DESC, id DESC"
    )
    fun getForTitle(titleId: Int, mediaType: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entry WHERE status = :status ORDER BY watched_on DESC, id DESC")
    fun getByStatus(status: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entry WHERE id = :id")
    suspend fun getById(id: Long): DiaryEntryEntity?

    @Query(
        "SELECT * FROM diary_entry WHERE title_id = :titleId AND media_type = :mediaType " +
            "ORDER BY watched_on DESC, id DESC LIMIT 1"
    )
    suspend fun getLatestForTitle(titleId: Int, mediaType: String): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntryEntity): Long

    @Query("DELETE FROM diary_entry WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM diary_entry WHERE title_id = :titleId AND media_type = :mediaType")
    suspend fun deleteForTitle(titleId: Int, mediaType: String)
}
