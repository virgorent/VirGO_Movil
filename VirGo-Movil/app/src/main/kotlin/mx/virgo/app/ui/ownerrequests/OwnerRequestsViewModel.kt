package mx.virgo.app.ui.ownerrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AnswerField
import mx.virgo.core.domain.AnswerProblem
import mx.virgo.core.domain.GetQuoteUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteAnswerValidator
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.domain.RespondQuoteUseCase
import javax.inject.Inject

/** Callbacks de P7/P8, agrupados para probar el contenido sin `ViewModel`. */
data class OwnerRequestsActions(
    val onTab: (RequestsTab) -> Unit = {},
    val onOpen: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRetryDetail: (String) -> Unit = {},
    val onForm: (AnswerForm) -> Unit = {},
    val onSend: () -> Unit = {},
)

/** Pestañas de P7 (024 RF-01). */
enum class RequestsTab {
    NEW,
    ANSWERED,
    ALL,
}

/** Lo capturado en P8; se conserva ante errores (024 RF-06). */
data class AnswerForm(
    val total: String = "",
    val deposit: String = "",
    val message: String = "",
)

enum class AnswerError {
    NETWORK,
    NOT_FOUND,
    FORBIDDEN,
    ALREADY_ANSWERED,
    REJECTED,
    SERVER,
}

data class RequestDetailState(
    val quoteId: String,
    val loading: Boolean = true,
    val quote: Quote? = null,
    val failed: Boolean = false,
    val form: AnswerForm = AnswerForm(),
    val fieldErrors: Map<AnswerField, AnswerProblem> = emptyMap(),
    val sending: Boolean = false,
    val error: AnswerError? = null,
    /** La respuesta se acaba de enviar desde esta pantalla. */
    val justAnswered: Boolean = false,
)

data class OwnerRequestsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val quotes: List<Quote> = emptyList(),
    val tab: RequestsTab = RequestsTab.NEW,
    val detail: RequestDetailState? = null,
) {
    val visible: List<Quote>
        get() =
            when (tab) {
                RequestsTab.NEW -> quotes.filter { it.status == QuoteStatus.SENT }
                RequestsTab.ANSWERED -> quotes.filter { it.status == QuoteStatus.ANSWERED }
                RequestsTab.ALL -> quotes
            }
}

@HiltViewModel
class OwnerRequestsViewModel
    @Inject
    constructor(
        private val getQuotes: GetQuotesUseCase,
        private val getQuote: GetQuoteUseCase,
        private val respondQuote: RespondQuoteUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(OwnerRequestsUiState())
        val state: StateFlow<OwnerRequestsUiState> = mutableState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            mutableState.value = mutableState.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = getQuotes()) {
                    is AppResult.Success ->
                        mutableState.value = mutableState.value.copy(loading = false, quotes = result.value)
                    is AppResult.Failure ->
                        if (!endsSession(result.error)) {
                            mutableState.value = mutableState.value.copy(loading = false, failed = true)
                        }
                }
            }
        }

        fun selectTab(tab: RequestsTab) {
            mutableState.value = mutableState.value.copy(tab = tab)
        }

        /** Abre el detalle leyendo el servidor; `keep` conserva el aviso o el formulario actual. */
        fun open(
            quoteId: String,
            keep: RequestDetailState? = null,
        ) {
            updateDetail(
                quoteId,
                create = true,
            ) { (keep ?: RequestDetailState(quoteId)).copy(loading = true, failed = false) }
            viewModelScope.launch {
                val result = getQuote(quoteId)
                if (result is AppResult.Failure && endsSession(result.error)) return@launch
                updateDetail(quoteId) {
                    when (result) {
                        is AppResult.Success -> it.copy(loading = false, quote = result.value)
                        is AppResult.Failure -> it.copy(loading = false, failed = true)
                    }
                }
            }
        }

        fun closeDetail() {
            mutableState.value = mutableState.value.copy(detail = null)
        }

        fun updateForm(form: AnswerForm) {
            val detail = mutableState.value.detail ?: return
            val changed =
                buildSet {
                    if (form.total != detail.form.total) add(AnswerField.TOTAL)
                    if (form.deposit != detail.form.deposit) add(AnswerField.DEPOSIT)
                    if (form.message != detail.form.message) add(AnswerField.MESSAGE)
                }
            updateDetail(detail.quoteId) { it.copy(form = form, fieldErrors = it.fieldErrors - changed, error = null) }
        }

        /** Valida y envía una sola vez (024 RF-05). */
        fun send() {
            val detail =
                mutableState.value.detail?.takeIf { !it.sending && it.quote?.status == QuoteStatus.SENT } ?: return
            val (answer, errors) =
                QuoteAnswerValidator.validate(
                    detail.form.total,
                    detail.form.deposit,
                    detail.form.message,
                )
            updateDetail(detail.quoteId) { it.copy(fieldErrors = errors, sending = answer != null, error = null) }
            if (answer == null) return
            viewModelScope.launch {
                when (val result = respondQuote(detail.quoteId, answer)) {
                    is AppResult.Success -> {
                        updateDetail(detail.quoteId) {
                            it.copy(sending = false, quote = result.value, justAnswered = true, form = AnswerForm())
                        }
                        mutableState.value =
                            mutableState.value.copy(
                                quotes =
                                    mutableState.value.quotes.map { q ->
                                        if (q.id ==
                                            result.value.id
                                        ) {
                                            result.value
                                        } else {
                                            q
                                        }
                                    },
                            )
                    }
                    is AppResult.Failure -> handleFailure(detail.quoteId, result.error)
                }
            }
        }

        private suspend fun handleFailure(
            quoteId: String,
            error: AppError,
        ) {
            if (endsSession(error)) return
            val mapped =
                when (error) {
                    AppError.Network, AppError.Timeout -> AnswerError.NETWORK
                    AppError.NotFound -> AnswerError.NOT_FOUND
                    AppError.Forbidden -> AnswerError.FORBIDDEN
                    AppError.Conflict -> AnswerError.ALREADY_ANSWERED
                    is AppError.Validation -> AnswerError.REJECTED
                    else -> AnswerError.SERVER
                }
            updateDetail(quoteId) { it.copy(sending = false, error = mapped) }
            // Otro dispositivo pudo responder antes: se muestra lo que tiene el servidor (024 ADR-03).
            if (mapped == AnswerError.ALREADY_ANSWERED) {
                open(quoteId, keep = mutableState.value.detail)
                refresh()
            }
        }

        /** Solo toca el detalle abierto; una respuesta tardía no reabre uno cerrado. */
        private fun updateDetail(
            quoteId: String,
            create: Boolean = false,
            transform: (RequestDetailState) -> RequestDetailState,
        ) {
            val current = mutableState.value.detail
            val base =
                when {
                    create -> current?.takeIf { it.quoteId == quoteId } ?: RequestDetailState(quoteId)
                    current?.quoteId == quoteId -> current
                    else -> return
                }
            mutableState.value = mutableState.value.copy(detail = transform(base))
        }

        private suspend fun endsSession(error: AppError): Boolean {
            if (error != AppError.Unauthorized) return false
            session.invalidate(SessionEndReason.REVOKED)
            return true
        }
    }
