package com.today.app.core.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.today.app.core.data.source.local.entity.CustomListEntity
import com.today.app.core.data.source.local.entity.CustomListEntryEntity
import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.data.source.local.entity.TitleEntity
import com.today.app.core.data.source.local.entity.WatchlistItemEntity

@Database(
    entities = [
        TitleEntity::class,
        DiaryEntryEntity::class,
        WatchlistItemEntity::class,
        CustomListEntity::class,
        CustomListEntryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TitleDatabase : RoomDatabase() {

    abstract fun titleDao(): TitleDao

    abstract fun diaryDao(): DiaryDao

    abstract fun watchlistDao(): WatchlistDao

    abstract fun customListDao(): CustomListDao
}
