package mx.virgo.core.network.api

import mx.virgo.core.network.dto.HealthStatusDto
import retrofit2.http.GET

interface HealthApi {
    @GET("health")
    suspend fun health(): HealthStatusDto
}
