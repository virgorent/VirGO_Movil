package mx.virgo.app.ui.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.virgo.core.domain.ValidateLoginFormUseCase
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val submitted: Boolean = false,
    val emailValid: Boolean = true,
    val passwordValid: Boolean = true,
    val loggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val validateLogin: ValidateLoginFormUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(LoginUiState())
        val state: StateFlow<LoginUiState> = _state.asStateFlow()

        fun onEmailChange(email: String) = update { it.copy(email = email) }

        fun onPasswordChange(password: String) = update { it.copy(password = password) }

        fun submit() {
            update { it.copy(submitted = true) }
            if (validateLogin(_state.value.email, _state.value.password)) {
                update { it.copy(loggedIn = true) }
            }
        }

        private fun update(block: (LoginUiState) -> LoginUiState) {
            val next = block(_state.value)
            _state.value =
                next.copy(
                    emailValid = validateLogin.emailValid(next.email),
                    passwordValid = validateLogin.passwordValid(next.password),
                )
        }
    }
