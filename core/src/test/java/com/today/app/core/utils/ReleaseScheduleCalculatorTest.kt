package com.today.app.core.utils

import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.NextEpisode
import com.today.app.core.domain.model.TitleDetail
import com.today.app.core.domain.model.UpcomingRelease
import com.today.app.core.domain.model.WatchlistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReleaseScheduleCalculatorTest {

    private fun now(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis

    private fun watchlist(
        titleId: Int,
        mediaType: MediaType,
        name: String = "Judul"
    ) = WatchlistItem(
        titleId = titleId,
        mediaType = mediaType,
        titleName = name,
        posterPath = null,
        addedOn = now(2026, 1, 1)
    )

    private fun detail(
        id: Int,
        mediaType: MediaType,
        name: String = "Judul",
        releaseDate: String = "",
        rating: Double = 0.0,
        nextEpisode: NextEpisode? = null
    ) = TitleDetail(
        id = id,
        mediaType = mediaType,
        name = name,
        overview = "",
        posterPath = null,
        backdropPath = null,
        rating = rating,
        releaseDate = releaseDate,
        genreNames = emptyList(),
        runtimeMinutes = null,
        nextEpisode = nextEpisode
    )

    @Test
    fun `includes future tv episode with label`() {
        val items = listOf(
            watchlist(1, MediaType.TV, "Reply 1988") to detail(
                id = 1,
                mediaType = MediaType.TV,
                rating = 8.7,
                nextEpisode = NextEpisode("2026-10-20", 2, 5, "Pesta Kelulusan")
            )
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17))

        assertEquals(1, result.size)
        assertEquals("S2 E5 · Pesta Kelulusan", result.first().detail)
        assertTrue(result.first().isEpisode)
        assertEquals("2026-10-20", result.first().date)
        assertEquals(8.7, result.first().rating, 0.0)
    }

    @Test
    fun `includes future movie release`() {
        val items = listOf(
            watchlist(2, MediaType.MOVIE, "Dune Part Three") to detail(
                id = 2,
                mediaType = MediaType.MOVIE,
                releaseDate = "2027-03-01"
            )
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17))

        assertEquals(1, result.size)
        assertEquals("Rilis film", result.first().detail)
        assertTrue(!result.first().isEpisode)
    }

    @Test
    fun `excludes past dates`() {
        val items = listOf(
            watchlist(3, MediaType.TV) to detail(
                id = 3,
                mediaType = MediaType.TV,
                nextEpisode = NextEpisode("2026-10-15", 1, 1, "")
            ),
            watchlist(4, MediaType.MOVIE) to detail(
                id = 4,
                mediaType = MediaType.MOVIE,
                releaseDate = "2026-10-16"
            )
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `includes today`() {
        val items = listOf(
            watchlist(5, MediaType.TV) to detail(
                id = 5,
                mediaType = MediaType.TV,
                nextEpisode = NextEpisode("2026-10-17", 1, 2, "")
            )
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17, hour = 23))

        assertEquals(1, result.size)
        assertEquals("S1 E2", result.first().detail)
    }

    @Test
    fun `excludes titles without upcoming date`() {
        val items = listOf(
            watchlist(6, MediaType.TV) to detail(id = 6, mediaType = MediaType.TV),
            watchlist(7, MediaType.MOVIE) to detail(id = 7, mediaType = MediaType.MOVIE)
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `excludes invalid date strings`() {
        val items = listOf(
            watchlist(8, MediaType.MOVIE) to detail(
                id = 8,
                mediaType = MediaType.MOVIE,
                releaseDate = "belum diumumkan"
            )
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `sorts releases ascending by date`() {
        val items = listOf(
            watchlist(9, MediaType.MOVIE, "C") to detail(
                id = 9,
                mediaType = MediaType.MOVIE,
                releaseDate = "2026-12-01"
            ),
            watchlist(10, MediaType.TV, "A") to detail(
                id = 10,
                mediaType = MediaType.TV,
                nextEpisode = NextEpisode("2026-10-18", 1, 1, "")
            ),
            watchlist(11, MediaType.MOVIE, "B") to detail(
                id = 11,
                mediaType = MediaType.MOVIE,
                releaseDate = "2026-11-05"
            )
        )

        val result = ReleaseScheduleCalculator.build(items, now(2026, 10, 17))

        assertEquals(listOf("A", "B", "C"), result.map { it.titleName })
    }

    private fun upcoming(year: Int, month: Int, day: Int, name: String = "Judul") = UpcomingRelease(
        titleId = year + month + day,
        mediaType = MediaType.TV,
        titleName = name,
        posterPath = null,
        rating = 0.0,
        date = "$year-$month-$day",
        dateMillis = now(year, month, day),
        detail = "S1 E1",
        isEpisode = true
    )

    @Test
    fun `due soon keeps today and tomorrow`() {
        val releases = listOf(
            upcoming(2026, 10, 17, "Hari ini"),
            upcoming(2026, 10, 18, "Besok")
        )

        val result = ReleaseScheduleCalculator.dueSoon(releases, now(2026, 10, 17))

        assertEquals(listOf("Hari ini", "Besok"), result.map { it.titleName })
    }

    @Test
    fun `due soon excludes the day after tomorrow`() {
        val releases = listOf(
            upcoming(2026, 10, 17, "Hari ini"),
            upcoming(2026, 10, 19, "Lusa")
        )

        val result = ReleaseScheduleCalculator.dueSoon(releases, now(2026, 10, 17))

        assertEquals(listOf("Hari ini"), result.map { it.titleName })
    }

    @Test
    fun `due soon excludes past releases`() {
        val releases = listOf(
            upcoming(2026, 10, 10, "Lampau"),
            upcoming(2026, 10, 18, "Besok")
        )

        val result = ReleaseScheduleCalculator.dueSoon(releases, now(2026, 10, 17))

        assertEquals(listOf("Besok"), result.map { it.titleName })
    }
}
