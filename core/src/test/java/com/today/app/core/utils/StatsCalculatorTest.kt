package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.domain.model.CountItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsCalculatorTest {

    private fun entry(
        id: Long = 0,
        titleId: Int = 1,
        mediaType: String = "movie",
        watchedOn: Long = 0,
        genreIds: String = "",
        country: String = "",
        runtimeMinutes: Int? = null
    ) = DiaryEntryEntity(
        id = id,
        titleId = titleId,
        mediaType = mediaType,
        titleName = "Title $titleId",
        posterPath = null,
        watchedOn = watchedOn,
        rating = 4.0,
        mood = null,
        note = "",
        watchedWith = null,
        status = "COMPLETED",
        seasonNumber = null,
        episodeNumber = null,
        rewatchOf = null,
        genreIds = genreIds,
        country = country,
        runtimeMinutes = runtimeMinutes
    )

    @Test
    fun `empty entries produce default stats`() {
        val stats = StatsCalculator.calculate(emptyList())

        assertEquals(0, stats.totalTitles)
        assertEquals(0, stats.entryCount)
        assertEquals(0L, stats.totalMinutes)
        assertEquals(0L, stats.totalHours)
        assertTrue(stats.topGenres.isEmpty())
        assertTrue(stats.topCountries.isEmpty())
        assertTrue(stats.heatmap.isEmpty())
    }

    @Test
    fun `totalTitles counts distinct media type and id pairs`() {
        val entries = listOf(
            entry(id = 1, titleId = 1, mediaType = "movie"),
            entry(id = 2, titleId = 1, mediaType = "movie"),
            entry(id = 3, titleId = 1, mediaType = "tv"),
            entry(id = 4, titleId = 2, mediaType = "movie")
        )

        val stats = StatsCalculator.calculate(entries)

        assertEquals(3, stats.totalTitles)
        assertEquals(4, stats.entryCount)
    }

    @Test
    fun `totalMinutes sums runtimes and treats null as zero`() {
        val entries = listOf(
            entry(id = 1, runtimeMinutes = 120),
            entry(id = 2, runtimeMinutes = null),
            entry(id = 3, runtimeMinutes = 30)
        )

        val stats = StatsCalculator.calculate(entries)

        assertEquals(150L, stats.totalMinutes)
        assertEquals(2L, stats.totalHours)
    }

    @Test
    fun `topGenres aggregates genre names across entries sorted by count`() {
        val entries = listOf(
            entry(id = 1, genreIds = "28, 878"),
            entry(id = 2, genreIds = "28"),
            entry(id = 3, genreIds = "878")
        )

        val stats = StatsCalculator.calculate(entries)

        assertEquals(CountItem("Action", 2), stats.topGenres.first())
        assertEquals(CountItem("Sci-Fi", 2), stats.topGenres[1])
    }

    @Test
    fun `topCountries maps ISO codes to names and dedupes per entry`() {
        val entries = listOf(
            entry(id = 1, country = "US, KR"),
            entry(id = 2, country = "US, US")
        )

        val stats = StatsCalculator.calculate(entries)

        val us = stats.topCountries.first { it.label == "Amerika Serikat" }
        val kr = stats.topCountries.first { it.label == "Korea Selatan" }
        assertEquals(CountItem("Amerika Serikat", 2), us)
        assertEquals(CountItem("Korea Selatan", 1), kr)
    }

    @Test
    fun `topList is limited to five items`() {
        val entries = listOf(
            entry(id = 1, genreIds = "28, 12, 16, 35, 80, 18, 14")
        )

        val stats = StatsCalculator.calculate(entries)

        assertEquals(5, stats.topGenres.size)
    }

    @Test
    fun `heatmap groups entries by day`() {
        val day = 86_400_000L
        val entries = listOf(
            entry(id = 1, watchedOn = 0),
            entry(id = 2, watchedOn = day / 2),
            entry(id = 3, watchedOn = day)
        )

        val stats = StatsCalculator.calculate(entries)

        assertEquals(2, stats.heatmap[0L])
        assertEquals(1, stats.heatmap[1L])
    }
}
