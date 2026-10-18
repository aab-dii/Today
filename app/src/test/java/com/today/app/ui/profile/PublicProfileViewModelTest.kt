package com.today.app.ui.profile

import com.today.app.core.domain.model.MediaType
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
class PublicProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionFlow = MutableStateFlow<UserSession?>(null)
    private val followingFlow = MutableStateFlow<List<String>>(emptyList())
    private val userOpinionsFlow = MutableStateFlow<List<Opinion>>(emptyList())

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
    private var targetProfileResult: Result<UserProfile?> = Result.success(null)

    private val social = object : SocialRepository {
        override suspend fun getProfile(uid: String): Result<UserProfile?> = targetProfileResult
        override suspend fun upsertProfile(profile: UserProfile) = Result.success(Unit)
        override suspend fun publishOpinion(opinion: Opinion) = Result.success("id")
        override suspend fun deleteOpinion(opinionId: String) = Result.success(Unit)
        override fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int): Flow<List<Opinion>> =
            flowOf(emptyList())
        override fun observeAllOpinions(limit: Int): Flow<List<Opinion>> = flowOf(emptyList())
        override fun observeUserOpinions(uid: String, limit: Int): Flow<List<Opinion>> = userOpinionsFlow
        override suspend fun setLike(opinionId: String, liked: Boolean): Result<Unit> {
            likeCalls += opinionId to liked
            return setLikeResult
        }
        override fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean> = flowOf(false)
        override suspend fun isLiked(opinionId: String, uid: String): Result<Boolean> = Result.success(false)
        override fun observeComments(opinionId: String): Flow<List<Comment>> = flowOf(emptyList())
        override suspend fun addComment(opinionId: String, text: String, spoiler: Boolean) =
            Result.failure<Comment>(UnsupportedOperationException())
        override suspend fun deleteComment(opinionId: String, commentId: String) = Result.success(Unit)
        override fun observeFollowing(uid: String): Flow<List<String>> = followingFlow
        override suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean): Result<Unit> {
            followCalls += Triple(followerUid, targetUid, following)
            return Result.success(Unit)
        }
    }

    private val dummyOpinion = Opinion(
        id = "op-22",
        authorUid = "target-uid",
        authorName = "Target User",
        titleId = 10,
        mediaType = MediaType.MOVIE,
        titleName = "Parasite",
        posterPath = null,
        rating = 5.0,
        mood = null,
        note = "Masterpiece",
        spoiler = false,
        likeCount = 0,
        commentCount = 0,
        createdAt = 1_000L
    )

    @Test
    fun `initProfile loads remote profile and sets display name`() = runTest {
        targetProfileResult = Result.success(
            UserProfile(
                uid = "target-uid",
                displayName = "Budi Sutanto",
                photoUrl = "https://example.com/p.jpg",
                bio = "Pencinta sinema",
                createdAt = 100L,
                updatedAt = 200L
            )
        )
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("target-uid", fallbackName = "Fallback Budi")

        assertEquals("Budi Sutanto", viewModel.state.value.displayName)
        assertEquals("Pencinta sinema", viewModel.state.value.profile?.bio)
        assertFalse(viewModel.state.value.loading)
    }

    @Test
    fun `initProfile uses fallbackName when remote profile is null`() = runTest {
        targetProfileResult = Result.success(null)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("target-uid", fallbackName = "Siti")

        assertEquals("Siti", viewModel.state.value.displayName)
        assertNull(viewModel.state.value.profile)
    }

    @Test
    fun `isSelf is true when logged in user equals targetUid`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("user-1", fallbackName = "User 1")

        assertTrue(viewModel.state.value.isSelf)
    }

    @Test
    fun `isSelf is false when targetUid is different from session`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("user-2", fallbackName = "User 2")

        assertFalse(viewModel.state.value.isSelf)
    }

    @Test
    fun `toggleFollow follows unfollowed user when signed in`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("user-2", fallbackName = "User 2")

        viewModel.toggleFollow()

        assertEquals(1, followCalls.size)
        assertEquals("user-1", followCalls.first().first)
        assertEquals("user-2", followCalls.first().second)
        assertTrue(followCalls.first().third)
        assertTrue(viewModel.state.value.isFollowing)
        assertEquals("Mengikuti", viewModel.state.value.message)
    }

    @Test
    fun `toggleFollow unfollows already followed user`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        followingFlow.value = listOf("user-2")
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("user-2", fallbackName = "User 2")

        viewModel.toggleFollow()

        assertEquals(1, followCalls.size)
        assertEquals("user-1", followCalls.first().first)
        assertEquals("user-2", followCalls.first().second)
        assertFalse(followCalls.first().third)
        assertFalse(viewModel.state.value.isFollowing)
        assertEquals("Berhenti mengikuti", viewModel.state.value.message)
    }

    @Test
    fun `toggleFollow on self reports error and does not call repo`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("user-1", fallbackName = "User 1")

        viewModel.toggleFollow()

        assertEquals(0, followCalls.size)
        assertEquals("Tidak bisa mengikuti akun sendiri.", viewModel.state.value.error)
    }

    @Test
    fun `toggleFollow without session reports error and does not call repo`() = runTest {
        sessionFlow.value = null
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("user-2", fallbackName = "User 2")

        viewModel.toggleFollow()

        assertEquals(0, followCalls.size)
        assertEquals("Masuk dulu untuk mengikuti akun.", viewModel.state.value.error)
    }

    @Test
    fun `toggleLike toggles like state and calls repo`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("target-uid", fallbackName = "Target")

        viewModel.toggleLike(dummyOpinion)

        assertTrue(viewModel.state.value.isLiked("op-22"))
        assertEquals(1, likeCalls.size)
        assertEquals("op-22" to true, likeCalls.first())

        viewModel.toggleLike(dummyOpinion)
        assertFalse(viewModel.state.value.isLiked("op-22"))
        assertEquals(2, likeCalls.size)
        assertEquals("op-22" to false, likeCalls[1])
    }

    @Test
    fun `toggleLike rolls back state on repo failure`() = runTest {
        sessionFlow.value = UserSession(uid = "user-1", email = "u1@example.com", displayName = "User 1", photoUrl = null)
        setLikeResult = Result.failure(RuntimeException("Remote error"))
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("target-uid", fallbackName = "Target")

        viewModel.toggleLike(dummyOpinion)

        assertFalse(viewModel.state.value.isLiked("op-22"))
        assertEquals("Gagal memperbarui suka. Coba lagi.", viewModel.state.value.error)
    }

    @Test
    fun `opinions flow emits user opinions for target user`() = runTest {
        userOpinionsFlow.value = listOf(dummyOpinion)
        val viewModel = PublicProfileViewModel(auth, social)
        viewModel.initProfile("target-uid", fallbackName = "Target")

        val emitted = viewModel.opinions.first { it.isNotEmpty() }
        assertEquals(1, emitted.size)
        assertEquals("Parasite", emitted.first().titleName)
    }
}
