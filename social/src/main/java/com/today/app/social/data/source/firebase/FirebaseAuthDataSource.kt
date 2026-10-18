package com.today.app.social.data.source.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.today.app.social.data.source.AuthDataSource
import com.today.app.social.domain.model.UserSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth
) : AuthDataSource {

    override fun observeSession(): Flow<UserSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toSession()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun currentSession(): UserSession? = auth.currentUser?.toSession()

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String): UserSession {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Pendaftaran gagal: pengguna tidak terbentuk")
        if (displayName.isNotBlank()) {
            val request = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
            user.updateProfile(request).await()
        }
        return user.toSession(displayName)
    }

    override suspend fun signInWithEmail(email: String, password: String): UserSession {
        val user = auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Masuk gagal: pengguna tidak ditemukan")
        return user.toSession()
    }

    override suspend fun signInWithGoogle(idToken: String): UserSession {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.signInWithCredential(credential).await().user
            ?: error("Masuk dengan Google gagal")
        return user.toSession()
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        val user = auth.currentUser ?: error("Tidak ada pengguna yang masuk")
        user.delete().await()
    }
}

private fun FirebaseUser.toSession(displayNameFallback: String? = null): UserSession = UserSession(
    uid = uid,
    email = email,
    displayName = displayName ?: displayNameFallback,
    photoUrl = photoUrl?.toString(),
    isAnonymous = isAnonymous
)
