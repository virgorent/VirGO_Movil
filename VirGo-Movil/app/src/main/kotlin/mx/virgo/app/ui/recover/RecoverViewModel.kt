package mx.virgo.app.ui.recover

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.virgo.core.domain.CODE_LENGTH
import mx.virgo.core.domain.GenerateRecoveryCodeUseCase
import mx.virgo.core.domain.RecoveryError
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateRecoveryUseCase
import javax.inject.Inject

sealed interface RecoverEvent {
    data class Sent(
        val code: String,
    ) : RecoverEvent

    data object Done : RecoverEvent
}

data class RecoverUiState(
    val email: String = "",
    val emailValid: Boolean = false,
    val codeSent: Boolean = false,
    val demoCode: String = "",
    val sentAt: Long = 0L,
    val code: String = "",
    val password: String = "",
    val confirm: String = "",
    val stepError: RecoveryError? = null,
)

@HiltViewModel
class RecoverViewModel
    @Inject
    constructor(
        private val validateEmail: ValidateEmailUseCase,
        private val generateCode: GenerateRecoveryCodeUseCase,
        private val validateRecovery: ValidateRecoveryUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RecoverUiState())
        val state: StateFlow<RecoverUiState> = _state.asStateFlow()

        private val _events = MutableSharedFlow<RecoverEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<RecoverEvent> = _events.asSharedFlow()

        fun onEmailChange(email: String) {
            update { it.copy(email = email, emailValid = validateEmail(email)) }
        }

        fun sendCode(nowMillis: Long = System.currentTimeMillis()) {
            val generated = generateCode()
            update {
                it.copy(codeSent = true, demoCode = generated, sentAt = nowMillis, stepError = null)
            }
            _events.tryEmit(RecoverEvent.Sent(generated))
        }

        fun onCodeChange(code: String) {
            update { it.copy(code = code.filter(Char::isDigit).take(CODE_LENGTH)) }
        }

        fun onPasswordChange(password: String) {
            update { it.copy(password = password) }
        }

        fun onConfirmChange(confirm: String) {
            update { it.copy(confirm = confirm) }
        }

        fun save(nowMillis: Long = System.currentTimeMillis()) {
            val current = _state.value
            val error =
                validateRecovery(
                    expectedCode = current.demoCode,
                    enteredCode = current.code,
                    sentAtMillis = current.sentAt,
                    nowMillis = nowMillis,
                    password = current.password,
                    confirm = current.confirm,
                )
            if (error != null) {
                update { it.copy(stepError = error) }
            } else {
                update { it.copy(stepError = null) }
                _events.tryEmit(RecoverEvent.Done)
            }
        }

        private fun update(block: (RecoverUiState) -> RecoverUiState) {
            _state.value = block(_state.value)
        }
    }
