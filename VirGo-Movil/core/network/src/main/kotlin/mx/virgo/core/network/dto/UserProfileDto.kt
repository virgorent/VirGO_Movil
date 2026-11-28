package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

/** Campos de `AuthController`/`UsersController` (backend `006-users-me`); el OpenAPI no declara esquema. */
@Serializable
data class UserProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val isVerified: Boolean = false,
)
