package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.CustomListEntryEntity
import com.today.app.core.data.source.local.entity.CustomListWithCount
import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.data.source.local.entity.WatchlistItemEntity
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiaryMapperTest {

    private fun diaryEntity(
        mediaType: String = "movie",
        mood: String? = "HAPPY",
        status: String = "WATCHING"
    ) = DiaryEntryEntity(
        id = 7,
        titleId = 42,
        mediaType = mediaType,
        titleName = "Arrival",
        posterPath = "/p.jpg",
        watchedOn = 1_700_000_000_000,
        rating = 4.5,
        mood = mood,
        note = "Bagus",
        watchedWith = "Bareng",
        status = status,
        seasonNumber = 1,
        episodeNumber = 3,
        rewatchOf = 5,
        genreIds = "878",
        country = "US",
        runtimeMinutes = 116
    )

    @Test
    fun `toDomain maps every field`() {
        val domain = DiaryMapper.toDomain(diaryEntity())

        assertEquals(7L, domain.id)
        assertEquals(42, domain.titleId)
        assertEquals(MediaType.MOVIE, domain.mediaType)
        assertEquals("Arrival", domain.titleName)
        assertEquals("/p.jpg", domain.posterPath)
        assertEquals(1_700_000_000_000, domain.watchedOn)
        assertEquals(4.5, domain.rating, 0.0)
        assertEquals(Mood.HAPPY, domain.mood)
        assertEquals("Bagus", domain.note)
        assertEquals("Bareng", domain.watchedWith)
        assertEquals(WatchStatus.WATCHING, domain.status)
        assertEquals(1, domain.seasonNumber)
        assertEquals(3, domain.episodeNumber)
        assertEquals(5L, domain.rewatchOf)
        assertEquals("878", domain.genreIds)
        assertEquals("US", domain.country)
        assertEquals(116, domain.runtimeMinutes)
    }

    @Test
    fun `toDomain falls back when enum values are unknown or null`() {
        val domain = DiaryMapper.toDomain(diaryEntity(mediaType = "bogus", mood = null, status = "nope"))

        assertEquals(MediaType.MOVIE, domain.mediaType)
        assertNull(domain.mood)
        assertEquals(WatchStatus.COMPLETED, domain.status)
    }

    @Test
    fun `entity to domain and back preserves values`() {
        val original = diaryEntity()
        val roundTrip = DiaryMapper.toEntity(DiaryMapper.toDomain(original))

        assertEquals(original, roundTrip)
    }

    @Test
    fun `toEntity encodes enum names`() {
        val entry = DiaryEntry(
            titleId = 1,
            mediaType = MediaType.TV,
            titleName = "Show",
            posterPath = null,
            watchedOn = 10,
            mood = Mood.BORED,
            status = WatchStatus.DROPPED
        )

        val entity = DiaryMapper.toEntity(entry)

        assertEquals("tv", entity.mediaType)
        assertEquals("BORED", entity.mood)
        assertEquals("DROPPED", entity.status)
    }

    @Test
    fun `watchlist mapping round trips`() {
        val entity = WatchlistItemEntity(
            titleId = 9,
            mediaType = "tv",
            titleName = "Severance",
            posterPath = null,
            addedOn = 123
        )

        val domain = DiaryMapper.watchlistToDomain(entity)

        assertEquals(9, domain.titleId)
        assertEquals(MediaType.TV, domain.mediaType)
        assertEquals("Severance", domain.titleName)
        assertEquals(123L, domain.addedOn)
        assertEquals(entity, DiaryMapper.watchlistToEntity(domain))
    }

    @Test
    fun `watchlist domain to entity maps media type value`() {
        val entity = DiaryMapper.watchlistToEntity(
            WatchlistItem(1, MediaType.MOVIE, "A", "/x", 5)
        )

        assertEquals("movie", entity.mediaType)
    }

    @Test
    fun `custom list with count maps to domain`() {
        val domain = DiaryMapper.listToDomain(
            CustomListWithCount(id = 3, name = "Favorit", description = "desc", createdAt = 99, itemCount = 4)
        )

        assertEquals(3L, domain.id)
        assertEquals("Favorit", domain.name)
        assertEquals(4, domain.itemCount)
    }

    @Test
    fun `custom list item maps both directions`() {
        val entryEntity = CustomListEntryEntity(
            listId = 2,
            titleId = 11,
            mediaType = "movie",
            titleName = "Dune",
            posterPath = "/d.jpg",
            addedAt = 55
        )

        val domain = DiaryMapper.listItemToDomain(entryEntity)
        assertEquals(11, domain.titleId)
        assertEquals(MediaType.MOVIE, domain.mediaType)

        val back = DiaryMapper.listItemToEntity(
            listId = 2,
            item = CustomListItem(11, MediaType.MOVIE, "Dune", "/d.jpg"),
            addedAt = 55
        )
        assertEquals(entryEntity, back)
    }
}
