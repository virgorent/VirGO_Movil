package mx.virgo.core.domain

import mx.virgo.core.common.AppResult

interface NoticesRepository {
    suspend fun getNotices(): AppResult<List<Notice>>
}
