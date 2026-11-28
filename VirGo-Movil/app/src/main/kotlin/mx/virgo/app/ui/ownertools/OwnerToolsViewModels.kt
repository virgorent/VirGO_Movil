package mx.virgo.app.ui.ownertools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.endsSession
import mx.virgo.app.ui.finance.SaveNotice
import mx.virgo.app.ui.finance.financeProblem
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.GetBookingsUseCase
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.MonthStats
import mx.virgo.core.domain.OwnerPayments
import mx.virgo.core.domain.OwnerStats
import mx.virgo.core.domain.OwnerToolsRepository
import mx.virgo.core.domain.OwnerVenueTools
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.VenuePricing
import mx.virgo.core.domain.VenueSetups
import java.time.LocalDate
import javax.inject.Inject

data class VenueToolsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val venues: List<OwnerVenueTools> = emptyList(),
    val venueId: String? = null,
    val pricing: VenuePricing? = null,
    val setups: VenueSetups = VenueSetups(),
    val saving: Boolean = false,
    val notice: SaveNotice? = null,
)

/** «Paquetes y precios» y «Planos y montajes» (`025` RF-01 y RF-02). */
@HiltViewModel
class VenueToolsViewModel
    @Inject
    constructor(
        private val repository: OwnerToolsRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(VenueToolsUiState())
        val state: StateFlow<VenueToolsUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = repository.venues()) {
                    is AppResult.Success -> {
                        val published = result.value.filter { it.published }
                        ui.value = ui.value.copy(loading = false, venues = published)
                        published.firstOrNull()?.let { select(it.id) }
                    }
                    is AppResult.Failure ->
                        if (!session.endsSession(result.error)) ui.value = ui.value.copy(loading = false, failed = true)
                }
            }
        }

        fun select(venueId: String) {
            val venue = ui.value.venues.firstOrNull { it.id == venueId } ?: return
            ui.value =
                ui.value.copy(
                    venueId = venueId,
                    pricing = venue.pricing,
                    setups = venue.setups ?: VenueSetups(),
                    notice = null,
                )
        }

        fun editPricing(pricing: VenuePricing) {
            ui.value = ui.value.copy(pricing = pricing, notice = null)
        }

        fun editSetups(setups: VenueSetups) {
            ui.value = ui.value.copy(setups = setups, notice = null)
        }

        fun savePricing() {
            val pricing = ui.value.pricing?.takeIf { it.valid } ?: return
            save { repository.savePricing(it, pricing) }
        }

        fun saveSetups() = save { repository.saveSetups(it, ui.value.setups) }

        private fun save(block: suspend (String) -> AppResult<Unit>) {
            val current = ui.value
            val venueId = current.venueId ?: return
            if (current.saving) return
            ui.value = current.copy(saving = true, notice = null)
            viewModelScope.launch {
                val notice =
                    when (val result = block(venueId)) {
                        is AppResult.Success -> SaveNotice.SAVED
                        is AppResult.Failure -> financeProblem(session, result.error) ?: return@launch
                    }
                ui.value = ui.value.copy(saving = false, notice = notice)
            }
        }
    }

data class OwnerInsightsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val payments: OwnerPayments? = null,
    val months: Int = 3,
    val stats: List<MonthStats> = emptyList(),
)

/** «Pagos recibidos» y «Estadísticas» calculados con reservaciones y cotizaciones (`025` RF-03 y RF-04). */
@HiltViewModel
class OwnerInsightsViewModel
    @Inject
    constructor(
        private val getVenues: GetMyVenuesUseCase,
        private val getQuotes: GetQuotesUseCase,
        private val getBookings: GetBookingsUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        internal var today: () -> LocalDate = { LocalDate.now() }

        private val ui = MutableStateFlow(OwnerInsightsUiState())
        val state: StateFlow<OwnerInsightsUiState> = ui.asStateFlow()
        private var data: Triple<Set<String>, List<Quote>, List<Booking>>? = null

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                val venues = async { getVenues() }
                val quotes = async { getQuotes() }
                val bookings = async { getBookings() }
                val results = listOf(venues.await(), quotes.await(), bookings.await())
                val failure = results.filterIsInstance<AppResult.Failure>().firstOrNull()
                if (failure != null) {
                    if (!session.endsSession(failure.error)) ui.value = ui.value.copy(loading = false, failed = true)
                    return@launch
                }
                val ids = (venues.await() as AppResult.Success).value.map { it.id }.toSet()
                data =
                    Triple(
                        ids,
                        (quotes.await() as AppResult.Success).value,
                        (bookings.await() as AppResult.Success).value,
                    )
                recompute(ui.value.months)
            }
        }

        fun selectMonths(months: Int) = recompute(months)

        private fun recompute(months: Int) {
            val (ids, quotes, bookings) = data ?: return
            ui.value =
                ui.value.copy(
                    loading = false,
                    months = months,
                    payments = OwnerPayments.from(ids, bookings),
                    stats = OwnerStats.monthly(ids, quotes, bookings, months, today()),
                )
        }
    }
