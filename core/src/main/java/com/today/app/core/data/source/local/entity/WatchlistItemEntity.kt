package com.today.app.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "watchlist_item",
    primaryKeys = ["title_id", "media_type"]
)
data class WatchlistItemEntity(
    @ColumnInfo("title_id")
    val titleId: Int,

    @ColumnInfo("media_type")
    val mediaType: String,

    @ColumnInfo("title_name")
    val titleName: String,

    @ColumnInfo("poster_path")
    val posterPath: String?,

    @ColumnInfo("added_on")
    val addedOn: Long
)
