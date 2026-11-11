package mx.virgo.core.domain

import mx.virgo.core.common.AppResult

interface HealthRepository {
    suspend fun checkHealth(): AppResult<ApiHealth>
}
