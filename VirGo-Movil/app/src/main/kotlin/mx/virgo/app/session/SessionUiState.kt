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

    /** `021` RF-04: el servidor borró la cuenta (204). */
    ACCOUNT_DELETED,

    /** `021` RF-05: 401 al borrar; la cuenta no se borró. */
    DELETE_EXPIRED,
}

data class SessionUiState(
    val loading: Boolean = true,
    val session: ActiveSession? = null,
    val error: Boolean = false,
    val loggingOut: Boolean = false,
    val endReason: SessionEndReason? = null,
)
