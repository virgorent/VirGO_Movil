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
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.EventFinanceRepository
import mx.virgo.core.domain.ExternalPayment
import mx.virgo.core.domain.PaymentProblem
import mx.virgo.core.domain.PaymentRules
import mx.virgo.core.domain.PaymentStatus
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/** Lo capturado en «Registrar pago» (`018` RF-03). */
data class PaymentForm(
    val concept: String = "",
    val amount: String = "",
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val date: LocalDate? = null,
)

data class PaymentsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val finance: EventFinance? = null,
    val form: PaymentForm? = null,
    val problems: Set<PaymentProblem> = emptySet(),
    val saving: Boolean = false,
    val notice: SaveNotice? = null,
)

/** «Pagos» del primer evento (`018`): la app no cobra, solo registra lo pagado por fuera. */
@HiltViewModel
class PaymentsViewModel
    @Inject
    constructor(
        private val repository: EventFinanceRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        internal var today: () -> LocalDate = { LocalDate.now() }
        internal var newId: () -> String = { UUID.randomUUID().toString() }

        private val ui = MutableStateFlow(PaymentsUiState())
        val state: StateFlow<PaymentsUiState> = ui.asStateFlow()

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

        /** `null` cierra el formulario. */
        fun updateForm(form: PaymentForm?) {
            ui.value = ui.value.copy(form = form, problems = emptySet(), notice = null)
        }

        fun add() {
            val current = ui.value
            val form = current.form ?: return
            val amount = form.amount.replace(',', '.').toBigDecimalOrNull()
            val problems = PaymentRules.validate(form.concept, amount, form.date, today())
            if (problems.isNotEmpty() || amount == null) {
                ui.value = current.copy(problems = problems)
                return
            }
            val payment = ExternalPayment(newId(), form.concept.trim(), amount, form.status, form.date ?: today())
            save(listOf(payment) + current.finance?.payments.orEmpty(), closeForm = true)
        }

        fun remove(id: String) =
            save(
                ui.value.finance
                    ?.payments
                    .orEmpty()
                    .filterNot { it.id == id },
                closeForm = false,
            )

        /** Cambio optimista: si el servidor falla, la lista vuelve a como estaba (`018` RF-05). */
        private fun save(
            payments: List<ExternalPayment>,
            closeForm: Boolean,
        ) {
            val current = ui.value
            val finance = current.finance ?: return
            if (current.saving) return
            ui.value = current.copy(saving = true, notice = null, finance = finance.copy(payments = payments))
            viewModelScope.launch {
                ui.value =
                    when (val result = repository.savePayments(finance.event.id, payments)) {
                        is AppResult.Success ->
                            ui.value.copy(
                                saving = false,
                                notice = SaveNotice.SAVED,
                                form = if (closeForm) null else ui.value.form,
                            )
                        is AppResult.Failure ->
                            ui.value.copy(
                                saving = false,
                                finance = finance,
                                notice = financeProblem(session, result.error) ?: return@launch,
                            )
                    }
            }
        }
    }
