package com.today.app.ui.social

import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OpinionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionFlow = MutableStateFlow<UserSession?>(null)

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

    private val published = mutableListOf<Opinion>()

    private val social = object : SocialRepository {
        override suspend fun getProfile(uid: String) = Result.success<UserProfile?>(null)
        override suspend fun upsertProfile(profile: UserProfile) = Result.success(Unit)
        override suspend fun publishOpinion(opinion: Opinion): Result<String> {
            published += opinion
            return Result.success("new-id")
        }

        override suspend fun deleteOpinion(opinionId: String) = Result.success(Unit)
        override fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int): Flow<List<Opinion>> =
            flowOf(emptyList())
        override fun observeAllOpinions(limit: Int): Flow<List<Opinion>> = flowOf(emptyList())
        override fun observeUserOpinions(uid: String, limit: Int): Flow<List<Opinion>> =
            flowOf(emptyList())
        override suspend fun setLike(opinionId: String, liked: Boolean) = Result.success(Unit)
        override fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean> = flowOf(false)
        override suspend fun isLiked(opinionId: String, uid: String): Result<Boolean> = Result.success(false)
        override fun observeComments(opinionId: String): Flow<List<Comment>> = flowOf(emptyList())
        override suspend fun addComment(opinionId: String, text: String, spoiler: Boolean) =
            Result.failure<Comment>(UnsupportedOperationException())
        override suspend fun deleteComment(opinionId: String, commentId: String): Result<Unit> = Result.success(Unit)
        override fun observeFollowing(uid: String): Flow<List<String>> = flowOf(emptyList())
        override suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean) =
            Result.success(Unit)
    }

    private val entry = DiaryEntry(
        id = 1,
        titleId = 42,
        mediaType = MediaType.TV,
        titleName = "Severance",
        posterPath = "/p.jpg",
        watchedOn = 1_700_000_000_000L,
        rating = 4.5,
        mood = Mood.MOVED,
        note = "catatan"
    )

    @Test
    fun `publish shares opinion using session display name`() = runTest {
        sessionFlow.value = UserSession(uid = "uid-1", email = "rina@example.com", displayName = "Rina", photoUrl = null)
        val viewModel = OpinionViewModel(auth, social)

        viewModel.publish(entry, note = "opini keren", spoiler = true)

        assertEquals(1, published.size)
        assertEquals("uid-1", published.first().authorUid)
        assertEquals("Rina", published.first().authorName)
        assertEquals("opini keren", published.first().note)
        assertEquals(true, published.first().spoiler)
        assertEquals("Opini dibagikan", viewModel.state.value.message)
    }

    @Test
    fun `publish falls back to email prefix when display name missing`() = runTest {
        sessionFlow.value = UserSession(uid = "uid-2", email = "budi@example.com", displayName = null, photoUrl = null)
        val viewModel = OpinionViewModel(auth, social)

        viewModel.publish(entry, note = "mantap", spoiler = false)

        assertEquals("budi", published.first().authorName)
    }

    @Test
    fun `publish without session reports error and does not publish`() = runTest {
        sessionFlow.value = null
        val viewModel = OpinionViewModel(auth, social)

        viewModel.publish(entry, note = "x", spoiler = false)

        assertEquals(0, published.size)
        assertEquals("Masuk dulu untuk membagikan opini.", viewModel.state.value.error)
    }
}
