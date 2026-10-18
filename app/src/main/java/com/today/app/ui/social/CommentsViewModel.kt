package com.today.app.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class CommentsUiState(
    val opinion: Opinion? = null,
    val session: UserSession? = null,
    val isLiked: Boolean = false,
    val revealedCommentIds: Set<String> = emptySet(),
    val submitting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsViewModel(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CommentsUiState())
    val state: StateFlow<CommentsUiState> = _state.asStateFlow()

    private val currentOpinion = _state.map { it.opinion }.distinctUntilChanged()
    private val currentUid = authRepository.observeSession().map { it?.uid }.distinctUntilChanged()

    val comments: StateFlow<List<Comment>> = currentOpinion
        .flatMapLatest { op ->
            if (op == null || op.id.isBlank()) flowOf(emptyList())
            else socialRepository.observeComments(op.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _state.update { it.copy(session = session) }
            }
        }
        viewModelScope.launch {
            currentOpinion.flatMapLatest { op ->
                currentUid.flatMapLatest { uid ->
                    if (op == null || uid == null || op.id.isBlank()) flowOf(false)
                    else socialRepository.observeIsLiked(op.id, uid)
                }
            }.collect { liked ->
                _state.update { it.copy(isLiked = liked) }
            }
        }
    }

    fun selectOpinion(opinion: Opinion) {
        _state.update {
            it.copy(
                opinion = opinion,
                revealedCommentIds = emptySet(),
                error = null,
                message = null
            )
        }
    }

    fun dismiss() {
        _state.update { it.copy(opinion = null, error = null, message = null) }
    }

    fun toggleSpoiler(commentId: String) {
        _state.update { current ->
            val updated = if (commentId in current.revealedCommentIds) {
                current.revealedCommentIds - commentId
            } else {
                current.revealedCommentIds + commentId
            }
            current.copy(revealedCommentIds = updated)
        }
    }

    fun toggleLike() {
        val op = _state.value.opinion ?: return
        val uid = _state.value.session?.uid
        if (uid == null) {
            _state.update { it.copy(error = "Masuk dulu untuk menyukai opini.") }
            return
        }
        val willLike = !_state.value.isLiked
        viewModelScope.launch {
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.setLike(op.id, willLike)
            }
            if (result == null || result.isFailure) {
                _state.update { it.copy(error = "Gagal memperbarui suka.") }
            }
        }
    }

    fun addComment(text: String, spoiler: Boolean) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val op = _state.value.opinion ?: return
        val uid = _state.value.session?.uid
        if (uid == null) {
            _state.update { it.copy(error = "Masuk dulu untuk berkomentar.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.addComment(op.id, trimmed, spoiler)
            }
            if (result == null) {
                _state.update { it.copy(submitting = false, error = "Koneksi batas waktu. Coba lagi.") }
                return@launch
            }
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            submitting = false,
                            message = "Komentar terkirim"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(submitting = false, error = e.localizedMessage ?: "Gagal mengirim komentar.") }
                }
            )
        }
    }

    fun deleteComment(commentId: String) {
        val op = _state.value.opinion ?: return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.deleteComment(op.id, commentId)
            }
            result?.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            submitting = false,
                            message = "Komentar dihapus"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(submitting = false, error = e.localizedMessage ?: "Gagal menghapus komentar.") }
                }
            ) ?: _state.update { it.copy(submitting = false, error = "Batas waktu koneksi.") }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, error = null) }

    private companion object {
        const val REMOTE_TIMEOUT_MS = 15_000L
    }
}
