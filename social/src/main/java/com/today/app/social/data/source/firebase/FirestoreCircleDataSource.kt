package com.today.app.social.data.source.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.today.app.social.data.mapper.SocialMapper.toDoc
import com.today.app.social.data.mapper.SocialMapper.toDomain
import com.today.app.social.data.model.CircleDoc
import com.today.app.social.data.model.CircleMemberDoc
import com.today.app.social.data.source.CircleDataSource
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreCircleDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CircleDataSource {

    private val circles = firestore.collection(COLLECTION_CIRCLES)

    override suspend fun createCircle(name: String): Circle {
        val uid = requireUid()
        val ref = circles.document()
        val member = CircleMemberDoc(name = displayName(), joinedAt = now())
        val circle = Circle(
            id = ref.id,
            name = name.trim(),
            ownerUid = uid,
            ownerName = member.name,
            inviteCode = generateInviteCode(),
            memberCount = 1,
            createdAt = now()
        )
        val batch = firestore.batch()
        batch.set(ref, circle.toDoc().copy(memberUids = listOf(uid)))
        batch.set(ref.collection(COLLECTION_MEMBERS).document(uid), member)
        batch.commit().await()
        return circle
    }

    override suspend fun joinByCode(code: String): Circle {
        val uid = requireUid()
        val normalized = code.trim().uppercase()
        val snapshot = circles
            .whereEqualTo(FIELD_INVITE_CODE, normalized)
            .limit(1)
            .get()
            .await()
        val document = snapshot.documents.firstOrNull()
            ?: error("Kode undangan tidak ditemukan")
        val doc = document.toObject(CircleDoc::class.java)
            ?: error("Circle tidak dapat dibaca")
        val circle = doc.toDomain(document.id)
        if (doc.memberUids.contains(uid)) return circle

        val member = CircleMemberDoc(name = displayName(), joinedAt = now())
        val batch = firestore.batch()
        batch.update(
            document.reference,
            mapOf(
                FIELD_MEMBER_UIDS to FieldValue.arrayUnion(uid),
                FIELD_MEMBER_COUNT to FieldValue.increment(1)
            )
        )
        batch.set(document.reference.collection(COLLECTION_MEMBERS).document(uid), member)
        batch.commit().await()
        return circle.copy(memberCount = circle.memberCount + 1)
    }

    override suspend fun leaveCircle(circleId: String) {
        val uid = requireUid()
        val ref = circles.document(circleId)
        val batch = firestore.batch()
        batch.delete(ref.collection(COLLECTION_MEMBERS).document(uid))
        batch.update(
            ref,
            mapOf(
                FIELD_MEMBER_UIDS to FieldValue.arrayRemove(uid),
                FIELD_MEMBER_COUNT to FieldValue.increment(-1)
            )
        )
        batch.commit().await()
    }

    override fun observeMyCircles(): Flow<List<Circle>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return circles
            .whereArrayContains(FIELD_MEMBER_UIDS, uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(CircleDoc::class.java)?.toDomain(it.id) }
                    .sortedByDescending { it.createdAt }
            }
    }

    override fun observeCircle(circleId: String): Flow<Circle?> =
        circles
            .document(circleId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) return@map null
                snapshot.toObject(CircleDoc::class.java)?.toDomain(snapshot.id)
            }

    override fun observeMembers(circleId: String): Flow<List<CircleMember>> =
        circles
            .document(circleId)
            .collection(COLLECTION_MEMBERS)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(CircleMemberDoc::class.java)?.toDomain(it.id) }
                    .sortedBy { it.joinedAt }
            }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("Belum masuk")

    private fun displayName(): String {
        val user = auth.currentUser
        return user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.takeIf { it.isNotBlank() }
            ?: "Pengguna"
    }

    private fun generateInviteCode(): String =
        (1..INVITE_CODE_LENGTH)
            .map { INVITE_ALPHABET.random() }
            .joinToString("")

    private fun now(): Long = System.currentTimeMillis()

    private companion object {
        const val COLLECTION_CIRCLES = "circles"
        const val COLLECTION_MEMBERS = "members"
        const val FIELD_INVITE_CODE = "inviteCode"
        const val FIELD_MEMBER_UIDS = "memberUids"
        const val FIELD_MEMBER_COUNT = "memberCount"
        const val INVITE_CODE_LENGTH = 6
        const val INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
