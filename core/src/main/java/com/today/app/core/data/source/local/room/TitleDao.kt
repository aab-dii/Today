package com.today.app.core.data.source.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.today.app.core.data.source.local.entity.TitleEntity

@Dao
interface TitleDao {

    @Query("SELECT * FROM title")
    fun getPagedTitles(): PagingSource<Int, TitleEntity>

    @Upsert
    suspend fun upsertTitles(titles: List<TitleEntity>)
}
