package com.today.app.social.data.model

data class CommentDoc(
    var authorUid: String = "",
    var authorName: String = "",
    var text: String = "",
    var spoiler: Boolean = false,
    var createdAt: Long = 0L
)
