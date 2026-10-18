package com.today.app.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class MediaType(val value: String) {
    MOVIE("movie"),
    TV("tv");

    companion object {
        fun from(value: String?): MediaType? =
            entries.firstOrNull { it.value == value }
    }
}

@Parcelize
data class Title(
    val id: Int,
    val mediaType: MediaType,
    val name: String,
    val originalName: String,
    val country: String,
    val language: String,
    val genre: String,
    val releaseDate: String,
    val rating: Double,
    val popularity: Double,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?
) : Parcelable
