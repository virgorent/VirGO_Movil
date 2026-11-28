package mx.virgo.app.ui.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Gig
import mx.virgo.core.domain.GigSchedule
import mx.virgo.core.domain.ProviderDashboard
import mx.virgo.core.domain.ProvidersRepository
import mx.virgo.core.domain.QuotesRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Lo capturado del servicio (`027` RF-03); horas `HH:mm`. */
data class GigForm(
    val id: String? = null,
    val eventName: String = "",
    val clientName: String = "",
    val place: String = "",
    val date: String = "",
    val start: String = "",
    val end: String = "",
    val amount: String = "",
) {
    fun toGig(): Gig? {
        val day = runCatching { LocalDate.parse(date.trim()) }.getOrNull()
        val from = runCatching { LocalTime.parse(start.trim()) }.getOrNull()
        val to = runCatching { LocalTime.parse(end.trim()) }.getOrNull()
        val valid = day != null && from != null && to != null && eventName.isNotBlank() && from.isBefore(to)
        return if (valid) {
            Gig(
                id = id,
                eventName = eventName.trim(),
                clientName = clientName.trim(),
                place = place.trim(),
                date = day,
                start = from,
                end = to,
                amount = amount.trim().takeIf { it.isNotEmpty() }?.toBigDecimalOrNull(),
            )
        } else {
            null
        }
    }

    companion object {
        fun from(gig: Gig): GigForm =
            GigForm(
                gig.id,
                gig.eventName,
                gig.clientName,
                gig.place,
                gig.date?.toString().orEmpty(),
                gig.start?.toString().orEmpty(),
                gig.end?.toString().orEmpty(),
                gig.amount?.toPlainString().orEmpty(),
            )
    }
}

enum class GigNotice {
    INVALID,
    UNAVAILABLE,
    FAILED,
}

data class ProviderAgendaUiState(
    val load: Load = Load.LOADING,
    val gigs: List<Gig> = emptyList(),
    val monday: LocalDate = GigSchedule.weekStart(LocalDate.now()),
    val form: GigForm? = null,
    val saving: Boolean = false,
    val notice: GigNotice? = null,
) {
    val week: List<Gig> get() = GigSchedule.inWeek(gigs, monday)
    val overlaps: Map<Gig, List<Gig>> get() = GigSchedule.overlaps(week)
}

/** Agenda semanal del proveedor (`027` RF-02–RF-04). */
@HiltViewModel
class ProviderAgendaViewModel
    @Inject
    constructor(
        private val repository: ProvidersRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ProviderAgendaUiState())
        val state: StateFlow<ProviderAgendaUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(load = Load.LOADING)
            viewModelScope.launch {
                when (val result = repository.gigs()) {
                    is AppResult.Success -> ui.value = ui.value.copy(load = Load.READY, gigs = result.value)
                    is AppResult.Failure -> session.loadFor(result.error)?.let { ui.value = ui.value.copy(load = it) }
                }
            }
        }

        fun moveWeek(weeks: Long) {
            ui.value = ui.value.copy(monday = ui.value.monday.plusWeeks(weeks))
        }

        fun openForm(gig: Gig?) {
            ui.value =
                ui.value.copy(
                    form = gig?.let(GigForm::from) ?: GigForm(date = ui.value.monday.toString()),
                    notice = null,
                )
        }

        fun editForm(form: GigForm) {
            ui.value = ui.value.copy(form = form, notice = null)
        }

        fun closeForm() {
            ui.value = ui.value.copy(form = null, notice = null)
        }

        fun save() {
            val current = ui.value
            val gig = current.form?.toGig()
            if (current.saving) return
            if (gig == null) {
                ui.value = current.copy(notice = GigNotice.INVALID)
                return
            }
            ui.value = current.copy(saving = true, notice = null)
            viewModelScope.launch {
                when (val result = repository.saveGig(gig)) {
                    is AppResult.Success -> {
                        val saved = result.value
                        val others = ui.value.gigs.filterNot { it.id != null && it.id == saved.id }
                        ui.value = ui.value.copy(saving = false, form = null, gigs = others + saved)
                    }
                    is AppResult.Failure -> {
                        val load = session.loadFor(result.error) ?: return@launch
                        val notice = if (load == Load.UNAVAILABLE) GigNotice.UNAVAILABLE else GigNotice.FAILED
                        ui.value = ui.value.copy(saving = false, notice = notice)
                    }
                }
            }
        }
    }

data class ProviderHomeUiState(
    val load: Load = Load.LOADING,
    val dashboard: ProviderDashboard? = null,
)

/** Inicio del proveedor (`027` RF-01): cotizaciones y agenda; si falta una, cuenta con cero. */
@HiltViewModel
class ProviderHomeViewModel
    @Inject
    constructor(
        private val providers: ProvidersRepository,
        private val quotes: QuotesRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ProviderHomeUiState())
        val state: StateFlow<ProviderHomeUiState> = ui.asStateFlow()

        /** Reloj inyectable en pruebas. */
        internal var today: () -> LocalDate = { LocalDate.now() }

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(load = Load.LOADING)
            viewModelScope.launch {
                val quoteCall = async { quotes.list() }
                val gigCall = async { providers.gigs() }
                val quoteResult = quoteCall.await()
                val gigResult = gigCall.await()
                val failures = listOf(quoteResult, gigResult).filterIsInstance<AppResult.Failure>()
                if (failures.size == 2) {
                    session.loadFor(failures.first().error)?.let { ui.value = ui.value.copy(load = it) }
                    return@launch
                }
                failures.forEach { if (session.loadFor(it.error) == null) return@launch }
                val quoteList = (quoteResult as? AppResult.Success)?.value.orEmpty()
                val gigList = (gigResult as? AppResult.Success)?.value.orEmpty()
                ui.value = ProviderHomeUiState(Load.READY, ProviderDashboard.from(quoteList, gigList, today()))
            }
        }
    }
