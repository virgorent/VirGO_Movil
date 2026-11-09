package mx.virgo.core.domain

data class AuthSession(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresAt: String,
    val user: LoggedInUser,
)

data class LoggedInUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
)
