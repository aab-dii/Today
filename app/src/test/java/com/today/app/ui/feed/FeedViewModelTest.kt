package com.today.app.ui.feed

import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.SocialRepository
import com.today.app.ui.social.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionFlow = MutableStateFlow<UserSession?>(null)
    private val followingFlow = MutableStateFlow<List<String>>(emptyList())

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

    private val followCalls = mutableListOf<Triple<String, String, Boolean>>()
    private val likeCalls = mutableListOf<Pair<String, Boolean>>()
    private var setLikeResult: Result<Unit> = Result.success(Unit)

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
            likeCalls += opinionId to liked
            return setLikeResult
        }
        override fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean> = flowOf(false)
        override suspend fun isLiked(opinionId: String, uid: String): Result<Boolean> = Result.success(false)
        override fun observeComments(opinionId: String): Flow<List<Comment>> = flowOf(emptyList())
        override suspend fun addComment(opinionId: String, text: String, spoiler: Boolean) =
            Result.failure<Comment>(UnsupportedOperationException())
        override suspend fun deleteComment(opinionId: String, commentId: String): Result<Unit> = Result.success(Unit)
        override fun observeFollowing(uid: String): Flow<List<String>> = followingFlow
        override suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean): Result<Unit> {
            followCalls += Triple(followerUid, targetUid, following)
            return Result.success(Unit)
        }
    }

    private val dummyOpinion = Opinion(
        id = "op-1",
        authorUid = "author-1",
        authorName = "Author",
        titleId = 1,
        mediaType = com.today.app.core.domain.model.MediaType.MOVIE,
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
    fun `setMode updates state mode`() = runTest {
        val viewModel = FeedViewModel(auth, social)
        assertEquals(FeedMode.FOR_YOU, viewModel.state.value.mode)

        viewModel.setMode(FeedMode.FOLLOWING)
        assertEquals(FeedMode.FOLLOWING, viewModel.state.value.mode)
    }

    @Test
    fun `toggleFollow follows unfollowed user when signed in`() = runTest {
        sessionFlow.value = UserSession(uid = "my-uid", email = "test@example.com", displayName = "Me", photoUrl = null)
        followingFlow.value = emptyList()
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleFollow("author-123")

        assertEquals(1, followCalls.size)
        assertEquals("my-uid", followCalls.first().first)
        assertEquals("author-123", followCalls.first().second)
        assertTrue(followCalls.first().third)
        assertEquals("Mengikuti", viewModel.state.value.message)
    }

    @Test
    fun `toggleFollow unfollows already followed user`() = runTest {
        sessionFlow.value = UserSession(uid = "my-uid", email = "test@example.com", displayName = "Me", photoUrl = null)
        followingFlow.value = listOf("author-123")
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleFollow("author-123")

        assertEquals(1, followCalls.size)
        assertEquals("my-uid", followCalls.first().first)
        assertEquals("author-123", followCalls.first().second)
        assertFalse(followCalls.first().third)
        assertEquals("Berhenti mengikuti", viewModel.state.value.message)
    }

    @Test
    fun `toggleFollow on self reports error and does not call repo`() = runTest {
        sessionFlow.value = UserSession(uid = "my-uid", email = "test@example.com", displayName = "Me", photoUrl = null)
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleFollow("my-uid")

        assertEquals(0, followCalls.size)
        assertEquals("Tidak bisa mengikuti akun sendiri.", viewModel.state.value.error)
    }

    @Test
    fun `toggleFollow without session reports error and does not call repo`() = runTest {
        sessionFlow.value = null
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleFollow("author-123")

        assertEquals(0, followCalls.size)
        assertEquals("Masuk dulu untuk mengikuti akun.", viewModel.state.value.error)
    }

    @Test
    fun `toggleLike adds opinion to liked set and invokes repo`() = runTest {
        sessionFlow.value = UserSession(uid = "my-uid", email = "test@example.com", displayName = "Me", photoUrl = null)
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleLike(dummyOpinion)

        assertTrue(viewModel.state.value.isLiked("op-1"))
        assertEquals(1, likeCalls.size)
        assertEquals("op-1" to true, likeCalls.first())
    }

    @Test
    fun `toggleLike unlikes opinion when already liked`() = runTest {
        sessionFlow.value = UserSession(uid = "my-uid", email = "test@example.com", displayName = "Me", photoUrl = null)
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleLike(dummyOpinion)
        assertTrue(viewModel.state.value.isLiked("op-1"))

        viewModel.toggleLike(dummyOpinion)
        assertFalse(viewModel.state.value.isLiked("op-1"))
        assertEquals(2, likeCalls.size)
        assertEquals("op-1" to false, likeCalls[1])
    }

    @Test
    fun `toggleLike rolls back state on failure`() = runTest {
        sessionFlow.value = UserSession(uid = "my-uid", email = "test@example.com", displayName = "Me", photoUrl = null)
        setLikeResult = Result.failure(RuntimeException("Firestore error"))
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleLike(dummyOpinion)

        assertFalse(viewModel.state.value.isLiked("op-1"))
        assertEquals("Gagal memperbarui suka. Coba lagi.", viewModel.state.value.error)
    }

    @Test
    fun `toggleLike without session reports error and does not call repo`() = runTest {
        sessionFlow.value = null
        val viewModel = FeedViewModel(auth, social)

        viewModel.toggleLike(dummyOpinion)

        assertEquals(0, likeCalls.size)
        assertEquals("Masuk dulu untuk menyukai opini.", viewModel.state.value.error)
    }
}
