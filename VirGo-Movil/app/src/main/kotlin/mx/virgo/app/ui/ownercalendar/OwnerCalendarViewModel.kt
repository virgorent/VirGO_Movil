package mx.virgo.app.ui.ownercalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BlockDayUseCase
import mx.virgo.core.domain.BlockReason
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.CalendarDay
import mx.virgo.core.domain.GetBookingsUseCase
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.GetVenueDaysUseCase
import mx.virgo.core.domain.OwnerCalendar
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.UnblockDayUseCase
import mx.virgo.core.domain.VenueDay
import mx.virgo.core.domain.VenueStatus
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** Lo capturado en «Bloquear este día» (`023` RF-09). */
data class BlockForm(
    val reason: BlockReason? = null,
    val note: String = "",
)

enum class BlockError {
    NOT_FREE,
    INVALID,
    GONE,
    FAILED,
}

data class OwnerCalendarUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val venues: List<OwnerVenue> = emptyList(),
    val venueId: String? = null,
    val month: YearMonth = YearMonth.now(),
    val bookings: List<Booking> = emptyList(),
    val venueDays: List<VenueDay> = emptyList(),
    val daysLoading: Boolean = false,
    val daysFailed: Boolean = false,
    val selected: LocalDate? = null,
    val blockForm: BlockForm? = null,
    val saving: Boolean = false,
    val blockError: BlockError? = null,
) {
    val days: List<CalendarDay>
        get() = venueId?.let { OwnerCalendar.month(month, it, bookings, venueDays) }.orEmpty()

    val selectedDay: CalendarDay? get() = selected?.let { date -> days.firstOrNull { it.date == date } }
}

/** «Calendario» del propietario (`023` RF-05–RF-11). */
@HiltViewModel
class OwnerCalendarViewModel
    @Inject
    constructor(
        private val getVenues: GetMyVenuesUseCase,
        private val getBookings: GetBookingsUseCase,
        private val getDays: GetVenueDaysUseCase,
        private val blockDay: BlockDayUseCase,
        private val unblockDay: UnblockDayUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        internal var today: () -> LocalDate = { LocalDate.now() }

        private val ui = MutableStateFlow(OwnerCalendarUiState())
        val state: StateFlow<OwnerCalendarUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value =
                ui.value.copy(loading = true, failed = false, month = YearMonth.from(today()))
            viewModelScope.launch {
                val venues = async { getVenues() }
                val bookings = async { getBookings() }
                val venuesResult = venues.await()
                val bookingsResult = bookings.await()
                val failure = listOf(venuesResult, bookingsResult).filterIsInstance<AppResult.Failure>().firstOrNull()
                if (failure != null) {
                    if (!session.endsSession(failure.error)) ui.edit { copy(loading = false, failed = true) }
                    return@launch
                }
                val published =
                    (venuesResult as AppResult.Success).value.filter { it.status == VenueStatus.PUBLISHED }
                val current = ui.value.venueId?.takeIf { id -> published.any { it.id == id } }
                ui.edit {
                    copy(
                        loading = false,
                        venues = published,
                        venueId = current ?: published.firstOrNull()?.id,
                        bookings = (bookingsResult as AppResult.Success).value,
                    )
                }
                loadDays()
            }
        }

        fun selectVenue(id: String) {
            if (id == ui.value.venueId) return
            ui.edit { copy(venueId = id, selected = null, venueDays = emptyList()) }
            viewModelScope.launch { loadDays() }
        }

        fun changeMonth(delta: Long) {
            ui.edit { copy(month = month.plusMonths(delta), selected = null, venueDays = emptyList()) }
            viewModelScope.launch { loadDays() }
        }

        fun selectDay(date: LocalDate?) = ui.edit { copy(selected = date, blockForm = null, blockError = null) }

        fun openBlock() {
            val day = ui.value.selectedDay ?: return
            if (!day.free || day.date.isBefore(today())) return
            ui.edit { copy(blockForm = BlockForm(), blockError = null) }
        }

        /** `null` cierra el formulario sin enviar nada. */
        fun updateBlock(form: BlockForm?) =
            ui.edit {
                copy(
                    blockForm = form?.copy(note = form.note.take(OwnerCalendar.NOTE_MAX)),
                    blockError = null,
                )
            }

        fun confirmBlock() {
            val (venueId, date, form) = ui.value.pendingBlock() ?: return
            ui.edit { copy(saving = true, blockError = null) }
            viewModelScope.launch {
                when (val result = blockDay(venueId, date, requireNotNull(form.reason), form.note)) {
                    is AppResult.Success ->
                        ui.edit {
                            copy(
                                saving = false,
                                blockForm = null,
                                venueDays = venueDays + result.value,
                            )
                        }
                    is AppResult.Failure -> onBlockFailure(result.error)
                }
            }
        }

        fun unblock() {
            val current = ui.value
            val blockId = current.selectedDay?.block?.blockId
            val venueId = current.venueId
            if (current.saving || blockId == null || venueId == null) return
            ui.edit { copy(saving = true, blockError = null) }
            viewModelScope.launch {
                when (val result = unblockDay(venueId, blockId)) {
                    is AppResult.Success ->
                        ui.edit {
                            copy(
                                saving = false,
                                venueDays = venueDays.filterNot { it.blockId == blockId },
                            )
                        }
                    is AppResult.Failure -> onBlockFailure(result.error)
                }
            }
        }

        private suspend fun loadDays() {
            val current = ui.value
            val venueId = current.venueId ?: return
            val month = current.month
            ui.edit { copy(daysLoading = true, daysFailed = false) }
            when (val result = getDays(venueId, month)) {
                is AppResult.Success ->
                    if (ui.value.venueId == venueId && ui.value.month == month) {
                        ui.edit { copy(daysLoading = false, venueDays = result.value) }
                    }
                is AppResult.Failure ->
                    if (!session.endsSession(
                            result.error,
                        )
                    ) {
                        ui.edit { copy(daysLoading = false, daysFailed = true) }
                    }
            }
        }

        private suspend fun onBlockFailure(error: AppError) {
            if (session.endsSession(error)) return
            val reason =
                when (error) {
                    AppError.Conflict -> BlockError.NOT_FREE
                    is AppError.Validation -> BlockError.INVALID
                    AppError.NotFound -> BlockError.GONE
                    else -> BlockError.FAILED
                }
            ui.edit { copy(saving = false, blockError = reason) }
            if (reason == BlockError.GONE) refresh()
        }
    }

/** Datos del bloqueo listo para enviar, o `null` si falta algo o ya se está guardando. */
private fun OwnerCalendarUiState.pendingBlock(): Triple<String, LocalDate, BlockForm>? {
    val form = blockForm?.takeIf { it.reason != null && !saving }
    return form?.let { ready -> venueId?.let { venue -> selected?.let { date -> Triple(venue, date, ready) } } }
}

/** Un 401 cierra la sesión (`030`); el resto lo maneja cada pantalla. */
internal suspend fun SessionRepository.endsSession(error: AppError): Boolean {
    if (error != AppError.Unauthorized) return false
    invalidate(SessionEndReason.REVOKED)
    return true
}

private inline fun MutableStateFlow<OwnerCalendarUiState>.edit(
    change: OwnerCalendarUiState.() -> OwnerCalendarUiState,
) {
    value = value.change()
}
