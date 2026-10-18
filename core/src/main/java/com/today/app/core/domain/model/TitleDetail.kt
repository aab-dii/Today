package com.today.app.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NextEpisode(
    val airDate: String,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val name: String
) : Parcelable

@Parcelize
data class TitleDetail(
    val id: Int,
    val mediaType: MediaType,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val rating: Double,
    val releaseDate: String,
    val genreNames: List<String>,
    val runtimeMinutes: Int?,
    val totalSeasons: Int? = null,
    val totalEpisodes: Int? = null,
    val cast: List<String> = emptyList(),
    val similar: List<Title> = emptyList(),
    val trailerKey: String? = null,
    val nextEpisode: NextEpisode? = null
) : Parcelable
