package com.today.app.social.data.source

import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface SocialDataSource {
    suspend fun getProfile(uid: String): UserProfile?

    suspend fun setProfile(profile: UserProfile)

    suspend fun publishOpinion(opinion: Opinion): String

    suspend fun deleteOpinion(opinionId: String)

    fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int): Flow<List<Opinion>>

    fun observeAllOpinions(limit: Int): Flow<List<Opinion>>

    fun observeUserOpinions(uid: String, limit: Int): Flow<List<Opinion>>

    suspend fun setLike(opinionId: String, liked: Boolean)

    fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean>

    suspend fun isLiked(opinionId: String, uid: String): Boolean

    fun observeComments(opinionId: String): Flow<List<Comment>>

    suspend fun addComment(opinionId: String, text: String, spoiler: Boolean): Comment

    suspend fun deleteComment(opinionId: String, commentId: String)

    fun observeFollowing(uid: String): Flow<List<String>>

    suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean)
}
