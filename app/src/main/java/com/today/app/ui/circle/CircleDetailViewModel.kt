package com.today.app.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.CircleRepository
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class CircleDetailUiState(
    val circle: Circle? = null,
    val members: List<CircleMember> = emptyList(),
    val opinions: List<Opinion> = emptyList(),
    val currentUid: String? = null,
    val likedOpinionIds: Set<String> = emptySet(),
    val loaded: Boolean = false,
    val busy: Boolean = false,
    val left: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    val isOwner: Boolean
        get() = circle != null && circle.ownerUid == currentUid

    fun isLiked(opinionId: String): Boolean = opinionId in likedOpinionIds
}

@OptIn(ExperimentalCoroutinesApi::class)
class CircleDetailViewModel(
    private val circleRepository: CircleRepository,
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CircleDetailUiState())
    val state: StateFlow<CircleDetailUiState> = _state.asStateFlow()

    private val circleId = MutableStateFlow<String?>(null)

    private val currentUid: StateFlow<String?> = authRepository.observeSession()
        .map { it?.uid }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val circle: StateFlow<Circle?> = circleId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else circleRepository.observeCircle(id)
        }
        .catch { e ->
            _state.update { it.copy(error = friendly(e), loaded = true) }
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val members: StateFlow<List<CircleMember>> = circleId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else circleRepository.observeMembers(id)
        }
        .catch { e ->
            _state.update { it.copy(error = friendly(e)) }
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val opinions: StateFlow<List<Opinion>> = members
        .flatMapLatest { list ->
            val uids = list.map { it.uid }.distinct()
            if (uids.isEmpty()) flowOf(emptyList())
            else socialRepository.observeOpinionsByAuthors(uids)
        }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            currentUid.collect { uid -> _state.update { it.copy(currentUid = uid) } }
        }
        viewModelScope.launch {
            circle.collect { value ->
                _state.update { it.copy(circle = value, loaded = true) }
            }
        }
        viewModelScope.launch {
            members.collect { list -> _state.update { it.copy(members = list) } }
        }
        viewModelScope.launch {
            opinions.collect { list ->
                _state.update { it.copy(opinions = list) }
                val uid = _state.value.currentUid ?: return@collect
                val liked = list.mapNotNull { op ->
                    if (op.id.isNotBlank() && socialRepository.isLiked(op.id, uid).getOrDefault(false)) {
                        op.id
                    } else null
                }.toSet()
                if (liked.isNotEmpty()) {
                    _state.update { it.copy(likedOpinionIds = it.likedOpinionIds + liked) }
                }
            }
        }
    }

    fun load(id: String) {
        if (circleId.value != id) {
            circleId.value = id
        }
    }

    fun toggleLike(opinion: Opinion) {
        val uid = _state.value.currentUid
        if (uid == null) {
            _state.update { it.copy(error = "Masuk dulu untuk menyukai opini.") }
            return
        }
        val willLike = opinion.id !in _state.value.likedOpinionIds
        _state.update {
            val updated = if (willLike) {
                it.likedOpinionIds + opinion.id
            } else {
                it.likedOpinionIds - opinion.id
            }
            it.copy(likedOpinionIds = updated)
        }
        viewModelScope.launch {
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.setLike(opinion.id, willLike)
            }
            if (result == null || result.isFailure) {
                _state.update {
                    val rollback = if (willLike) {
                        it.likedOpinionIds - opinion.id
                    } else {
                        it.likedOpinionIds + opinion.id
                    }
                    it.copy(likedOpinionIds = rollback, error = "Gagal memperbarui suka. Coba lagi.")
                }
            }
        }
    }

    fun leave() {
        val id = circleId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                circleRepository.leaveCircle(id)
            }
            if (result == null) {
                _state.update { it.copy(busy = false, error = CONNECT_ERROR) }
                return@launch
            }
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(busy = false, left = true, message = "Kamu keluar dari circle")
                    }
                },
                onFailure = { e -> _state.update { it.copy(busy = false, error = friendly(e)) } }
            )
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
        const val CONNECT_ERROR = "Tidak bisa terhubung ke server. Coba lagi."
    }
}
