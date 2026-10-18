package com.today.app.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "title",
    primaryKeys = ["id", "media_type"]
)
data class TitleEntity(
    @ColumnInfo("id")
    val id: Int,

    @ColumnInfo("media_type")
    val mediaType: String,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("original_name")
    val originalName: String,

    @ColumnInfo("country")
    val country: String,

    @ColumnInfo("language")
    val language: String,

    @ColumnInfo("genre")
    val genre: String,

    @ColumnInfo("release_date")
    val releaseDate: String,

    @ColumnInfo("rating")
    val rating: Double,

    @ColumnInfo("popularity")
    val popularity: Double,

    @ColumnInfo("overview")
    val overview: String,

    @ColumnInfo("poster_path")
    val posterPath: String?,

    @ColumnInfo("backdrop_path")
    val backdropPath: String?,

    @ColumnInfo("page")
    val page: Int = 1
)
