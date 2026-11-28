package mx.virgo.app.ui.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.endsSession
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.DecorIdea
import mx.virgo.core.domain.EventPlanningRules
import mx.virgo.core.domain.EventsRepository
import mx.virgo.core.domain.GetDecorIdeasUseCase
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.domain.IdeasRequest
import mx.virgo.core.domain.StaffRole
import mx.virgo.core.domain.TableLayout
import java.math.BigDecimal
import javax.inject.Inject

enum class StudioTab {
    TABLES,
    STAFF,
    IDEAS,
}

/** Estado de las ideas con IA (`012` RF-06–RF-08). */
enum class IdeasStatus {
    IDLE,
    LOADING,
    READY,
    UNAVAILABLE,
    BUSY,
    FAILED,
}

data class StudioUiState(
    val loading: Boolean = true,
    val event: ClientEvent? = null,
    val guestsInput: String = DEFAULT_GUESTS.toString(),
    val venueCapacity: Int? = null,
    val tab: StudioTab = StudioTab.TABLES,
    val layout: TableLayout = TableLayout.CENTER_DANCE_FLOOR,
    val staff: Map<StaffRole, Int> = EventPlanningRules.staff(DEFAULT_GUESTS),
    val ideas: List<DecorIdea> = emptyList(),
    val ideasStatus: IdeasStatus = IdeasStatus.IDLE,
) {
    /** Invitados válidos (1–2000) o `null` si lo capturado no sirve. */
    val guests: Int?
        get() =
            guestsInput.toIntOrNull()?.takeIf {
                it in EventPlanningRules.MIN_GUESTS..EventPlanningRules.MAX_GUESTS
            }

    val staffEdited: Boolean get() = guests?.let { staff != EventPlanningRules.staff(it) } == true

    val decorationBudget: BigDecimal? get() =
        event
            ?.budget
            ?.takeIf {
                it.signum() > 0
            }?.let(EventPlanningRules::decorationBudget)

    val ideasTotal: BigDecimal get() = ideas.fold(BigDecimal.ZERO) { sum, idea -> sum + idea.cost }
}

private const val DEFAULT_GUESTS = 100

/** «IA Studio» (`012`): mesas y personal con reglas del prototipo; ideas desde la IA del servidor. */
@HiltViewModel
class StudioViewModel
    @Inject
    constructor(
        private val events: EventsRepository,
        private val venueDetail: GetVenueDetailUseCase,
        private val getIdeas: GetDecorIdeasUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(StudioUiState())
        val state: StateFlow<StudioUiState> = ui.asStateFlow()

        init {
            viewModelScope.launch { loadEvent() }
        }

        fun selectTab(tab: StudioTab) {
            ui.value = ui.value.copy(tab = tab)
            if (tab == StudioTab.IDEAS && ui.value.ideasStatus == IdeasStatus.IDLE) loadIdeas()
        }

        fun selectLayout(layout: TableLayout) {
            ui.value = ui.value.copy(layout = layout)
        }

        /** Solo dígitos; al cambiar los invitados el personal vuelve a la sugerencia. */
        fun changeGuests(input: String) {
            val digits = input.filter(Char::isDigit).take(GUESTS_DIGITS)
            val next = ui.value.copy(guestsInput = digits)
            ui.value = next.guests?.let { next.copy(staff = EventPlanningRules.staff(it)) } ?: next
        }

        fun adjustStaff(
            role: StaffRole,
            delta: Int,
        ) {
            val current = ui.value.staff
            ui.value = ui.value.copy(staff = current + (role to maxOf(0, (current[role] ?: 0) + delta)))
        }

        fun resetStaff() {
            val guests = ui.value.guests ?: return
            ui.value = ui.value.copy(staff = EventPlanningRules.staff(guests))
        }

        fun loadIdeas() {
            val current = ui.value
            val guests = current.guests ?: return
            if (current.ideasStatus == IdeasStatus.LOADING) return
            ui.value = current.copy(ideasStatus = IdeasStatus.LOADING)
            viewModelScope.launch {
                val request = IdeasRequest(current.event?.eventType, guests, current.event?.budget)
                ui.value =
                    when (val result = getIdeas(request)) {
                        is AppResult.Success -> ui.value.copy(ideas = result.value, ideasStatus = IdeasStatus.READY)
                        is AppResult.Failure -> ui.value.copy(ideasStatus = ideasProblem(result.error) ?: return@launch)
                    }
            }
        }

        private suspend fun loadEvent() {
            val event = (events.list() as? AppResult.Success)?.value?.firstOrNull()
            ui.value =
                if (event == null) {
                    ui.value.copy(loading = false)
                } else {
                    val guests = event.guests.coerceIn(EventPlanningRules.MIN_GUESTS, EventPlanningRules.MAX_GUESTS)
                    ui.value.copy(
                        loading = false,
                        event = event,
                        guestsInput = guests.toString(),
                        staff = EventPlanningRules.staff(guests),
                    )
                }
            val venueId = event?.venueId ?: return
            val capacity = (venueDetail(venueId) as? AppResult.Success)?.value?.capacity
            ui.value = ui.value.copy(venueCapacity = capacity?.takeIf { it > 0 })
        }

        /** `null` cuando el 401 ya cerró la sesión. */
        private suspend fun ideasProblem(error: AppError): IdeasStatus? =
            when {
                session.endsSession(error) -> null
                error == AppError.NotFound -> IdeasStatus.UNAVAILABLE
                error == AppError.RateLimited -> IdeasStatus.BUSY
                else -> IdeasStatus.FAILED
            }

        private companion object {
            const val GUESTS_DIGITS = 4
        }
    }
