package com.today.app.social.domain.repository

import com.today.app.social.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<UserSession?>

    fun currentUser(): UserSession?

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserSession>

    suspend fun signInWithEmail(email: String, password: String): Result<UserSession>

    suspend fun signInWithGoogle(idToken: String): Result<UserSession>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun signOut(): Result<Unit>

    suspend fun deleteAccount(): Result<Unit>
}
