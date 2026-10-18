package com.today.app.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.social.domain.mapper.OpinionMapper
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

data class OpinionUiState(
    val session: UserSession? = null,
    val publishing: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class OpinionViewModel(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OpinionUiState())
    val state: StateFlow<OpinionUiState> = _state.asStateFlow()

    val myOpinions: StateFlow<List<Opinion>> = authRepository.observeSession()
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else socialRepository.observeUserOpinions(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _state.update { it.copy(session = session) }
            }
        }
    }

    fun publish(entry: DiaryEntry, note: String, spoiler: Boolean) {
        val session = _state.value.session
        if (session == null) {
            _state.update { it.copy(error = "Masuk dulu untuk membagikan opini.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(publishing = true, error = null, message = null) }
            val opinion = OpinionMapper.fromDiaryEntry(
                entry = entry,
                authorUid = session.uid,
                authorName = session.authorName(),
                note = note,
                spoiler = spoiler
            )
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                socialRepository.publishOpinion(opinion)
            }
            if (result == null) {
                _state.update {
                    it.copy(publishing = false, error = "Tidak bisa terhubung ke server. Coba lagi.")
                }
                return@launch
            }
            result.fold(
                onSuccess = { _state.update { it.copy(publishing = false, message = "Opini dibagikan") } },
                onFailure = { e -> _state.update { it.copy(publishing = false, error = friendly(e)) } }
            )
        }
    }

    fun deleteOpinion(opinionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(error = null, message = null) }
            socialRepository.deleteOpinion(opinionId).fold(
                onSuccess = { _state.update { it.copy(message = "Opini dihapus") } },
                onFailure = { e -> _state.update { it.copy(error = friendly(e)) } }
            )
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, error = null) }

    private fun UserSession.authorName(): String =
        displayName?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore('@')
            ?: "Pengguna"

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
