package com.today.app.social.data.source

import com.today.app.social.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    fun observeSession(): Flow<UserSession?>

    fun currentSession(): UserSession?

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): UserSession

    suspend fun signInWithEmail(email: String, password: String): UserSession

    suspend fun signInWithGoogle(idToken: String): UserSession

    suspend fun sendPasswordReset(email: String)

    suspend fun signOut()

    suspend fun deleteAccount()
}
