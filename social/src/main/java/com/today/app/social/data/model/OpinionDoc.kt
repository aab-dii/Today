package com.today.app.social.data.model

data class OpinionDoc(
    var authorUid: String = "",
    var authorName: String = "",
    var titleId: Int = 0,
    var mediaType: String = "",
    var titleName: String = "",
    var posterPath: String? = null,
    var rating: Double = 0.0,
    var mood: String? = null,
    var note: String = "",
    var spoiler: Boolean = false,
    var createdAt: Long = 0L,
    var likeCount: Int = 0,
    var commentCount: Int = 0
)
