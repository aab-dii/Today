package com.today.app.social.domain.model

enum class AuthProvider {
    EMAIL,
    GOOGLE
}

data class UserSession(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false
)
