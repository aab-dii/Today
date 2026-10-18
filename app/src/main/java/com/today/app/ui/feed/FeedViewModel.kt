package com.today.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.Opinion
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

enum class FeedMode { FOR_YOU, FOLLOWING }

data class FeedUiState(
    val mode: FeedMode = FeedMode.FOR_YOU,
    val session: UserSession? = null,
    val following: Set<String> = emptySet(),
    val likedOpinionIds: Set<String> = emptySet(),
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    fun isFollowing(authorUid: String): Boolean = authorUid in following
    fun isLiked(opinionId: String): Boolean = opinionId in likedOpinionIds
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private val currentUid: StateFlow<String?> = authRepository.observeSession()
        .map { it?.uid }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val forYou: StateFlow<List<Opinion>> = socialRepository.observeAllOpinions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val followingUids: StateFlow<Set<String>> = currentUid
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else socialRepository.observeFollowing(uid)
        }
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val followingFeed: StateFlow<List<Opinion>> = currentUid
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else socialRepository.observeFollowing(uid).flatMapLatest { followed ->
                socialRepository.observeOpinionsByAuthors(buildList {
                    add(uid)
                    addAll(followed)
                }.distinct())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _state.update { it.copy(session = session) }
            }
        }
        viewModelScope.launch {
            followingUids.collect { followed ->
                _state.update { it.copy(following = followed) }
            }
        }
        viewModelScope.launch {
            combine(currentUid, forYou, followingFeed) { uid, fy, ff ->
                if (uid == null) return@combine
                val allOps = (fy + ff).distinctBy { it.id }
                val liked = allOps.mapNotNull { op ->
                    if (op.id.isNotBlank() && socialRepository.isLiked(op.id, uid).getOrDefault(false)) {
                        op.id
                    } else null
                }.toSet()
                _state.update { it.copy(likedOpinionIds = it.likedOpinionIds + liked) }
            }.collect()
        }
    }

    fun setMode(mode: FeedMode) = _state.update { it.copy(mode = mode) }

    fun toggleFollow(authorUid: String) {
        val uid = _state.value.session?.uid
        if (uid == null) {
            _state.update { it.copy(error = "Masuk dulu untuk mengikuti akun.") }
            return
        }
        if (authorUid == uid) {
            _state.update { it.copy(error = "Tidak bisa mengikuti akun sendiri.") }
            return
        }
        val willFollow = authorUid !in _state.value.following
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.setFollowing(uid, authorUid, willFollow)
            }
            if (result == null) {
                _state.update {
                    it.copy(busy = false, error = "Tidak bisa terhubung ke server. Coba lagi.")
                }
                return@launch
            }
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            busy = false,
                            message = if (willFollow) "Mengikuti" else "Berhenti mengikuti"
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(busy = false, error = friendly(e)) } }
            )
        }
    }

    fun toggleLike(opinion: Opinion) {
        val uid = _state.value.session?.uid
        if (uid == null) {
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
