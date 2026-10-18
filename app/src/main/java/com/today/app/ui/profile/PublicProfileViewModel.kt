package com.today.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserProfile
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class PublicProfileUiState(
    val targetUid: String = "",
    val fallbackName: String = "",
    val profile: UserProfile? = null,
    val session: UserSession? = null,
    val isFollowing: Boolean = false,
    val likedOpinionIds: Set<String> = emptySet(),
    val loading: Boolean = false,
    val busyFollow: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    val isSelf: Boolean get() = session?.uid != null && session.uid == targetUid
    val displayName: String get() = profile?.displayName?.ifBlank { null } ?: fallbackName.ifBlank { "Pengguna" }
    fun isLiked(opinionId: String): Boolean = opinionId in likedOpinionIds
}

@OptIn(ExperimentalCoroutinesApi::class)
class PublicProfileViewModel(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PublicProfileUiState())
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    private val targetUidFlow = _state.map { it.targetUid }.distinctUntilChanged()
    private val currentUidFlow = authRepository.observeSession().map { it?.uid }.distinctUntilChanged()

    val opinions: StateFlow<List<Opinion>> = targetUidFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList())
            else socialRepository.observeUserOpinions(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _state.update { it.copy(session = session) }
            }
        }

        viewModelScope.launch {
            combine(currentUidFlow, targetUidFlow) { myUid, targetUid ->
                if (myUid == null || targetUid.isBlank() || myUid == targetUid) {
                    flowOf(false)
                } else {
                    socialRepository.observeFollowing(myUid).map { targetUid in it }
                }
            }.flatMapLatest { it }.collect { following ->
                _state.update { it.copy(isFollowing = following) }
            }
        }

        viewModelScope.launch {
            combine(currentUidFlow, opinions) { myUid, ops ->
                if (myUid == null) return@combine
                val liked = ops.mapNotNull { op ->
                    if (op.id.isNotBlank() && socialRepository.isLiked(op.id, myUid).getOrDefault(false)) {
                        op.id
                    } else null
                }.toSet()
                _state.update { it.copy(likedOpinionIds = it.likedOpinionIds + liked) }
            }.collect()
        }
    }

    fun initProfile(targetUid: String, fallbackName: String = "") {
        if (_state.value.targetUid == targetUid && _state.value.profile != null) return

        _state.update {
            it.copy(
                targetUid = targetUid,
                fallbackName = fallbackName,
                loading = true,
                error = null,
                message = null
            )
        }

        viewModelScope.launch {
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.getProfile(targetUid)
            }
            if (result == null) {
                _state.update { it.copy(loading = false) }
                return@launch
            }
            result.fold(
                onSuccess = { prof ->
                    _state.update { it.copy(loading = false, profile = prof) }
                },
                onFailure = {
                    _state.update { it.copy(loading = false) }
                }
            )
        }
    }

    fun toggleFollow() {
        val myUid = _state.value.session?.uid
        val targetUid = _state.value.targetUid
        if (myUid == null) {
            _state.update { it.copy(error = "Masuk dulu untuk mengikuti akun.") }
            return
        }
        if (myUid == targetUid) {
            _state.update { it.copy(error = "Tidak bisa mengikuti akun sendiri.") }
            return
        }
        val willFollow = !_state.value.isFollowing
        viewModelScope.launch {
            _state.update { it.copy(busyFollow = true, error = null, message = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.setFollowing(myUid, targetUid, willFollow)
            }
            if (result == null) {
                _state.update {
                    it.copy(busyFollow = false, error = "Tidak bisa terhubung ke server. Coba lagi.")
                }
                return@launch
            }
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            busyFollow = false,
                            isFollowing = willFollow,
                            message = if (willFollow) "Mengikuti" else "Berhenti mengikuti"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(busyFollow = false, error = friendly(e)) }
                }
            )
        }
    }

    fun toggleLike(opinion: Opinion) {
        val myUid = _state.value.session?.uid
        if (myUid == null) {
            _state.update { it.copy(error = "Masuk dulu untuk menyukai opini.") }
            return
        }
        val willLike = opinion.id !in _state.value.likedOpinionIds
        _state.update {
            val updated = if (willLike) it.likedOpinionIds + opinion.id else it.likedOpinionIds - opinion.id
            it.copy(likedOpinionIds = updated)
        }
        viewModelScope.launch {
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.setLike(opinion.id, willLike)
            }
            if (result == null || result.isFailure) {
                _state.update {
                    val rollback = if (willLike) it.likedOpinionIds - opinion.id else it.likedOpinionIds + opinion.id
                    it.copy(
                        likedOpinionIds = rollback,
                        error = "Gagal memperbarui suka. Coba lagi."
                    )
                }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, error = null) }

    private fun friendly(e: Throwable): String {
        val raw = e.localizedMessage ?: e.message ?: ""
        return when {
            raw.contains("PERMISSION_DENIED", ignoreCase = true) ||
                raw.contains("insufficient permissions", ignoreCase = true) ->
                "Akses ditolak server. Periksa Firestore Rules."

            raw.isNotBlank() -> raw
            else -> "Terjadi kesalahan. Coba lagi."
        }
    }

    private companion object {
        const val REMOTE_TIMEOUT_MS = 15_000L
    }
}
