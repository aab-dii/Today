package com.today.app.social.domain.mapper

import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import org.junit.Assert.assertEquals
import org.junit.Test

class OpinionMapperTest {

    private val entry = DiaryEntry(
        id = 5,
        titleId = 42,
        mediaType = MediaType.TV,
        titleName = "Severance",
        posterPath = "/poster.jpg",
        watchedOn = 1_700_000_000_000L,
        rating = 4.5,
        mood = Mood.MOVED,
        note = "catatan pribadi"
    )

    @Test
    fun `maps diary entry snapshot to opinion`() {
        val opinion = OpinionMapper.fromDiaryEntry(
            entry = entry,
            authorUid = "uid-1",
            authorName = "Rina",
            note = "opini publik",
            spoiler = true,
            createdAt = 999L
        )

        assertEquals("", opinion.id)
        assertEquals("uid-1", opinion.authorUid)
        assertEquals("Rina", opinion.authorName)
        assertEquals(42, opinion.titleId)
        assertEquals(MediaType.TV, opinion.mediaType)
        assertEquals("Severance", opinion.titleName)
        assertEquals("/poster.jpg", opinion.posterPath)
        assertEquals(4.5, opinion.rating, 0.0)
        assertEquals(Mood.MOVED, opinion.mood)
        assertEquals("opini publik", opinion.note)
        assertEquals(true, opinion.spoiler)
        assertEquals(999L, opinion.createdAt)
        assertEquals(0, opinion.likeCount)
        assertEquals(0, opinion.commentCount)
    }

    @Test
    fun `trims note and starts without engagement`() {
        val opinion = OpinionMapper.fromDiaryEntry(
            entry = entry,
            authorUid = "uid-1",
            authorName = "Rina",
            note = "   ada spasi   ",
            spoiler = false,
            createdAt = 1L
        )

        assertEquals("ada spasi", opinion.note)
        assertEquals(false, opinion.spoiler)
        assertEquals(0, opinion.likeCount)
        assertEquals(0, opinion.commentCount)
    }
}
