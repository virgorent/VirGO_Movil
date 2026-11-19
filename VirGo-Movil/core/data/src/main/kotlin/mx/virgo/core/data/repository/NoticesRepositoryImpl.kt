package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Notice
import mx.virgo.core.domain.NoticesRepository
import mx.virgo.core.network.api.NoticesApi
import mx.virgo.core.network.dto.NoticeDto
import javax.inject.Inject

class NoticesRepositoryImpl
    @Inject
    constructor(
        private val noticesApi: NoticesApi,
    ) : NoticesRepository {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun getNotices(): AppResult<List<Notice>> =
            try {
                val response = noticesApi.notices()
                AppResult.Success(response.data.map { it.toNotice() })
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun NoticeDto.toNotice(): Notice =
    Notice(
        id = id,
        type = type,
        title = title,
        body = body,
        isRead = readAt != null,
        createdAt = createdAt,
    )
