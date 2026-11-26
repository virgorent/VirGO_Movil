package mx.virgo.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
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
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(LoginUiState())
        val state: StateFlow<LoginUiState> = _state.asStateFlow()

        fun onEmailChange(email: String) = update { it.copy(email = email, error = null, loggedIn = false) }

        fun onPasswordChange(password: String) = update { it.copy(password = password, error = null, loggedIn = false) }

        fun submit() {
            update { it.copy(submitted = true, error = null, loggedIn = false) }
            val current = _state.value
            if (!current.emailValid || !current.passwordValid || current.isLoading) return
            viewModelScope.launch {
                update { it.copy(isLoading = true) }
                when (val result = login(current.email, current.password)) {
                    is AppResult.Success -> {
                        val persisted = sessionRepository.establish(result.value)
                        // Se borra la contraseña y se reinicia `submitted`: si no, el campo vacío
                        // se marca «Mínimo 8 caracteres» el instante antes de cambiar de pantalla.
                        update {
                            it.copy(
                                isLoading = false,
                                loggedIn = persisted,
                                password = "",
                                submitted = false,
                                error = if (persisted) null else AppError.Unauthorized,
                            )
                        }
                    }
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
