package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BookingTest {
    private val today = LocalDate.parse("2026-11-25")

    private fun validate(
        date: LocalDate?,
        hours: Int?,
        minimum: Int? = null,
    ) = BookingValidator.validate(BookingRequest("v1", date, hours), today, minimum)

    @Test
    fun todayAndFutureDatesAreValid() {
        assertTrue(validate(today, 1).isEmpty())
        assertTrue(validate(today.plusDays(1), 5, minimum = 5).isEmpty())
    }

    @Test
    fun pastOrMissingDateIsRejected() {
        assertEquals(BookingProblem.PAST_DATE, validate(today.minusDays(1), 4)[BookingField.DATE])
        assertEquals(BookingProblem.REQUIRED, validate(null, 4)[BookingField.DATE])
    }

    @Test
    fun hoursRespectTheVenueMinimum() {
        assertEquals(BookingProblem.REQUIRED, validate(today, 0)[BookingField.HOURS])
        assertEquals(BookingProblem.REQUIRED, validate(today, null)[BookingField.HOURS])
        assertEquals(BookingProblem.BELOW_MINIMUM, validate(today, 3, minimum = 4)[BookingField.HOURS])
        assertTrue(validate(today, 4, minimum = 4).isEmpty())
        assertTrue(validate(today, 1, minimum = null).isEmpty())
    }

    @Test
    fun statusesFromTheServer() {
        assertEquals(BookingStatus.PENDING_PAYMENT, BookingStatus.fromWire("pending_payment"))
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.fromWire("confirmed"))
        assertEquals(BookingStatus.CANCELLED, BookingStatus.fromWire("cancelled"))
        assertEquals(BookingStatus.UNKNOWN, BookingStatus.fromWire("refunded"))
        assertEquals(BookingStatus.UNKNOWN, BookingStatus.fromWire(null))
    }

    @Test
    fun useCasesDelegateAndSortNewestFirst() =
        runTest {
            val older = booking("a", "2026-11-20T10:00:00Z")
            val newer = booking("b", "2026-11-24T10:00:00Z")
            val repository =
                object : BookingsRepository {
                    val keys = mutableListOf<String>()

                    override suspend fun create(
                        request: BookingRequest,
                        idempotencyKey: String,
                    ): AppResult<Booking> {
                        keys += idempotencyKey
                        return AppResult.Success(older)
                    }

                    override suspend fun list(): AppResult<List<Booking>> = AppResult.Success(listOf(older, newer))

                    override suspend fun get(id: String): AppResult<Booking> = AppResult.Failure(AppError.NotFound)
                }
            assertEquals(listOf("b", "a"), (GetBookingsUseCase(repository)() as AppResult.Success).value.map { it.id })
            CreateBookingUseCase(repository)(BookingRequest("v1", today, 2), "key-1")
            assertEquals(listOf("key-1"), repository.keys)
            assertEquals(AppResult.Failure(AppError.NotFound), GetBookingUseCase(repository)("x"))
        }

    @Test
    fun folioIsNormalizedAndValidated() {
        assertEquals("VG-2026-0001", BookingFolio.normalize(" vg-2026-0001 "))
        assertEquals("VG-2026-0001", BookingFolio.normalize("VG - 2026 - 0001"))
        assertNull(BookingFolio.normalize(""))
        assertNull(BookingFolio.normalize("VG-26-1"))
        assertNull(BookingFolio.normalize("XX-2026-0001"))
        assertNull(BookingFolio.normalize("VG-2026-0001/../x"))
        assertNull(BookingFolio.normalize("VG-2026-00012"))
    }

    private fun booking(
        id: String,
        createdAt: String,
    ) = Booking(id, "VG-$id", "v1", "Salón", today, 2, null, null, BookingStatus.PENDING_PAYMENT, createdAt)
}
