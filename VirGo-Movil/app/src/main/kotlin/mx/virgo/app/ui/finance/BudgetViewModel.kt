package mx.virgo.app.ui.finance

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
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.BudgetPreference
import mx.virgo.core.domain.BudgetRules
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.EventFinanceRepository
import java.math.BigDecimal
import javax.inject.Inject

/** Resultado de guardar en el servidor (`015` RF-08, `018` RF-05). */
enum class SaveNotice {
    SAVED,
    UNAVAILABLE,
    FAILED,
}

data class BudgetUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val finance: EventFinance? = null,
    val preferences: Set<BudgetPreference> = emptySet(),
    /** Montos editados a mano en la sugerencia; los demás salen de las reglas. */
    val edits: Map<BudgetCategory, BigDecimal> = emptyMap(),
    val saving: Boolean = false,
    val notice: SaveNotice? = null,
) {
    val suggestion: Map<BudgetCategory, BigDecimal>
        get() {
            val event = finance?.event ?: return emptyMap()
            return BudgetRules.suggest(event.eventType, event.budget, preferences) + edits
        }
}

/** «Presupuesto» y «Presupuesto sugerido» del primer evento (`015`). */
@HiltViewModel
class BudgetViewModel
    @Inject
    constructor(
        private val repository: EventFinanceRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(BudgetUiState())
        val state: StateFlow<BudgetUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = repository.current()) {
                    is AppResult.Success -> ui.value = ui.value.copy(loading = false, finance = result.value)
                    is AppResult.Failure ->
                        if (!session.endsSession(result.error)) ui.value = ui.value.copy(loading = false, failed = true)
                }
            }
        }

        fun togglePreference(preference: BudgetPreference) {
            val current = ui.value.preferences
            ui.value =
                ui.value.copy(preferences = if (preference in current) current - preference else current + preference)
        }

        /** Solo dígitos; vacío cuenta como 0. */
        fun edit(
            category: BudgetCategory,
            input: String,
        ) {
            val amount =
                input
                    .filter(Char::isDigit)
                    .take(AMOUNT_DIGITS)
                    .ifEmpty { "0" }
                    .toBigDecimal()
            ui.value = ui.value.copy(edits = ui.value.edits + (category to amount), notice = null)
        }

        fun reset() {
            ui.value = ui.value.copy(edits = emptyMap(), preferences = emptySet(), notice = null)
        }

        /** Guarda la sugerencia como plan (`015` RF-06). */
        fun apply() {
            val current = ui.value
            val finance = current.finance ?: return
            if (current.saving) return
            val plan = current.suggestion
            ui.value = current.copy(saving = true, notice = null)
            viewModelScope.launch {
                ui.value =
                    when (val result = repository.savePlan(finance.event.id, plan)) {
                        is AppResult.Success ->
                            ui.value.copy(
                                saving = false,
                                notice = SaveNotice.SAVED,
                                finance = finance.copy(plan = plan),
                            )
                        is AppResult.Failure ->
                            ui.value.copy(
                                saving = false,
                                notice =
                                    saveProblem(result.error) ?: return@launch,
                            )
                    }
            }
        }

        private suspend fun saveProblem(error: AppError): SaveNotice? = financeProblem(session, error)

        private companion object {
            const val AMOUNT_DIGITS = 9
        }
    }

/** `400`/`404` al guardar significa que el backend aún no acepta el campo (contrato §4); `null` si cerró sesión. */
internal suspend fun financeProblem(
    session: SessionRepository,
    error: AppError,
): SaveNotice? =
    when {
        session.endsSession(error) -> null
        error == AppError.NotFound || error is AppError.Validation -> SaveNotice.UNAVAILABLE
        else -> SaveNotice.FAILED
    }
