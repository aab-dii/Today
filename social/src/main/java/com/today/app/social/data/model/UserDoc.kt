package com.today.app.social.data.model

data class UserDoc(
    var displayName: String = "",
    var photoUrl: String? = null,
    var bio: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
)
