package com.today.app.social.data.mapper

import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.social.data.mapper.SocialMapper.toDoc
import com.today.app.social.data.mapper.SocialMapper.toDomain
import com.today.app.social.data.model.CircleDoc
import com.today.app.social.data.model.CircleMemberDoc
import com.today.app.social.data.model.CommentDoc
import com.today.app.social.data.model.OpinionDoc
import com.today.app.social.data.model.UserDoc
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SocialMapperTest {

    @Test
    fun `user doc domain round trip preserves values`() {
        val doc = UserDoc(
            displayName = "Rina",
            photoUrl = "https://img/p.png",
            bio = "suka K-drama",
            createdAt = 100L,
            updatedAt = 200L
        )

        val profile = doc.toDomain("uid-1")

        assertEquals("uid-1", profile.uid)
        assertEquals("Rina", profile.displayName)
        assertEquals("suka K-drama", profile.bio)
        assertEquals("https://img/p.png", profile.photoUrl)
        assertEquals(doc, profile.toDoc())
    }

    @Test
    fun `opinion doc maps media type and mood`() {
        val doc = OpinionDoc(
            authorUid = "uid-1",
            authorName = "Rina",
            titleId = 42,
            mediaType = "tv",
            titleName = "Severance",
            rating = 4.5,
            mood = "MOVED",
            note = "keren",
            spoiler = true,
            createdAt = 300L,
            likeCount = 2,
            commentCount = 1
        )

        val opinion = doc.toDomain("opinion-1", likedByMe = true)

        assertEquals("opinion-1", opinion.id)
        assertEquals(MediaType.TV, opinion.mediaType)
        assertEquals(Mood.MOVED, opinion.mood)
        assertEquals("Severance", opinion.titleName)
        assertEquals(4.5, opinion.rating, 0.0)
        assertEquals(true, opinion.spoiler)
        assertEquals(true, opinion.likedByMe)
    }

    @Test
    fun `opinion doc falls back when unknown media type and mood`() {
        val doc = OpinionDoc(mediaType = "person", mood = "UNKNOWN")

        val opinion = doc.toDomain("x")

        assertEquals(MediaType.MOVIE, opinion.mediaType)
        assertNull(opinion.mood)
    }

    @Test
    fun `opinion domain doc round trip preserves values`() {
        val opinion = Opinion(
            id = "op-1",
            authorUid = "uid-2",
            authorName = "Budi",
            titleId = 7,
            mediaType = MediaType.MOVIE,
            titleName = "Dune",
            posterPath = "/p",
            rating = 5.0,
            mood = Mood.AMAZED,
            note = "epik",
            spoiler = false,
            createdAt = 500L,
            likeCount = 9,
            commentCount = 3
        )

        val back = opinion.toDoc().toDomain("op-1")

        assertEquals(opinion.id, back.id)
        assertEquals(opinion.mediaType, back.mediaType)
        assertEquals(opinion.mood, back.mood)
        assertEquals(opinion.authorName, back.authorName)
        assertEquals(opinion.rating, back.rating, 0.0)
        assertEquals(opinion.likeCount, back.likeCount)
    }

    @Test
    fun `comment doc maps with external ids`() {
        val doc = CommentDoc(
            authorUid = "uid-3",
            authorName = "Citra",
            text = "setuju!",
            spoiler = true,
            createdAt = 700L
        )

        val comment = doc.toDomain("c-1", "op-9")

        assertEquals("c-1", comment.id)
        assertEquals("op-9", comment.opinionId)
        assertEquals("Citra", comment.authorName)
        assertEquals(true, comment.spoiler)
        assertEquals(700L, comment.createdAt)
    }

    @Test
    fun `user profile to doc omits uid`() {
        val profile = UserProfile(
            uid = "uid-9",
            displayName = "Dewi",
            photoUrl = null,
            bio = "",
            createdAt = 1L,
            updatedAt = 2L
        )

        val doc: UserDoc = profile.toDoc()

        assertEquals("Dewi", doc.displayName)
        assertNull(doc.photoUrl)
    }

    @Test
    fun `circle doc maps with external id`() {
        val doc = CircleDoc(
            name = "Nobar Jumat",
            ownerUid = "uid-1",
            ownerName = "Rina",
            inviteCode = "ABC123",
            memberUids = listOf("uid-1", "uid-2"),
            memberCount = 2,
            createdAt = 900L
        )

        val circle = doc.toDomain("circle-1")

        assertEquals("circle-1", circle.id)
        assertEquals("Nobar Jumat", circle.name)
        assertEquals("uid-1", circle.ownerUid)
        assertEquals("ABC123", circle.inviteCode)
        assertEquals(2, circle.memberCount)
    }

    @Test
    fun `circle domain doc round trip preserves values`() {
        val circle = Circle(
            id = "circle-2",
            name = "Anime Club",
            ownerUid = "uid-5",
            ownerName = "Budi",
            inviteCode = "ZX9K2P",
            memberCount = 4,
            createdAt = 1000L
        )

        val back = circle.toDoc().toDomain("circle-2")

        assertEquals(circle.name, back.name)
        assertEquals(circle.ownerUid, back.ownerUid)
        assertEquals(circle.inviteCode, back.inviteCode)
        assertEquals(circle.memberCount, back.memberCount)
        assertEquals(circle.createdAt, back.createdAt)
    }

    @Test
    fun `circle member doc maps with external uid`() {
        val doc = CircleMemberDoc(name = "Citra", joinedAt = 1234L)

        val member = doc.toDomain("uid-3")

        assertEquals("uid-3", member.uid)
        assertEquals("Citra", member.name)
        assertEquals(1234L, member.joinedAt)
    }
}
