package com.today.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.UserProfile
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ProfileUiState(
    val session: UserSession? = null,
    val profile: UserProfile? = null,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _state.update { it.copy(session = session) }
                if (session != null) {
                    ensureProfile(session)
                } else {
                    _state.update { it.copy(profile = null) }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            authRepository.signOut().fold(
                onSuccess = { _state.update { it.copy(busy = false) } },
                onFailure = { e -> _state.update { it.copy(busy = false, error = friendly(e)) } }
            )
        }
    }

    fun saveProfile(displayName: String, bio: String) {
        val current = _state.value.profile ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            val updated = current.copy(
                displayName = displayName.trim().ifBlank { current.displayName },
                bio = bio.trim(),
                updatedAt = System.currentTimeMillis()
            )
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.upsertProfile(updated)
            }
            if (result == null) {
                _state.update {
                    it.copy(busy = false, error = "Tidak bisa menyimpan ke server. Profil ditampilkan lokal.")
                }
                return@launch
            }
            result.fold(
                onSuccess = {
                    _state.update { it.copy(busy = false, profile = updated, message = "Profil disimpan") }
                },
                onFailure = { e ->
                    _state.update { it.copy(busy = false, error = friendly(e)) }
                }
            )
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, error = null) }

    private suspend fun ensureProfile(session: UserSession) {
        val now = System.currentTimeMillis()
        val fallback = UserProfile(
            uid = session.uid,
            displayName = session.displayName
                ?: session.email?.substringBefore('@')
                ?: "Pengguna",
            photoUrl = session.photoUrl,
            bio = "",
            createdAt = now,
            updatedAt = now
        )

        if (_state.value.profile == null) {
            _state.update { it.copy(profile = fallback) }
        }

        val remote = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
            socialRepository.getProfile(session.uid)
        } ?: return

        val existing = remote.getOrNull()
        if (remote.isSuccess && existing != null) {
            _state.update { it.copy(profile = existing) }
            return
        }
        if (remote.isSuccess) {
            withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.upsertProfile(fallback)
            }
        }
    }

    private fun friendly(e: Throwable): String {
        val raw = e.localizedMessage ?: e.message ?: ""
        return when {
            raw.contains("PERMISSION_DENIED", ignoreCase = true) ||
                raw.contains("insufficient permissions", ignoreCase = true) ->
                "Akses ditolak server. Periksa Firestore Rules di Firebase Console."

            raw.contains("not been used", ignoreCase = true) ||
                raw.contains("disabled", ignoreCase = true) ->
                "Firestore belum aktif di Firebase Console."

            raw.isNotBlank() -> raw
            else -> "Terjadi kesalahan. Coba lagi."
        }
    }

    private companion object {
        const val REMOTE_TIMEOUT_MS = 15_000L
    }
}
