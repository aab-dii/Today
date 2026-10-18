package com.today.app.social.data.mapper

import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.social.data.model.CircleDoc
import com.today.app.social.data.model.CircleMemberDoc
import com.today.app.social.data.model.CommentDoc
import com.today.app.social.data.model.OpinionDoc
import com.today.app.social.data.model.UserDoc
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile

object SocialMapper {

    fun UserDoc.toDomain(uid: String): UserProfile = UserProfile(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl,
        bio = bio,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun UserProfile.toDoc(): UserDoc = UserDoc(
        displayName = displayName,
        photoUrl = photoUrl,
        bio = bio,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun OpinionDoc.toDomain(id: String, likedByMe: Boolean = false): Opinion = Opinion(
        id = id,
        authorUid = authorUid,
        authorName = authorName,
        titleId = titleId,
        mediaType = MediaType.from(mediaType) ?: MediaType.MOVIE,
        titleName = titleName,
        posterPath = posterPath,
        rating = rating,
        mood = Mood.from(mood),
        note = note,
        spoiler = spoiler,
        createdAt = createdAt,
        likeCount = likeCount,
        commentCount = commentCount,
        likedByMe = likedByMe
    )

    fun Opinion.toDoc(): OpinionDoc = OpinionDoc(
        authorUid = authorUid,
        authorName = authorName,
        titleId = titleId,
        mediaType = mediaType.value,
        titleName = titleName,
        posterPath = posterPath,
        rating = rating,
        mood = mood?.name,
        note = note,
        spoiler = spoiler,
        createdAt = createdAt,
        likeCount = likeCount,
        commentCount = commentCount
    )

    fun CommentDoc.toDomain(id: String, opinionId: String): Comment = Comment(
        id = id,
        opinionId = opinionId,
        authorUid = authorUid,
        authorName = authorName,
        text = text,
        spoiler = spoiler,
        createdAt = createdAt
    )

    fun Comment.toDoc(): CommentDoc = CommentDoc(
        authorUid = authorUid,
        authorName = authorName,
        text = text,
        spoiler = spoiler,
        createdAt = createdAt
    )

    fun CircleDoc.toDomain(id: String): Circle = Circle(
        id = id,
        name = name,
        ownerUid = ownerUid,
        ownerName = ownerName,
        inviteCode = inviteCode,
        memberCount = memberCount,
        createdAt = createdAt
    )

    fun Circle.toDoc(): CircleDoc = CircleDoc(
        name = name,
        ownerUid = ownerUid,
        ownerName = ownerName,
        inviteCode = inviteCode,
        memberCount = memberCount,
        createdAt = createdAt
    )

    fun CircleMemberDoc.toDomain(uid: String): CircleMember = CircleMember(
        uid = uid,
        name = name,
        joinedAt = joinedAt
    )

    fun CircleMember.toDoc(): CircleMemberDoc = CircleMemberDoc(
        name = name,
        joinedAt = joinedAt
    )
}
