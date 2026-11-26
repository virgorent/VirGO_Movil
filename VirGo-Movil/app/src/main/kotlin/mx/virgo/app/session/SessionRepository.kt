package mx.virgo.app.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.virgo.core.datastore.StoredSession
import mx.virgo.core.datastore.TokenStore
import mx.virgo.core.domain.AuthSession
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Fuente compartida de sesión. No renueva tokens ni autoriza datos del demo. */
@Singleton
class SessionRepository(
    private val tokenStore: TokenStore,
    private val clock: Clock,
) {
    @Inject
    constructor(tokenStore: TokenStore) : this(tokenStore, Clock.systemUTC())

    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = mutableState.asStateFlow()
    private val mutableExpiration = MutableStateFlow(0L)
    internal val expiration: StateFlow<Long> = mutableExpiration.asStateFlow()

    private var blocked = false

    suspend fun refresh() =
        mutex.withLock {
            if (blocked) return@withLock
            protect {
                val stored = tokenStore.session.first()
                if (stored == null || !stored.isValidAt(clock.millis())) {
                    clearSession(error = false)
                } else {
                    publish(stored)
                }
            }
        }

    suspend fun establish(auth: AuthSession): Boolean =
        mutex.withLock {
            var established = false
            protect {
                val stored = auth.toStoredSession(clock.millis())
                // Una sola escritura cifrada: nunca hay token nuevo con identidad anterior.
                tokenStore.saveSession(stored)
                blocked = false
                publish(stored)
                established = true
            }
            established
        }

    suspend fun logout() =
        mutex.withLock {
            protect { clearSession(error = false) }
        }

    /** Cierra la sesión local y publica el motivo. Idempotente: un segundo motivo no reemplaza al primero. */
    suspend fun invalidate(reason: SessionEndReason) =
        mutex.withLock {
            if (blocked && mutableState.value.session == null) return@withLock
            protect { clearSession(error = false, reason = reason) }
        }

    /** Llamado por el temporizador: solo cierra si la vigencia realmente terminó. */
    suspend fun expire() =
        mutex.withLock {
            if (mutableState.value.session == null || remainingMillis > 0) return@withLock
            protect { clearSession(error = false, reason = SessionEndReason.EXPIRED) }
        }

    internal fun markLoggingOut(): Boolean {
        val current = mutableState.value
        if (current.loggingOut || current.session == null) return false
        mutableState.update { it.copy(loggingOut = true) }
        return true
    }

    fun consumeEndReason() {
        mutableState.update { it.copy(endReason = null) }
    }

    internal val remainingMillis: Long
        get() = (mutableExpiration.value - clock.millis()).coerceAtLeast(0L)

    private fun publish(stored: StoredSession) {
        require(stored.isValidAt(clock.millis())) { "Expired session" }
        val account = stored.account
        val role = requireNotNull(sessionRole(account.role))
        mutableExpiration.value = stored.expiresAtMillis
        mutableState.value =
            SessionUiState(
                loading = false,
                session = ActiveSession(account.accountId, account.name, account.email, role),
            )
    }

    private suspend fun clearSession(
        error: Boolean,
        reason: SessionEndReason? = null,
    ) {
        blocked = true
        mutableExpiration.value = 0
        mutableState.value = SessionUiState(loading = false, error = error, endReason = reason)
        tokenStore.clear()
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun protect(action: suspend () -> Unit) {
        try {
            action()
        } catch (cancellation: CancellationException) {
            // No publicar autorización parcial si se cancela la persistencia.
            blocked = true
            mutableExpiration.value = 0
            mutableState.value = SessionUiState(loading = false, error = true)
            throw cancellation
        } catch (ignored: Exception) {
            try {
                clearSession(error = true)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (ignored: Exception) {
                // El bloqueo en memoria sigue activo aunque falle el disco.
                mutableState.value = SessionUiState(loading = false, error = true)
            }
        }
    }
}
