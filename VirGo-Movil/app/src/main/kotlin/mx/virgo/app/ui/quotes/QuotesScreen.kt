package mx.virgo.app.ui.quotes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.BookingConfirmActions
import mx.virgo.app.ui.bookings.BookingConfirmScreen
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.Quote

/**
 * «Mis cotizaciones» del cliente: lista real, detalle y, si el salón respondió, reservar
 * con la cotización (008 RF-07–RF-11). El paso de reservar sobrevive a la rotación.
 * Lleva su propio encabezado: así la confirmación no queda debajo de «Mis cotizaciones»
 * y la flecha del detalle vuelve a la lista.
 */
@Composable
fun ClientQuotes(
    onCatalog: () -> Unit,
    onMyBookings: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: QuotesViewModel = hiltViewModel(),
    bookingStep: @Composable (Quote, () -> Unit) -> Unit = { quote, back ->
        BookingConfirmScreen(
            venueId = requireNotNull(quote.venueId),
            actions = BookingConfirmActions(onBack = back, onMyBookings = onMyBookings, onCatalog = onCatalog),
            quoteId = quote.id,
            date = quote.eventDate,
            quotedTotal = quote.total,
        )
    },
) {
    val state by viewModel.state.collectAsState()
    var booking by rememberSaveable { mutableStateOf(false) }
    val quote = state.detail?.quote
    if (booking && quote != null && quote.canBook) {
        BackHandler { booking = false }
        bookingStep(quote) { booking = false }
        return
    }
    if (state.detail != null) BackHandler(onBack = viewModel::closeDetail)
    Column(Modifier.fillMaxSize()) {
        VirGoPageHeader(
            title = stringResource(R.string.demo_title_quotes),
            kicker = stringResource(R.string.demo_role_client),
            onBack = if (state.detail != null) viewModel::closeDetail else onBack,
            backContentDescription = stringResource(R.string.demo_back),
        )
        QuotesContent(
            state,
            QuotesActions(
                onOpen = viewModel::open,
                onRetry = viewModel::refresh,
                onRetryDetail = viewModel::open,
                onCatalog = onCatalog,
                onBook = { booking = true },
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuotesContent(
    state: QuotesUiState,
    actions: QuotesActions,
) {
    val detail = state.detail
    if (detail != null) {
        VirGoPageBody { DetailFromList(detail, actions) }
        return
    }
    PullToRefreshBox(isRefreshing = state.loading && state.quotes.isNotEmpty(), onRefresh = actions.onRetry) {
        VirGoPageBody {
            when {
                state.loading && state.quotes.isEmpty() ->
                    LoadingState(
                        message = stringResource(R.string.quotes_loading),
                    )
                state.failed ->
                    ErrorBanner(
                        message = stringResource(R.string.quotes_error),
                        actionLabel = stringResource(R.string.quote_retry),
                        onAction = actions.onRetry,
                    )
                state.quotes.isEmpty() ->
                    VirGoCard {
                        EmptyState(
                            title = stringResource(R.string.quotes_empty),
                            text = stringResource(R.string.quotes_empty_hint),
                            iconVector = Icons.Outlined.RequestQuote,
                            action = {
                                VirGoButton(
                                    onClick = actions.onCatalog,
                                    text = stringResource(R.string.quotes_to_catalog),
                                )
                            },
                        )
                    }
                else -> QuoteList(state.quotes, actions.onOpen)
            }
        }
    }
}

@Composable
private fun DetailFromList(
    detail: QuoteDetailState,
    actions: QuotesActions,
) {
    val quote = detail.quote
    when {
        quote != null -> QuoteDetailBody(quote, actions.onBook)
        detail.failed ->
            ErrorBanner(
                message = stringResource(R.string.quotes_detail_error),
                actionLabel = stringResource(R.string.quote_retry),
                onAction = { actions.onRetryDetail(detail.quoteId) },
            )
        else -> LoadingState()
    }
}

@Composable
private fun QuoteList(
    quotes: List<Quote>,
    onOpen: (String) -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            quotes.forEachIndexed { index, quote ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = quote.venueName,
                    subtitle =
                        stringResource(
                            R.string.quotes_item,
                            quote.eventDate?.let(::formatDate).orEmpty(),
                            quote.guests,
                        ),
                    icon = Icons.Outlined.RequestQuote,
                    tone = VirGoTone.VIOLET,
                    standalone = false,
                    onClick = { onOpen(quote.id) },
                    trailing = { QuoteStatusBadge(quote.status) },
                )
            }
        }
    }
}
