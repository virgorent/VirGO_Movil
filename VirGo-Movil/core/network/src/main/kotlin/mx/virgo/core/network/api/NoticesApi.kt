package mx.virgo.core.network.api

import mx.virgo.core.network.dto.NoticesResponseDto
import retrofit2.http.GET

interface NoticesApi {
    @GET("api/v1/notices")
    suspend fun notices(): NoticesResponseDto
}
