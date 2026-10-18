package com.today.app.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_list")
data class CustomListEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long = 0,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("description")
    val description: String,

    @ColumnInfo("created_at")
    val createdAt: Long
)

@Entity(
    tableName = "custom_list_entry",
    primaryKeys = ["list_id", "title_id", "media_type"]
)
data class CustomListEntryEntity(
    @ColumnInfo("list_id")
    val listId: Long,

    @ColumnInfo("title_id")
    val titleId: Int,

    @ColumnInfo("media_type")
    val mediaType: String,

    @ColumnInfo("title_name")
    val titleName: String,

    @ColumnInfo("poster_path")
    val posterPath: String?,

    @ColumnInfo("added_at")
    val addedAt: Long
)

data class CustomListWithCount(
    @ColumnInfo("id") val id: Long,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("description") val description: String,
    @ColumnInfo("created_at") val createdAt: Long,
    @ColumnInfo("item_count") val itemCount: Int
)
