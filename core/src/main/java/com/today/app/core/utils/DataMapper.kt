package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.TitleEntity
import com.today.app.core.data.source.remote.response.TitleDetailResponse
import com.today.app.core.data.source.remote.response.TitleResult
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.NextEpisode
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.TitleDetail

object DataMapper {

    fun mapResultsToEntities(input: List<TitleResult>, page: Int): List<TitleEntity> =
        input.mapNotNull { mapResultToEntity(it, page) }

    fun mapResultToEntity(
        result: TitleResult,
        page: Int,
        forcedMediaType: MediaType? = null
    ): TitleEntity? {
        val id = result.id ?: return null
        val mediaType = forcedMediaType ?: MediaType.from(result.mediaType) ?: return null

        return TitleEntity(
            id = id,
            mediaType = mediaType.value,
            name = result.title ?: result.name ?: result.originalTitle ?: result.originalName ?: "",
            originalName = result.originalTitle ?: result.originalName ?: "",
            country = result.originCountry?.filterNotNull()?.joinToString(", ") ?: "",
            language = result.originalLanguage ?: "",
            genre = result.genreIds?.filterNotNull()?.joinToString(", ") ?: "",
            releaseDate = result.releaseDate ?: result.firstAirDate ?: "",
            rating = result.voteAverage ?: 0.0,
            popularity = result.popularity ?: 0.0,
            overview = result.overview ?: "",
            posterPath = result.posterPath,
            backdropPath = result.backdropPath,
            page = page
        )
    }

    fun mapEntitiesToDomain(input: List<TitleEntity>): List<Title> =
        input.map { mapEntityToDomain(it) }

    fun mapEntityToDomain(input: TitleEntity): Title = Title(
        id = input.id,
        mediaType = MediaType.from(input.mediaType) ?: MediaType.TV,
        name = input.name,
        originalName = input.originalName,
        country = input.country,
        language = input.language,
        genre = input.genre,
        releaseDate = input.releaseDate,
        rating = input.rating,
        popularity = input.popularity,
        overview = input.overview,
        posterPath = input.posterPath,
        backdropPath = input.backdropPath
    )

    fun mapDomainToEntity(input: Title): TitleEntity = TitleEntity(
        id = input.id,
        mediaType = input.mediaType.value,
        name = input.name,
        originalName = input.originalName,
        country = input.country,
        language = input.language,
        genre = input.genre,
        releaseDate = input.releaseDate,
        rating = input.rating,
        popularity = input.popularity,
        overview = input.overview,
        posterPath = input.posterPath,
        backdropPath = input.backdropPath
    )

    fun mapResultToDomain(result: TitleResult): Title? =
        mapResultToEntity(result, page = 1)?.let { mapEntityToDomain(it) }

    fun mapResultToDomain(result: TitleResult, forcedMediaType: MediaType): Title? =
        mapResultToEntity(result, page = 1, forcedMediaType = forcedMediaType)
            ?.let { mapEntityToDomain(it) }

    fun mapResultsToDomain(input: List<TitleResult>): List<Title> =
        input.mapNotNull { mapResultToDomain(it) }

    fun mapDetailToDomain(
        input: TitleDetailResponse,
        mediaType: MediaType,
        trailerKey: String? = null
    ): TitleDetail = TitleDetail(
        id = input.id ?: 0,
        mediaType = mediaType,
        name = input.title ?: input.name ?: "",
        overview = input.overview ?: "",
        posterPath = input.posterPath,
        backdropPath = input.backdropPath,
        rating = input.voteAverage ?: 0.0,
        releaseDate = input.releaseDate ?: input.firstAirDate ?: "",
        genreNames = input.genres?.mapNotNull { it.name } ?: emptyList(),
        runtimeMinutes = input.runtime ?: input.episodeRunTime?.firstOrNull(),
        totalSeasons = input.numberOfSeasons,
        totalEpisodes = input.numberOfEpisodes,
        cast = input.credits?.cast
            ?.sortedBy { it.order ?: Int.MAX_VALUE }
            ?.mapNotNull { it.name }
            ?.distinct()
            ?.take(CAST_LIMIT)
            ?: emptyList(),
        similar = input.recommendations?.results
            ?.mapNotNull { mapResultToDomain(it, mediaType) }
            ?.take(SIMILAR_LIMIT)
            ?: emptyList(),
        trailerKey = trailerKey,
        nextEpisode = input.nextEpisodeToAir
            ?.airDate
            ?.takeIf { it.isNotBlank() }
            ?.let {
                NextEpisode(
                    airDate = it,
                    seasonNumber = input.nextEpisodeToAir.seasonNumber,
                    episodeNumber = input.nextEpisodeToAir.episodeNumber,
                    name = input.nextEpisodeToAir.name.orEmpty()
                )
            }
    )

    private const val CAST_LIMIT = 10
    private const val SIMILAR_LIMIT = 10
}
