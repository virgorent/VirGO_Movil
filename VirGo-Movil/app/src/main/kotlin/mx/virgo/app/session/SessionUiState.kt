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

data class SessionUiState(
    val loading: Boolean = true,
    val session: ActiveSession? = null,
    val error: Boolean = false,
)
