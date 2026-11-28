package mx.virgo.app.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.LogoutUseCase
import javax.inject.Inject

/** Límite del logout remoto antes de cerrar localmente (030 §6.3). */
internal const val LOGOUT_TIMEOUT_MILLIS = 10_000L

@HiltViewModel
class SessionViewModel
    @Inject
    constructor(
        private val repository: SessionRepository,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        val state: StateFlow<SessionUiState> = repository.state

        init {
            refresh()
            viewModelScope.launch {
                repository.expiration.collectLatest { expiresAt ->
                    if (expiresAt > 0) {
                        delay(repository.remainingMillis)
                        viewModelScope.launch { repository.expire() }
                    }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch { repository.refresh() }
        }

        /**
         * Revoca en el servidor y luego borra el token local, aunque la revocación falle.
         * Un segundo toque mientras está en curso se ignora.
         */
        fun logout() {
            if (!repository.markLoggingOut()) return
            viewModelScope.launch {
                val result = withTimeoutOrNull(LOGOUT_TIMEOUT_MILLIS) { logoutUseCase() }
                val confirmed =
                    result is AppResult.Success ||
                        (result is AppResult.Failure && result.error == AppError.Unauthorized)
                if (confirmed) {
                    repository.logout()
                } else {
                    repository.invalidate(SessionEndReason.LOGOUT_UNCONFIRMED)
                }
            }
        }

        fun invalidate(reason: SessionEndReason) {
            viewModelScope.launch { repository.invalidate(reason) }
        }

        fun consumeEndReason() = repository.consumeEndReason()
    }
