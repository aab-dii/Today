package com.today.app.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class TitleResponse(
    @field:SerializedName("page")
    val page: Int? = null,

    @field:SerializedName("total_pages")
    val totalPages: Int? = null,

    @field:SerializedName("total_results")
    val totalResults: Int? = null,

    @field:SerializedName("results")
    val results: List<TitleResult> = emptyList()
)

data class TitleResult(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("media_type")
    val mediaType: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("original_title")
    val originalTitle: String? = null,

    @field:SerializedName("original_name")
    val originalName: String? = null,

    @field:SerializedName("release_date")
    val releaseDate: String? = null,

    @field:SerializedName("first_air_date")
    val firstAirDate: String? = null,

    @field:SerializedName("overview")
    val overview: String? = null,

    @field:SerializedName("poster_path")
    val posterPath: String? = null,

    @field:SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @field:SerializedName("vote_average")
    val voteAverage: Double? = null,

    @field:SerializedName("popularity")
    val popularity: Double? = null,

    @field:SerializedName("genre_ids")
    val genreIds: List<Int?>? = null,

    @field:SerializedName("origin_country")
    val originCountry: List<String?>? = null,

    @field:SerializedName("original_language")
    val originalLanguage: String? = null,

    @field:SerializedName("adult")
    val adult: Boolean? = null
)

data class TitleDetailResponse(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("overview")
    val overview: String? = null,

    @field:SerializedName("poster_path")
    val posterPath: String? = null,

    @field:SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @field:SerializedName("vote_average")
    val voteAverage: Double? = null,

    @field:SerializedName("release_date")
    val releaseDate: String? = null,

    @field:SerializedName("first_air_date")
    val firstAirDate: String? = null,

    @field:SerializedName("genres")
    val genres: List<GenreResult>? = null,

    @field:SerializedName("runtime")
    val runtime: Int? = null,

    @field:SerializedName("episode_run_time")
    val episodeRunTime: List<Int?>? = null,

    @field:SerializedName("number_of_seasons")
    val numberOfSeasons: Int? = null,

    @field:SerializedName("number_of_episodes")
    val numberOfEpisodes: Int? = null,

    @field:SerializedName("next_episode_to_air")
    val nextEpisodeToAir: EpisodeAirResult? = null,

    @field:SerializedName("credits")
    val credits: CreditsResponse? = null,

    @field:SerializedName("recommendations")
    val recommendations: TitleResponse? = null
)

data class EpisodeAirResult(
    @field:SerializedName("air_date")
    val airDate: String? = null,

    @field:SerializedName("season_number")
    val seasonNumber: Int? = null,

    @field:SerializedName("episode_number")
    val episodeNumber: Int? = null,

    @field:SerializedName("name")
    val name: String? = null
)

data class CreditsResponse(
    @field:SerializedName("cast")
    val cast: List<CastResult> = emptyList()
)

data class CastResult(
    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("character")
    val character: String? = null,

    @field:SerializedName("profile_path")
    val profilePath: String? = null,

    @field:SerializedName("order")
    val order: Int? = null
)

data class GenreResult(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("name")
    val name: String? = null
)
