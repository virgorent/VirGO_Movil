package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ApiHealth
import mx.virgo.core.domain.HealthRepository
import mx.virgo.core.network.api.HealthApi
import javax.inject.Inject
import kotlin.time.TimeSource

class HealthRepositoryImpl
    @Inject
    constructor(
        private val healthApi: HealthApi,
    ) : HealthRepository {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun checkHealth(): AppResult<ApiHealth> {
            val start = TimeSource.Monotonic.markNow()
            return try {
                val response = healthApi.health()
                AppResult.Success(
                    ApiHealth(
                        status = response.status,
                        latencyMs = start.elapsedNow().inWholeMilliseconds,
                    ),
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
        }
    }
