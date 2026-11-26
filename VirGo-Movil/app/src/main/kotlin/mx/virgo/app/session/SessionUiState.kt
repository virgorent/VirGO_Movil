package mx.virgo.app.session

enum class SessionRole {
    CLIENT,
    OWNER,
    PROVIDER,
}

data class ActiveSession(
    val accountId: String,
    val name: String,
    val email: String,
    val role: SessionRole,
)

/** Motivo de un cierre que la persona debe conocer (030 §6.3). Se muestra una sola vez. */
enum class SessionEndReason {
    EXPIRED,
    REVOKED,
    ROLE_MISMATCH,
    ACCOUNT_UNAVAILABLE,
    LOGOUT_UNCONFIRMED,
}

data class SessionUiState(
    val loading: Boolean = true,
    val session: ActiveSession? = null,
    val error: Boolean = false,
    val loggingOut: Boolean = false,
    val endReason: SessionEndReason? = null,
)
