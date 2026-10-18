package com.today.app.social.data.source.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.snapshots
import com.today.app.social.data.mapper.SocialMapper.toDoc
import com.today.app.social.data.mapper.SocialMapper.toDomain
import com.today.app.social.data.model.CommentDoc
import com.today.app.social.data.model.FollowDoc
import com.today.app.social.data.model.OpinionDoc
import com.today.app.social.data.model.UserDoc
import com.today.app.social.data.source.SocialDataSource
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreSocialDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SocialDataSource {

    private val users = firestore.collection(COLLECTION_USERS)
    private val opinions = firestore.collection(COLLECTION_OPINIONS)

    override suspend fun getProfile(uid: String): UserProfile? {
        val snapshot = users.document(uid).get(Source.SERVER).await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(UserDoc::class.java)?.toDomain(uid)
    }

    override suspend fun setProfile(profile: UserProfile) {
        users.document(profile.uid).set(profile.toDoc()).await()
    }

    override suspend fun publishOpinion(opinion: Opinion): String {
        val ref = opinions.document()
        ref.set(opinion.toDoc()).await()
        return ref.id
    }

    override suspend fun deleteOpinion(opinionId: String) {
        opinions.document(opinionId).delete().await()
    }

    override fun observeOpinionsByAuthors(authorUids: List<String>, limit: Int): Flow<List<Opinion>> {
        if (authorUids.isEmpty()) return flowOf(emptyList())
        return opinions
            .whereIn(FIELD_AUTHOR_UID, authorUids.take(MAX_IN_VALUES))
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(OpinionDoc::class.java)?.toDomain(it.id) }
                    .sortedByDescending { it.createdAt }
            }
    }

    override fun observeUserOpinions(uid: String, limit: Int): Flow<List<Opinion>> =
        opinions
            .whereEqualTo(FIELD_AUTHOR_UID, uid)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(OpinionDoc::class.java)?.toDomain(it.id) }
                    .sortedByDescending { it.createdAt }
            }

    override fun observeAllOpinions(limit: Int): Flow<List<Opinion>> =
        opinions
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(OpinionDoc::class.java)?.toDomain(it.id) }
                    .sortedByDescending { it.createdAt }
            }

    override suspend fun setLike(opinionId: String, liked: Boolean) {
        val uid = requireUid()
        val opinionRef = opinions.document(opinionId)
        val likeRef = opinionRef.collection(COLLECTION_LIKES).document(uid)
        firestore.runTransaction<Unit> { txn ->
            if (liked) {
                txn.set(likeRef, FollowDoc(now()))
                txn.update(opinionRef, FIELD_LIKE_COUNT, FieldValue.increment(1))
            } else {
                txn.delete(likeRef)
                txn.update(opinionRef, FIELD_LIKE_COUNT, FieldValue.increment(-1))
            }
            Unit
        }.await()
    }

    override fun observeIsLiked(opinionId: String, uid: String): Flow<Boolean> =
        opinions
            .document(opinionId)
            .collection(COLLECTION_LIKES)
            .document(uid)
            .snapshots()
            .map { it.exists() }

    override suspend fun isLiked(opinionId: String, uid: String): Boolean =
        opinions
            .document(opinionId)
            .collection(COLLECTION_LIKES)
            .document(uid)
            .get()
            .await()
            .exists()

    override fun observeComments(opinionId: String): Flow<List<Comment>> =
        opinions
            .document(opinionId)
            .collection(COLLECTION_COMMENTS)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(CommentDoc::class.java)?.toDomain(it.id, opinionId) }
                    .sortedBy { it.createdAt }
            }

    override suspend fun addComment(opinionId: String, text: String, spoiler: Boolean): Comment {
        val uid = requireUid()
        val user = auth.currentUser
        val authorName = user?.displayName ?: user?.email ?: uid
        val commentRef = opinions.document(opinionId).collection(COLLECTION_COMMENTS).document()
        val doc = CommentDoc(
            authorUid = uid,
            authorName = authorName,
            text = text,
            spoiler = spoiler,
            createdAt = now()
        )
        firestore.runTransaction<Unit> { txn ->
            txn.set(commentRef, doc)
            txn.update(opinions.document(opinionId), FIELD_COMMENT_COUNT, FieldValue.increment(1))
            Unit
        }.await()
        return doc.toDomain(commentRef.id, opinionId)
    }

    override suspend fun deleteComment(opinionId: String, commentId: String) {
        val opinionRef = opinions.document(opinionId)
        val commentRef = opinionRef.collection(COLLECTION_COMMENTS).document(commentId)
        firestore.runTransaction<Unit> { txn ->
            txn.delete(commentRef)
            txn.update(opinionRef, FIELD_COMMENT_COUNT, FieldValue.increment(-1))
            Unit
        }.await()
    }

    override fun observeFollowing(uid: String): Flow<List<String>> =
        users
            .document(uid)
            .collection(COLLECTION_FOLLOWING)
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.id } }

    override suspend fun setFollowing(followerUid: String, targetUid: String, following: Boolean) {
        val batch = firestore.batch()
        val followingRef = users.document(followerUid).collection(COLLECTION_FOLLOWING).document(targetUid)
        val followerRef = users.document(targetUid).collection(COLLECTION_FOLLOWERS).document(followerUid)
        if (following) {
            val doc = FollowDoc(now())
            batch.set(followingRef, doc)
            batch.set(followerRef, doc)
        } else {
            batch.delete(followingRef)
            batch.delete(followerRef)
        }
        batch.commit().await()
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Belum masuk")

    private fun now(): Long = System.currentTimeMillis()

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_OPINIONS = "opinions"
        const val COLLECTION_COMMENTS = "comments"
        const val COLLECTION_LIKES = "likes"
        const val COLLECTION_FOLLOWING = "following"
        const val COLLECTION_FOLLOWERS = "followers"
        const val FIELD_AUTHOR_UID = "authorUid"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_LIKE_COUNT = "likeCount"
        const val FIELD_COMMENT_COUNT = "commentCount"
        const val MAX_IN_VALUES = 10
    }
}
