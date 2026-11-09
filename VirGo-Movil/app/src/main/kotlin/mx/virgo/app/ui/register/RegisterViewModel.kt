package mx.virgo.app.ui.register

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.virgo.core.domain.PHONE_LENGTH
import mx.virgo.core.domain.RegisteredEmailStore
import mx.virgo.core.domain.ValidateRegisterFormUseCase
import javax.inject.Inject

enum class RegisterFormError {
    DUPLICATE,
}

sealed interface RegisterEvent {
    data object Registered : RegisterEvent
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val role: RegisterRole = RegisterRole.CLIENT,
    val submitted: Boolean = false,
    val nameValid: Boolean = true,
    val emailValid: Boolean = true,
    val phoneValid: Boolean = true,
    val passwordValid: Boolean = true,
    val formError: RegisterFormError? = null,
    val registered: Boolean = false,
)

@HiltViewModel
class RegisterViewModel
    @Inject
    constructor(
        private val validateRegisterForm: ValidateRegisterFormUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RegisterUiState())
        val state: StateFlow<RegisterUiState> = _state.asStateFlow()

        private val _events = MutableSharedFlow<RegisterEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()

        fun onNameChange(name: String) = update { it.copy(name = name) }

        fun onEmailChange(email: String) = update { it.copy(email = email) }

        fun onPhoneChange(phone: String) {
            update { it.copy(phone = phone.filter(Char::isDigit).take(PHONE_LENGTH)) }
        }

        fun onPasswordChange(password: String) = update { it.copy(password = password) }

        fun onRoleChange(role: RegisterRole) = update { it.copy(role = role) }

        fun submit() {
            val current = _state.value
            val valid =
                validateRegisterForm(current.name, current.email, current.phone, current.password)
            update { it.copy(submitted = true, formError = null) }
            when {
                !valid -> Unit
                !RegisteredEmailStore.register(current.email.trim()) ->
                    update { it.copy(formError = RegisterFormError.DUPLICATE) }
                else -> {
                    update { it.copy(registered = true) }
                    _events.tryEmit(RegisterEvent.Registered)
                }
            }
        }

        private fun update(block: (RegisterUiState) -> RegisterUiState) {
            val blocked = block(_state.value)
            _state.value =
                blocked.copy(
                    nameValid = validateRegisterForm.nameValid(blocked.name),
                    emailValid = validateRegisterForm.emailValid(blocked.email),
                    phoneValid = validateRegisterForm.phoneValid(blocked.phone),
                    passwordValid = validateRegisterForm.passwordValid(blocked.password),
                )
        }
    }
