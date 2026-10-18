package com.today.app.social.domain.repository

import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun getProfile(uid: String): Result<UserProfile?>

    suspend fun upsertProfile(profile: UserProfile): Result<Unit>

    suspend fun publishOpinion(opinion: Opinion): Result<String>

    suspend fun deleteOpinion(opinionId: String): Result<Unit>

    fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int = DEFAULT_FEED_LIMIT): Flow<List<Opinion>>

    fun observeAllOpinions(limit: Int = DEFAULT_FEED_LIMIT): Flow<List<Opinion>>

    fun observeUserOpinions(uid: String, limit: Int = DEFAULT_FEED_LIMIT): Flow<List<Opinion>>

    suspend fun setLike(opinionId: String, liked: Boolean): Result<Unit>

    fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean>

    suspend fun isLiked(opinionId: String, uid: String): Result<Boolean>

    fun observeComments(opinionId: String): Flow<List<Comment>>

    suspend fun addComment(opinionId: String, text: String, spoiler: Boolean): Result<Comment>

    suspend fun deleteComment(opinionId: String, commentId: String): Result<Unit>

    fun observeFollowing(uid: String): Flow<List<String>>

    suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean): Result<Unit>

    companion object {
        const val DEFAULT_FEED_LIMIT = 30
    }
}
