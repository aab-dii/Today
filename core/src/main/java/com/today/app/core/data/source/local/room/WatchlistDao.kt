package com.today.app.core.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.today.app.core.data.source.local.entity.WatchlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_item ORDER BY added_on DESC")
    fun getAll(): Flow<List<WatchlistItemEntity>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM watchlist_item " +
            "WHERE title_id = :titleId AND media_type = :mediaType)"
    )
    fun exists(titleId: Int, mediaType: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_item WHERE title_id = :titleId AND media_type = :mediaType")
    suspend fun delete(titleId: Int, mediaType: String)
}
