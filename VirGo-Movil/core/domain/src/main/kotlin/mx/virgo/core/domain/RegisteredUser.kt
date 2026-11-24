package mx.virgo.core.domain

data class RegisteredUser(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val isVerified: Boolean,
)
