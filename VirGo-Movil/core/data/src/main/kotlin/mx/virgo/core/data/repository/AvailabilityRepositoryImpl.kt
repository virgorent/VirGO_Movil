package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AvailabilityRepository
import mx.virgo.core.domain.BlockReason
import mx.virgo.core.domain.VenueDay
import mx.virgo.core.network.api.AvailabilityApi
import mx.virgo.core.network.dto.BlockRequestDto
import mx.virgo.core.network.dto.VenueDayDto
import retrofit2.HttpException
import java.time.LocalDate
import javax.inject.Inject

class AvailabilityRepositoryImpl
    @Inject
    constructor(
        private val api: AvailabilityApi,
    ) : AvailabilityRepository {
        override suspend fun days(
            venueId: String,
            from: LocalDate,
            to: LocalDate,
        ): AppResult<List<VenueDay>> =
            safeCall {
                AppResult.Success(
                    api.availability(venueId, from.toString(), to.toString()).data.mapNotNull { it.toVenueDay() },
                )
            }

        override suspend fun block(
            venueId: String,
            date: LocalDate,
            reason: BlockReason,
            note: String?,
        ): AppResult<VenueDay> =
            safeCall {
                val created = api.block(venueId, BlockRequestDto(date.toString(), reason.wire, note)).toVenueDay()
                AppResult.Success(created ?: VenueDay(date, blocked = true, reason = reason, note = note))
            }

        override suspend fun unblock(
            venueId: String,
            blockId: String,
        ): AppResult<Unit> =
            safeCall {
                val response = api.unblock(venueId, blockId)
                if (response.isSuccessful) {
                    AppResult.Success(
                        Unit,
                    )
                } else {
                    AppResult.Failure(HttpException(response).toAppError())
                }
            }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun <T> safeCall(block: suspend () -> AppResult<T>): AppResult<T> =
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun VenueDayDto.toVenueDay(): VenueDay? {
    val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    return VenueDay(
        date = day,
        blocked = kind == null || kind == "blocked",
        reason = BlockReason.fromWire(reason),
        note = note?.takeIf { it.isNotBlank() },
        blockId = id?.takeIf { it.isNotBlank() },
    )
}
