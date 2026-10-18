package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.domain.model.CountItem
import com.today.app.core.domain.model.Mood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RecapCalculatorTest {

    private fun at(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day)
        }.timeInMillis

    private fun entry(
        id: Long = 0,
        titleId: Int = 1,
        mediaType: String = "movie",
        titleName: String = "Title $titleId",
        watchedOn: Long = 0,
        rating: Double = 4.0,
        mood: String? = null,
        watchedWith: String? = null,
        rewatchOf: Long? = null,
        genreIds: String = "",
        country: String = "",
        runtimeMinutes: Int? = null
    ) = DiaryEntryEntity(
        id = id,
        titleId = titleId,
        mediaType = mediaType,
        titleName = titleName,
        posterPath = null,
        watchedOn = watchedOn,
        rating = rating,
        mood = mood,
        note = "",
        watchedWith = watchedWith,
        status = "COMPLETED",
        seasonNumber = null,
        episodeNumber = null,
        rewatchOf = rewatchOf,
        genreIds = genreIds,
        country = country,
        runtimeMinutes = runtimeMinutes
    )

    @Test
    fun `availableYears returns distinct years newest first`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2024, 5, 1)),
            entry(id = 2, watchedOn = at(2026, 1, 2)),
            entry(id = 3, watchedOn = at(2024, 9, 9)),
            entry(id = 4, watchedOn = at(2025, 3, 3))
        )

        assertEquals(listOf(2026, 2025, 2024), RecapCalculator.availableYears(entries))
    }

    @Test
    fun `no entries in target year produce empty recap`() {
        val entries = listOf(entry(id = 1, watchedOn = at(2025, 5, 1)))

        val recap = RecapCalculator.calculate(entries, 2026)

        assertEquals(2026, recap.year)
        assertTrue(recap.isEmpty)
        assertEquals(0, recap.entryCount)
    }

    @Test
    fun `totals count only target year and distinct titles`() {
        val entries = listOf(
            entry(id = 1, titleId = 1, mediaType = "movie", watchedOn = at(2026, 1, 1), runtimeMinutes = 100),
            entry(id = 2, titleId = 1, mediaType = "movie", watchedOn = at(2026, 2, 1), runtimeMinutes = 20),
            entry(id = 3, titleId = 1, mediaType = "tv", watchedOn = at(2026, 3, 1), runtimeMinutes = 40),
            entry(id = 4, titleId = 9, watchedOn = at(2025, 3, 1), runtimeMinutes = 999)
        )

        val recap = RecapCalculator.calculate(entries, 2026)

        assertEquals(2, recap.totalTitles)
        assertEquals(3, recap.entryCount)
        assertEquals(160L, recap.totalMinutes)
        assertEquals(2L, recap.totalHours)
    }

    @Test
    fun `rewatchCount counts entries linked to a previous watch`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2026, 1, 1)),
            entry(id = 2, watchedOn = at(2026, 1, 2), rewatchOf = 1L),
            entry(id = 3, watchedOn = at(2026, 1, 3), rewatchOf = 1L)
        )

        assertEquals(2, RecapCalculator.calculate(entries, 2026).rewatchCount)
    }

    @Test
    fun `topGenres aggregates and sorts desc limited to three`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2026, 1, 1), genreIds = "28, 878"),
            entry(id = 2, watchedOn = at(2026, 1, 2), genreIds = "28"),
            entry(id = 3, watchedOn = at(2026, 1, 3), genreIds = "16, 35, 80, 18")
        )

        val recap = RecapCalculator.calculate(entries, 2026)

        assertEquals(3, recap.topGenres.size)
        assertEquals(CountItem("Action", 2), recap.topGenres.first())
    }

    @Test
    fun `topMood picks most frequent mood`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2026, 1, 1), mood = "TENSE"),
            entry(id = 2, watchedOn = at(2026, 1, 2), mood = "TENSE"),
            entry(id = 3, watchedOn = at(2026, 1, 3), mood = "HAPPY")
        )

        assertEquals(Mood.TENSE, RecapCalculator.calculate(entries, 2026).topMood)
    }

    @Test
    fun `topTitle is the most frequently watched title`() {
        val entries = listOf(
            entry(id = 1, titleId = 5, titleName = "Parasite", watchedOn = at(2026, 1, 1)),
            entry(id = 2, titleId = 5, titleName = "Parasite", watchedOn = at(2026, 2, 1)),
            entry(id = 3, titleId = 7, titleName = "Dune", watchedOn = at(2026, 3, 1))
        )

        val top = RecapCalculator.calculate(entries, 2026).topTitle

        assertEquals("Parasite", top?.name)
        assertEquals(2, top?.count)
    }

    @Test
    fun `highestRated ignores unrated entries`() {
        val entries = listOf(
            entry(id = 1, titleId = 1, titleName = "A", watchedOn = at(2026, 1, 1), rating = 0.0),
            entry(id = 2, titleId = 2, titleName = "B", watchedOn = at(2026, 1, 2), rating = 4.5),
            entry(id = 3, titleId = 3, titleName = "C", watchedOn = at(2026, 1, 3), rating = 5.0)
        )

        assertEquals("C", RecapCalculator.calculate(entries, 2026).highestRated?.name)
    }

    @Test
    fun `highestRated is null when nothing rated`() {
        val entries = listOf(entry(id = 1, watchedOn = at(2026, 1, 1), rating = 0.0))

        assertNull(RecapCalculator.calculate(entries, 2026).highestRated)
    }

    @Test
    fun `busiestMonth uses one based month numbering`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2026, 3, 1)),
            entry(id = 2, watchedOn = at(2026, 3, 15)),
            entry(id = 3, watchedOn = at(2026, 7, 1))
        )

        val recap = RecapCalculator.calculate(entries, 2026)

        assertEquals(3, recap.busiestMonth)
        assertEquals(2, recap.busiestMonthCount)
    }

    @Test
    fun `longestStreak counts consecutive watched days`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2026, 1, 1)),
            entry(id = 2, watchedOn = at(2026, 1, 2)),
            entry(id = 3, watchedOn = at(2026, 1, 3)),
            entry(id = 4, watchedOn = at(2026, 1, 10))
        )

        assertEquals(3, RecapCalculator.calculate(entries, 2026).longestStreak)
    }

    @Test
    fun `topCompanion ignores blank names`() {
        val entries = listOf(
            entry(id = 1, watchedOn = at(2026, 1, 1), watchedWith = "sama teman kos"),
            entry(id = 2, watchedOn = at(2026, 1, 2), watchedWith = "sama teman kos"),
            entry(id = 3, watchedOn = at(2026, 1, 3), watchedWith = "  ")
        )

        assertEquals("sama teman kos", RecapCalculator.calculate(entries, 2026).topCompanion)
    }
}
