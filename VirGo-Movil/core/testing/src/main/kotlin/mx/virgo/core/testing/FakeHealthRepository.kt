package mx.virgo.core.testing

import kotlinx.coroutines.yield
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ApiHealth
import mx.virgo.core.domain.HealthRepository

class FakeHealthRepository(
    var result: AppResult<ApiHealth> = AppResult.Success(ApiHealth(status = "Healthy", latencyMs = 12)),
) : HealthRepository {
    var calls: Int = 0
        private set

    override suspend fun checkHealth(): AppResult<ApiHealth> {
        calls += 1
        yield()
        return result
    }
}
