package com.today.app.social.data.repository

import com.today.app.social.data.mapper.toFriendlyAuthMessage
import com.today.app.social.data.source.AuthDataSource
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val dataSource: AuthDataSource
) : AuthRepository {

    override fun observeSession(): Flow<UserSession?> = dataSource.observeSession()

    override fun currentUser(): UserSession? = dataSource.currentSession()

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserSession> =
        authCall { dataSource.signUpWithEmail(email, password, displayName) }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserSession> =
        authCall { dataSource.signInWithEmail(email, password) }

    override suspend fun signInWithGoogle(idToken: String): Result<UserSession> =
        authCall { dataSource.signInWithGoogle(idToken) }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        authCall { dataSource.sendPasswordReset(email) }

    override suspend fun signOut(): Result<Unit> = authCall { dataSource.signOut() }

    override suspend fun deleteAccount(): Result<Unit> = authCall { dataSource.deleteAccount() }

    private suspend fun <T> authCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.toFriendlyAuthMessage(), e))
    }
}
