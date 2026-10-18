package com.today.app.ui.social

import com.today.app.core.domain.model.MediaType
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionFlow = MutableStateFlow<UserSession?>(null)
    private val commentsFlow = MutableStateFlow<List<Comment>>(emptyList())
    private val isLikedFlow = MutableStateFlow(false)

    private val auth = object : AuthRepository {
        override fun observeSession(): Flow<UserSession?> = sessionFlow
        override fun currentUser(): UserSession? = sessionFlow.value
        override suspend fun signUpWithEmail(email: String, password: String, displayName: String) =
            Result.failure<UserSession>(UnsupportedOperationException())
        override suspend fun signInWithEmail(email: String, password: String) =
            Result.failure<UserSession>(UnsupportedOperationException())
        override suspend fun signInWithGoogle(idToken: String) =
            Result.failure<UserSession>(UnsupportedOperationException())
        override suspend fun sendPasswordReset(email: String) = Result.success(Unit)
        override suspend fun signOut() = Result.success(Unit)
        override suspend fun deleteAccount() = Result.success(Unit)
    }

    private val addCommentCalls = mutableListOf<Triple<String, String, Boolean>>()
    private val deleteCommentCalls = mutableListOf<Pair<String, String>>()
    private val setLikeCalls = mutableListOf<Pair<String, Boolean>>()

    private val social = object : SocialRepository {
        override suspend fun getProfile(uid: String) = Result.success<UserProfile?>(null)
        override suspend fun upsertProfile(profile: UserProfile) = Result.success(Unit)
        override suspend fun publishOpinion(opinion: Opinion) = Result.success("id")
        override suspend fun deleteOpinion(opinionId: String) = Result.success(Unit)
        override fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int): Flow<List<Opinion>> =
            flowOf(emptyList())
        override fun observeAllOpinions(limit: Int): Flow<List<Opinion>> = flowOf(emptyList())
        override fun observeUserOpinions(uid: String, limit: Int): Flow<List<Opinion>> = flowOf(emptyList())
        override suspend fun setLike(opinionId: String, liked: Boolean): Result<Unit> {
            setLikeCalls += opinionId to liked
            return Result.success(Unit)
        }
        override fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean> = isLikedFlow
        override suspend fun isLiked(opinionId: String, uid: String): Result<Boolean> = Result.success(isLikedFlow.value)
        override fun observeComments(opinionId: String): Flow<List<Comment>> = commentsFlow
        override suspend fun addComment(opinionId: String, text: String, spoiler: Boolean): Result<Comment> {
            addCommentCalls += Triple(opinionId, text, spoiler)
            val comment = Comment(
                id = "comm-1",
                opinionId = opinionId,
                authorUid = "uid",
                authorName = "User",
                text = text,
                spoiler = spoiler,
                createdAt = 1_000L
            )
            return Result.success(comment)
        }
        override suspend fun deleteComment(opinionId: String, commentId: String): Result<Unit> {
            deleteCommentCalls += opinionId to commentId
            return Result.success(Unit)
        }
        override fun observeFollowing(uid: String): Flow<List<String>> = flowOf(emptyList())
        override suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean) =
            Result.success(Unit)
    }

    private val dummyOpinion = Opinion(
        id = "op-99",
        authorUid = "author-1",
        authorName = "Author",
        titleId = 1,
        mediaType = MediaType.MOVIE,
        titleName = "Film Keren",
        posterPath = null,
        rating = 4.0,
        mood = null,
        note = "Bagus",
        spoiler = false,
        likeCount = 0,
        commentCount = 0,
        createdAt = 1_000L
    )

    @Test
    fun `selectOpinion updates state and clears previous messages`() = runTest {
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        assertEquals("op-99", viewModel.state.value.opinion?.id)
        assertTrue(viewModel.state.value.revealedCommentIds.isEmpty())
        assertNull(viewModel.state.value.message)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `dismiss resets opinion to null`() = runTest {
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)
        assertEquals("op-99", viewModel.state.value.opinion?.id)

        viewModel.dismiss()
        assertNull(viewModel.state.value.opinion)
    }

    @Test
    fun `toggleSpoiler reveals comment then hides it on subsequent toggle`() = runTest {
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        viewModel.toggleSpoiler("c-1")
        assertTrue("c-1" in viewModel.state.value.revealedCommentIds)

        viewModel.toggleSpoiler("c-1")
        assertFalse("c-1" in viewModel.state.value.revealedCommentIds)
    }

    @Test
    fun `addComment submits trimmed text with spoiler flag`() = runTest {
        sessionFlow.value = UserSession(uid = "uid-1", email = "test@example.com", displayName = "Test", photoUrl = null)
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        viewModel.addComment("  Keren banget endingnya!  ", spoiler = true)

        assertEquals(1, addCommentCalls.size)
        val (opId, text, spoiler) = addCommentCalls.first()
        assertEquals("op-99", opId)
        assertEquals("Keren banget endingnya!", text)
        assertTrue(spoiler)
        assertEquals("Komentar terkirim", viewModel.state.value.message)
    }

    @Test
    fun `addComment ignores blank input`() = runTest {
        sessionFlow.value = UserSession(uid = "uid-1", email = "test@example.com", displayName = "Test", photoUrl = null)
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        viewModel.addComment("   ", spoiler = false)

        assertEquals(0, addCommentCalls.size)
    }

    @Test
    fun `addComment requires signed in user`() = runTest {
        sessionFlow.value = null
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        viewModel.addComment("Halo", spoiler = false)

        assertEquals(0, addCommentCalls.size)
        assertEquals("Masuk dulu untuk berkomentar.", viewModel.state.value.error)
    }

    @Test
    fun `deleteComment invokes repo with comment id`() = runTest {
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        viewModel.deleteComment("comm-123")

        assertEquals(1, deleteCommentCalls.size)
        assertEquals("op-99" to "comm-123", deleteCommentCalls.first())
        assertEquals("Komentar dihapus", viewModel.state.value.message)
    }

    @Test
    fun `toggleLike calls repo and respects current like status`() = runTest {
        sessionFlow.value = UserSession(uid = "uid-1", email = "test@example.com", displayName = "Test", photoUrl = null)
        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        viewModel.toggleLike()

        assertEquals(1, setLikeCalls.size)
        assertEquals("op-99" to true, setLikeCalls.first())
    }

    @Test
    fun `comments flow emits comments from socialRepository`() = runTest {
        val commentList = listOf(
            Comment(
                id = "c-1",
                opinionId = "op-99",
                authorUid = "uid",
                authorName = "User",
                text = "Mantap!",
                spoiler = false,
                createdAt = 1_000L
            )
        )
        commentsFlow.value = commentList

        val viewModel = CommentsViewModel(auth, social)
        viewModel.selectOpinion(dummyOpinion)

        val emitted = viewModel.comments.first { it.isNotEmpty() }
        assertEquals(1, emitted.size)
        assertEquals("Mantap!", emitted.first().text)
    }
}
