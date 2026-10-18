package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.TitleEntity
import com.today.app.core.data.source.remote.response.CastResult
import com.today.app.core.data.source.remote.response.CreditsResponse
import com.today.app.core.data.source.remote.response.GenreResult
import com.today.app.core.data.source.remote.response.TitleDetailResponse
import com.today.app.core.data.source.remote.response.TitleResponse
import com.today.app.core.data.source.remote.response.TitleResult
import com.today.app.core.domain.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DataMapperTest {

    @Test
    fun `mapResultToEntity returns null when id missing`() {
        assertNull(DataMapper.mapResultToEntity(TitleResult(id = null, mediaType = "movie"), page = 1))
    }

    @Test
    fun `mapResultToEntity returns null when media type missing or unknown`() {
        assertNull(DataMapper.mapResultToEntity(TitleResult(id = 1, mediaType = null), page = 1))
        assertNull(DataMapper.mapResultToEntity(TitleResult(id = 1, mediaType = "person"), page = 1))
    }

    @Test
    fun `mapResultToEntity prefers title then name then original fields`() {
        val withTitle = DataMapper.mapResultToEntity(
            TitleResult(id = 1, mediaType = "movie", title = "Arrival", name = "N"),
            page = 2
        )
        assertEquals("Arrival", withTitle?.name)
        assertEquals(2, withTitle?.page)

        val withName = DataMapper.mapResultToEntity(
            TitleResult(id = 2, mediaType = "tv", name = "Severance"),
            page = 1
        )
        assertEquals("Severance", withName?.name)

        val withOriginal = DataMapper.mapResultToEntity(
            TitleResult(id = 3, mediaType = "movie", originalTitle = "Original"),
            page = 1
        )
        assertEquals("Original", withOriginal?.name)
    }

    @Test
    fun `mapResultToEntity joins origins and genres and falls back for empty lists`() {
        val entity = DataMapper.mapResultToEntity(
            TitleResult(
                id = 5,
                mediaType = "tv",
                name = "Show",
                originCountry = listOf("US", null, "KR"),
                genreIds = listOf(18, null, 10765),
                releaseDate = null,
                firstAirDate = "2024-02-22",
                voteAverage = null,
                popularity = null
            ),
            page = 1
        )

        assertEquals("US, KR", entity?.country)
        assertEquals("18, 10765", entity?.genre)
        assertEquals("2024-02-22", entity?.releaseDate)
        assertEquals(0.0, entity?.rating)
        assertEquals(0.0, entity?.popularity)
    }

    @Test
    fun `mapResultsToEntities filters invalid results`() {
        val entities = DataMapper.mapResultsToEntities(
            listOf(
                TitleResult(id = 1, mediaType = "movie", title = "A"),
                TitleResult(id = null, mediaType = "movie", title = "B"),
                TitleResult(id = 3, mediaType = "tv", name = "C")
            ),
            page = 1
        )

        assertEquals(2, entities.size)
    }

    @Test
    fun `mapEntityToDomain falls back to TV for unknown media type`() {
        val domain = DataMapper.mapEntityToDomain(
            TitleEntity(
                id = 1,
                mediaType = "bogus",
                name = "X",
                originalName = "",
                country = "",
                language = "",
                genre = "",
                releaseDate = "",
                rating = 0.0,
                popularity = 0.0,
                overview = "",
                posterPath = null,
                backdropPath = null
            )
        )

        assertEquals(MediaType.TV, domain.mediaType)
    }

    @Test
    fun `entity domain round trip preserves values`() {
        val entity = TitleEntity(
            id = 8,
            mediaType = "movie",
            name = "Dune",
            originalName = "Dune",
            country = "US",
            language = "en",
            genre = "878",
            releaseDate = "2021-10-22",
            rating = 8.1,
            popularity = 100.0,
            overview = "overview",
            posterPath = "/p",
            backdropPath = "/b",
            page = 1
        )

        val back = DataMapper.mapDomainToEntity(DataMapper.mapEntityToDomain(entity))

        assertEquals(entity, back)
    }

    @Test
    fun `mapDetailToDomain prefers title and uses episode runtime fallback`() {
        val detail = DataMapper.mapDetailToDomain(
            TitleDetailResponse(
                id = 3,
                title = null,
                name = "Severance",
                overview = "overview",
                voteAverage = 8.7,
                firstAirDate = "2022-02-18",
                genres = listOf(GenreResult(18, "Drama"), GenreResult(null, null)),
                runtime = null,
                episodeRunTime = listOf(48, 50)
            ),
            mediaType = MediaType.TV,
            trailerKey = "abc"
        )

        assertEquals("Severance", detail.name)
        assertEquals(48, detail.runtimeMinutes)
        assertEquals(listOf("Drama"), detail.genreNames)
        assertEquals("abc", detail.trailerKey)
        assertEquals(MediaType.TV, detail.mediaType)
    }

    @Test
    fun `mapResultToEntity honors forced media type when result lacks it`() {
        val entity = DataMapper.mapResultToEntity(
            TitleResult(id = 9, title = "Recommended"),
            page = 1,
            forcedMediaType = MediaType.MOVIE
        )

        assertEquals(MediaType.MOVIE.value, entity?.mediaType)
    }

    @Test
    fun `mapDetailToDomain maps totals cast and similar`() {
        val detail = DataMapper.mapDetailToDomain(
            TitleDetailResponse(
                id = 10,
                name = "Show",
                numberOfSeasons = 3,
                numberOfEpisodes = 24,
                credits = CreditsResponse(
                    cast = listOf(
                        CastResult(name = "Second", order = 2),
                        CastResult(name = "First", order = 1),
                        CastResult(name = "First", order = 1),
                        CastResult(name = null, order = 0)
                    )
                ),
                recommendations = TitleResponse(
                    results = listOf(
                        TitleResult(id = 11, title = "Rec A"),
                        TitleResult(id = null, title = "Invalid")
                    )
                )
            ),
            mediaType = MediaType.TV
        )

        assertEquals(3, detail.totalSeasons)
        assertEquals(24, detail.totalEpisodes)
        assertEquals(listOf("First", "Second"), detail.cast)
        assertEquals(1, detail.similar.size)
        assertEquals(MediaType.TV, detail.similar.first().mediaType)
    }
}
