package com.today.app.social.domain.model

import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood

data class Opinion(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val titleId: Int,
    val mediaType: MediaType,
    val titleName: String,
    val posterPath: String?,
    val rating: Double,
    val mood: Mood?,
    val note: String,
    val spoiler: Boolean,
    val createdAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val likedByMe: Boolean = false
)
