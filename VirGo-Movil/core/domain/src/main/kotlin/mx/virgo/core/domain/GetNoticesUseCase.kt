package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class GetNoticesUseCase
    @Inject
    constructor(
        private val repository: NoticesRepository,
    ) {
        suspend operator fun invoke(): AppResult<List<Notice>> = repository.getNotices()
    }
