package com.today.app.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.repository.CircleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class CirclesUiState(
    val circles: List<Circle> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val openCircleId: String? = null,
    val message: String? = null,
    val error: String? = null
)

class CirclesViewModel(
    private val circleRepository: CircleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CirclesUiState())
    val state: StateFlow<CirclesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            circleRepository.observeMyCircles()
                .catch { e -> _state.update { it.copy(loading = false, error = friendly(e)) } }
                .collect { circles ->
                    _state.update { it.copy(circles = circles, loading = false) }
                }
        }
    }

    fun createCircle(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(error = "Nama circle tidak boleh kosong") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                circleRepository.createCircle(trimmed)
            }
            if (result == null) {
                _state.update { it.copy(busy = false, error = CONNECT_ERROR) }
                return@launch
            }
            result.fold(
                onSuccess = { circle ->
                    _state.update {
                        it.copy(
                            busy = false,
                            openCircleId = circle.id,
                            message = "Circle dibuat. Bagikan kodenya!"
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(busy = false, error = friendly(e)) } }
            )
        }
    }

    fun joinByCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(error = "Masukkan kode undangan") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            val result = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                circleRepository.joinByCode(trimmed)
            }
            if (result == null) {
                _state.update { it.copy(busy = false, error = CONNECT_ERROR) }
                return@launch
            }
            result.fold(
                onSuccess = { circle ->
                    _state.update {
                        it.copy(
                            busy = false,
                            openCircleId = circle.id,
                            message = "Bergabung ke ${circle.name}"
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(busy = false, error = friendly(e)) } }
            )
        }
    }

    fun consumeOpenCircle() = _state.update { it.copy(openCircleId = null) }

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
