package mx.virgo.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.LoginUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val submitted: Boolean = false,
    val emailValid: Boolean = true,
    val passwordValid: Boolean = true,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val loggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val validateLogin: ValidateLoginFormUseCase,
        private val login: LoginUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(LoginUiState())
        val state: StateFlow<LoginUiState> = _state.asStateFlow()

        fun onEmailChange(email: String) = update { it.copy(email = email, error = null) }

        fun onPasswordChange(password: String) = update { it.copy(password = password, error = null) }

        fun submit() {
            update { it.copy(submitted = true, error = null) }
            val current = _state.value
            if (!current.emailValid || !current.passwordValid || current.isLoading) return
            viewModelScope.launch {
                update { it.copy(isLoading = true) }
                when (val result = login(current.email, current.password)) {
                    is AppResult.Success -> update { it.copy(isLoading = false, loggedIn = true) }
                    is AppResult.Failure -> update { it.copy(isLoading = false, error = result.error) }
                }
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
