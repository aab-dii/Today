package com.today.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.today.app.social.domain.model.UserSession
import com.today.app.social.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val ready: Boolean = false,
    val session: UserSession? = null,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _state.update { it.copy(ready = true, session = session) }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) =
        runAuth { authRepository.signInWithEmail(email.trim(), password) }

    fun signUpWithEmail(email: String, password: String, displayName: String) =
        runAuth { authRepository.signUpWithEmail(email.trim(), password, displayName.trim()) }

    fun signInWithGoogle(idToken: String) =
        runAuth { authRepository.signInWithGoogle(idToken) }

    fun sendPasswordReset(email: String) =
        runAuth(successMessage = "Email reset terkirim. Cek kotak masuk.") {
            authRepository.sendPasswordReset(email.trim())
        }

    fun reportError(message: String) = _state.update { it.copy(error = message) }

    fun clearMessage() = _state.update { it.copy(message = null, error = null) }

    private fun runAuth(successMessage: String? = null, block: suspend () -> Result<*>) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            block().fold(
                onSuccess = { _state.update { it.copy(busy = false, message = successMessage) } },
                onFailure = { e ->
                    _state.update { it.copy(busy = false, error = friendly(e)) }
                }
            )
        }
    }

    private fun friendly(e: Throwable): String =
        e.localizedMessage ?: e.message ?: "Terjadi kesalahan. Coba lagi."
}
