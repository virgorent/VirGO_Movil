package mx.virgo.core.network.api

import mx.virgo.core.network.dto.UserProfileDto
import retrofit2.http.GET

interface UsersApi {
    /** Backend `006-users-me`: requiere Bearer; 401 sin sesión válida. */
    @GET("api/v1/users/me")
    suspend fun me(): UserProfileDto
}
