package mx.virgo.app.ui.quotes

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
import mx.virgo.core.domain.GetQuoteUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.Quote
import javax.inject.Inject

/** Callbacks de C22/C7, agrupados para probar el contenido sin `ViewModel`. */
data class QuotesActions(
    val onOpen: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRetryDetail: (String) -> Unit = {},
    val onCatalog: () -> Unit = {},
    val onBook: (Quote) -> Unit = {},
)

/** Detalle abierto desde la lista: se lee de nuevo del servidor (008 RF-10). */
data class QuoteDetailState(
    val quoteId: String,
    val loading: Boolean = true,
    val quote: Quote? = null,
    val failed: Boolean = false,
)

data class QuotesUiState(
    val loading: Boolean = true,
    val quotes: List<Quote> = emptyList(),
    val failed: Boolean = false,
    val detail: QuoteDetailState? = null,
)

@HiltViewModel
class QuotesViewModel
    @Inject
    constructor(
        private val getQuotes: GetQuotesUseCase,
        private val getQuote: GetQuoteUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(QuotesUiState())
        val state: StateFlow<QuotesUiState> = mutableState.asStateFlow()

        init {
            refresh()
        }

        /** También lo usa «tirar para actualizar» (008 RF-09). */
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

        fun open(quoteId: String) {
            mutableState.value = mutableState.value.copy(detail = QuoteDetailState(quoteId))
            viewModelScope.launch {
                val result = getQuote(quoteId)
                if (result is AppResult.Failure && endsSession(result.error)) return@launch
                val detail =
                    when (result) {
                        is AppResult.Success -> QuoteDetailState(quoteId, loading = false, quote = result.value)
                        is AppResult.Failure -> QuoteDetailState(quoteId, loading = false, failed = true)
                    }
                if (mutableState.value.detail?.quoteId == quoteId) {
                    mutableState.value = mutableState.value.copy(detail = detail)
                }
            }
        }

        fun closeDetail() {
            mutableState.value = mutableState.value.copy(detail = null)
        }

        private suspend fun endsSession(error: AppError): Boolean {
            if (error != AppError.Unauthorized) return false
            session.invalidate(SessionEndReason.REVOKED)
            return true
        }
    }
