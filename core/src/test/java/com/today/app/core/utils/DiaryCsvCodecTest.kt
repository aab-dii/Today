package com.today.app.core.utils

import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DiaryCsvCodecTest {

    private fun at(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day)
        }.timeInMillis

    private fun entry(
        id: Long = 0,
        titleId: Int = 10,
        mediaType: MediaType = MediaType.MOVIE,
        titleName: String = "Parasite",
        watchedOn: Long = at(2026, 5, 1),
        rating: Double = 4.5,
        mood: Mood? = Mood.TENSE,
        note: String = "",
        watchedWith: String? = "teman kos",
        status: WatchStatus = WatchStatus.COMPLETED,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null
    ) = DiaryEntry(
        id = id,
        titleId = titleId,
        mediaType = mediaType,
        titleName = titleName,
        posterPath = null,
        watchedOn = watchedOn,
        rating = rating,
        mood = mood,
        note = note,
        watchedWith = watchedWith,
        status = status,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    )

    @Test
    fun `export writes header and one row per entry`() {
        val csv = DiaryCsvCodec.export(listOf(entry(), entry(titleId = 11, titleName = "Dune")))

        val lines = csv.trim().split("\n")
        assertEquals(3, lines.size)
        assertTrue(lines[0].startsWith("titleId,mediaType,titleName,watchedOn,watchedDate"))
    }

    @Test
    fun `round trip preserves entry fields`() {
        val original = entry(
            titleId = 42,
            mediaType = MediaType.TV,
            titleName = "Reply 1988",
            rating = 5.0,
            mood = Mood.MOVED,
            note = "manis, hangat",
            watchedWith = "keluarga",
            status = WatchStatus.WATCHING,
            seasonNumber = 2,
            episodeNumber = 5
        )

        val parsed = DiaryCsvCodec.parse(DiaryCsvCodec.export(listOf(original)))

        assertEquals(1, parsed.entries.size)
        val result = parsed.entries.first()
        assertEquals(42, result.titleId)
        assertEquals(MediaType.TV, result.mediaType)
        assertEquals("Reply 1988", result.titleName)
        assertEquals(original.watchedOn, result.watchedOn)
        assertEquals(5.0, result.rating, 0.0)
        assertEquals(Mood.MOVED, result.mood)
        assertEquals("manis, hangat", result.note)
        assertEquals("keluarga", result.watchedWith)
        assertEquals(WatchStatus.WATCHING, result.status)
        assertEquals(2, result.seasonNumber)
        assertEquals(5, result.episodeNumber)
    }

    @Test
    fun `round trip tolerates missing optional fields`() {
        val original = entry(mood = null, note = "", watchedWith = null)

        val result = DiaryCsvCodec.parse(DiaryCsvCodec.export(listOf(original))).entries.first()

        assertNull(result.mood)
        assertNull(result.watchedWith)
        assertEquals("", result.note)
        assertEquals(WatchStatus.COMPLETED, result.status)
    }

    @Test
    fun `imports letterboxd diary csv`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating,Rewatch,Watched Date
            2026-01-01,Parasite,2019,https://boxd.it/abc,4.5,No,2026-01-02
            2026-02-01,Dune,2021,,5.0,Yes,2026-02-03
        """.trimIndent()

        val parsed = DiaryCsvCodec.parse(csv)

        assertEquals(2, parsed.entries.size)
        val first = parsed.entries.first()
        assertEquals("Parasite", first.titleName)
        assertEquals(0, first.titleId)
        assertEquals(MediaType.MOVIE, first.mediaType)
        assertEquals(4.5, first.rating, 0.0)
        assertEquals(at(2026, 1, 2), first.watchedOn)
    }

    @Test
    fun `import skips rows without a title name`() {
        val csv = """
            titleId,mediaType,titleName,watchedOn,watchedDate,rating,mood,note,watchedWith,status,seasonNumber,episodeNumber
            1,movie,,"0","2026-01-01",0,,,,COMPLETED,,
            2,movie,Dune,"0","2026-01-02",4.0,,,,COMPLETED,,
        """.trimIndent()

        val parsed = DiaryCsvCodec.parse(csv)

        assertEquals(1, parsed.entries.size)
        assertEquals(1, parsed.skipped)
    }

    @Test
    fun `import uses watched date when millis absent`() {
        val csv = """
            titleId,mediaType,titleName,watchedDate,rating
            7,movie,Perfect Days,2026-03-10,4.0
        """.trimIndent()

        val result = DiaryCsvCodec.parse(csv).entries.first()

        assertEquals(at(2026, 3, 10), result.watchedOn)
    }

    @Test
    fun `unknown header yields no entries and counts skipped`() {
        val csv = "foo,bar\n1,2\n3,4"

        val parsed = DiaryCsvCodec.parse(csv)

        assertTrue(parsed.entries.isEmpty())
        assertEquals(2, parsed.skipped)
    }

    @Test
    fun `empty text yields empty result`() {
        val parsed = DiaryCsvCodec.parse("")

        assertTrue(parsed.entries.isEmpty())
        assertEquals(0, parsed.skipped)
    }
}
