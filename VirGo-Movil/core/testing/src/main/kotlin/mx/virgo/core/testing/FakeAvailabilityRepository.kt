package mx.virgo.core.testing

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AvailabilityRepository
import mx.virgo.core.domain.BlockReason
import mx.virgo.core.domain.VenueDay
import java.time.LocalDate

/** Como el servidor: un día bloqueado no se puede volver a bloquear (409). */
class FakeAvailabilityRepository : AvailabilityRepository {
    val days = mutableMapOf<String, MutableList<VenueDay>>()
    var daysFailure: AppResult.Failure? = null
    var blockFailure: AppResult.Failure? = null
    val queries = mutableListOf<Triple<String, LocalDate, LocalDate>>()
    var blockCalls: Int = 0
        private set

    override suspend fun days(
        venueId: String,
        from: LocalDate,
        to: LocalDate,
    ): AppResult<List<VenueDay>> {
        queries += Triple(venueId, from, to)
        daysFailure?.let { return it }
        return AppResult.Success(days[venueId].orEmpty().filter { !it.date.isBefore(from) && !it.date.isAfter(to) })
    }

    override suspend fun block(
        venueId: String,
        date: LocalDate,
        reason: BlockReason,
        note: String?,
    ): AppResult<VenueDay> {
        blockCalls += 1
        val list = days.getOrPut(venueId) { mutableListOf() }
        val failure = blockFailure
        return when {
            failure != null -> failure
            list.any { it.date == date } -> AppResult.Failure(AppError.Conflict)
            else -> {
                val id = "block-${list.size + 1}"
                val day = VenueDay(date, blocked = true, reason = reason, note = note, blockId = id)
                list += day
                AppResult.Success(day)
            }
        }
    }

    override suspend fun unblock(
        venueId: String,
        blockId: String,
    ): AppResult<Unit> {
        val removed = days[venueId]?.removeAll { it.blockId == blockId } == true
        return if (removed) AppResult.Success(Unit) else AppResult.Failure(AppError.NotFound)
    }
}
