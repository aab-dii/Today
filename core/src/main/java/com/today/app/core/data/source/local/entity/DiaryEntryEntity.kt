package com.today.app.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entry")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long = 0,

    @ColumnInfo("title_id")
    val titleId: Int,

    @ColumnInfo("media_type")
    val mediaType: String,

    @ColumnInfo("title_name")
    val titleName: String,

    @ColumnInfo("poster_path")
    val posterPath: String?,

    @ColumnInfo("watched_on")
    val watchedOn: Long,

    @ColumnInfo("rating")
    val rating: Double,

    @ColumnInfo("mood")
    val mood: String?,

    @ColumnInfo("note")
    val note: String,

    @ColumnInfo("watched_with")
    val watchedWith: String?,

    @ColumnInfo("status")
    val status: String,

    @ColumnInfo("season_number")
    val seasonNumber: Int?,

    @ColumnInfo("episode_number")
    val episodeNumber: Int?,

    @ColumnInfo("rewatch_of")
    val rewatchOf: Long?,

    @ColumnInfo("genre_ids")
    val genreIds: String,

    @ColumnInfo("country")
    val country: String,

    @ColumnInfo("runtime_minutes")
    val runtimeMinutes: Int?
)
