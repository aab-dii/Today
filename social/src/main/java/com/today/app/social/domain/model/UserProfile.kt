package com.today.app.social.domain.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val bio: String,
    val createdAt: Long,
    val updatedAt: Long
)
