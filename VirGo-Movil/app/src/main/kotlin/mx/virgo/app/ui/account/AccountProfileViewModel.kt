package mx.virgo.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.sessionRole
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetProfileUseCase
import mx.virgo.core.domain.UserProfile
import javax.inject.Inject

/**
 * `profile` solo existe tras validar con el servidor; mientras tanto la pantalla usa la
 * identidad de la sesión. `phone` e `isVerified` nunca se inventan ni se persisten.
 */
data class AccountProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val outdated: Boolean = false,
)

@HiltViewModel
class AccountProfileViewModel
    @Inject
    constructor(
        private val getProfile: GetProfileUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(AccountProfileUiState())
        val state: StateFlow<AccountProfileUiState> = mutableState.asStateFlow()

        init {
            load()
        }

        fun retry() = load()

        private fun load() {
            mutableState.value = mutableState.value.copy(loading = true, outdated = false)
            viewModelScope.launch {
                when (val result = getProfile()) {
                    is AppResult.Success -> accept(result.value)
                    is AppResult.Failure -> reject(result.error)
                }
            }
        }

        private suspend fun accept(profile: UserProfile) {
            val active = session.state.value.session
            val expected = active?.role
            if (expected == null || sessionRole(profile.role) != expected) {
                session.invalidate(SessionEndReason.ROLE_MISMATCH)
                return
            }
            mutableState.value = AccountProfileUiState(loading = false, profile = profile)
        }

        private suspend fun reject(error: AppError) {
            when (error) {
                AppError.Unauthorized -> session.invalidate(SessionEndReason.REVOKED)
                AppError.NotFound -> session.invalidate(SessionEndReason.ACCOUNT_UNAVAILABLE)
                else -> mutableState.value = AccountProfileUiState(loading = false, outdated = true)
            }
        }
    }
