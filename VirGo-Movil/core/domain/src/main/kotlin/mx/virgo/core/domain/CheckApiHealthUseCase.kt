package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class CheckApiHealthUseCase
    @Inject
    constructor(
        private val healthRepository: HealthRepository,
    ) {
        suspend operator fun invoke(): AppResult<ApiHealth> = healthRepository.checkHealth()
    }
