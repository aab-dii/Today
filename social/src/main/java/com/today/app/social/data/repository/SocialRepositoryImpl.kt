package com.today.app.social.data.repository

import com.today.app.social.data.source.SocialDataSource
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow

class SocialRepositoryImpl(
    private val dataSource: SocialDataSource
) : SocialRepository {

    override suspend fun getProfile(uid: String): Result<UserProfile?> =
        runCatching { dataSource.getProfile(uid) }

    override suspend fun upsertProfile(profile: UserProfile): Result<Unit> =
        runCatching { dataSource.setProfile(profile) }

    override suspend fun publishOpinion(opinion: Opinion): Result<String> =
        runCatching { dataSource.publishOpinion(opinion) }

    override suspend fun deleteOpinion(opinionId: String): Result<Unit> =
        runCatching { dataSource.deleteOpinion(opinionId) }

    override fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int): Flow<List<Opinion>> =
        dataSource.observeOpinionsByAuthors(authorUids, limit)

    override fun observeAllOpinions(limit: Int): Flow<List<Opinion>> =
        dataSource.observeAllOpinions(limit)

    override fun observeUserOpinions(uid: String, limit: Int): Flow<List<Opinion>> =
        dataSource.observeUserOpinions(uid, limit)

    override suspend fun setLike(opinionId: String, liked: Boolean): Result<Unit> =
        runCatching { dataSource.setLike(opinionId, liked) }

    override fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean> =
        dataSource.observeIsLiked(opinionId, uid)

    override suspend fun isLiked(opinionId: String, uid: String): Result<Boolean> =
        runCatching { dataSource.isLiked(opinionId, uid) }

    override fun observeComments(opinionId: String): Flow<List<Comment>> =
        dataSource.observeComments(opinionId)

    override suspend fun addComment(opinionId: String, text: String, spoiler: Boolean): Result<Comment> =
        runCatching { dataSource.addComment(opinionId, text, spoiler) }

    override suspend fun deleteComment(opinionId: String, commentId: String): Result<Unit> =
        runCatching { dataSource.deleteComment(opinionId, commentId) }

    override fun observeFollowing(uid: String): Flow<List<String>> =
        dataSource.observeFollowing(uid)

    override suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean): Result<Unit> =
        runCatching { dataSource.setFollowing(followerUid, targetUid, following) }
}
