package com.today.app.social.domain.model

data class Comment(
    val id: String,
    val opinionId: String,
    val authorUid: String,
    val authorName: String,
    val text: String,
    val spoiler: Boolean,
    val createdAt: Long
)
