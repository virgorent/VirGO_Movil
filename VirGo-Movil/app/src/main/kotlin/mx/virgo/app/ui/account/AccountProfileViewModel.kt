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
import mx.virgo.app.session.SessionRole
import mx.virgo.app.session.sessionRole
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.DeleteAccountUseCase
import mx.virgo.core.domain.GetProfileUseCase
import mx.virgo.core.domain.UserProfile
import javax.inject.Inject

/**
 * `profile` solo existe tras validar con el servidor; mientras tanto la pantalla usa la
 * identidad de la sesión. `phone` e `isVerified` nunca se inventan ni se persisten.
 * El borrado de cuenta (`021`) solo se ofrece a clientes (P-01).
 */
data class AccountProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val outdated: Boolean = false,
    val canDeleteAccount: Boolean = false,
    val confirmingDelete: Boolean = false,
    val deleting: Boolean = false,
    val deleteFailed: Boolean = false,
)

@HiltViewModel
class AccountProfileViewModel
    @Inject
    constructor(
        private val getProfile: GetProfileUseCase,
        private val deleteAccount: DeleteAccountUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState =
            MutableStateFlow(
                AccountProfileUiState(
                    canDeleteAccount =
                        session.state.value.session
                            ?.role == SessionRole.CLIENT,
                ),
            )
        val state: StateFlow<AccountProfileUiState> = mutableState.asStateFlow()

        init {
            load()
        }

        fun retry() = load()

        /** `021` RF-02: abre la confirmación; no envía nada todavía. */
        fun requestDelete() {
            val current = mutableState.value
            if (!current.canDeleteAccount || current.deleting) return
            mutableState.value = current.copy(confirmingDelete = true, deleteFailed = false)
        }

        fun cancelDelete() {
            if (mutableState.value.deleting) return
            mutableState.value = mutableState.value.copy(confirmingDelete = false)
        }

        /** `021` RF-03–RF-06: un solo envío; la sesión solo se cierra con 204 o 401. */
        fun confirmDelete() {
            val current = mutableState.value
            if (!current.confirmingDelete || current.deleting) return
            mutableState.value = current.copy(deleting = true)
            viewModelScope.launch {
                when (val result = deleteAccount()) {
                    is AppResult.Success -> session.invalidate(SessionEndReason.ACCOUNT_DELETED)
                    is AppResult.Failure ->
                        if (result.error == AppError.Unauthorized) {
                            session.invalidate(SessionEndReason.DELETE_EXPIRED)
                        } else {
                            mutableState.value =
                                mutableState.value.copy(confirmingDelete = false, deleting = false, deleteFailed = true)
                        }
                }
            }
        }

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
            mutableState.value = mutableState.value.copy(loading = false, profile = profile, outdated = false)
        }

        private suspend fun reject(error: AppError) {
            when (error) {
                AppError.Unauthorized -> session.invalidate(SessionEndReason.REVOKED)
                AppError.NotFound -> session.invalidate(SessionEndReason.ACCOUNT_UNAVAILABLE)
                else -> mutableState.value = mutableState.value.copy(loading = false, profile = null, outdated = true)
            }
        }
    }
