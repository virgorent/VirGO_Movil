package mx.virgo.app.ui.ownerrequests

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.quotes.QuoteStatusBadge
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.Quote

/** «Solicitudes» del propietario: lista por estado y responder (024). */
@Composable
fun OwnerRequests(viewModel: OwnerRequestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.detail != null) BackHandler(onBack = viewModel::closeDetail)
    OwnerRequestsContent(
        state,
        OwnerRequestsActions(
            onTab = viewModel::selectTab,
            onOpen = { viewModel.open(it) },
            onRetry = viewModel::refresh,
            onRetryDetail = { viewModel.open(it) },
            onForm = viewModel::updateForm,
            onSend = viewModel::send,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OwnerRequestsContent(
    state: OwnerRequestsUiState,
    actions: OwnerRequestsActions,
) {
    val detail = state.detail
    if (detail != null) {
        VirGoPageBody { RequestDetail(detail, actions) }
        return
    }
    PullToRefreshBox(isRefreshing = state.loading && state.quotes.isNotEmpty(), onRefresh = actions.onRetry) {
        VirGoPageBody {
            Tabs(state.tab, actions.onTab)
            when {
                state.loading && state.quotes.isEmpty() ->
                    LoadingState(message = stringResource(R.string.requests_loading))
                state.failed ->
                    ErrorBanner(
                        message = stringResource(R.string.requests_error),
                        actionLabel = stringResource(R.string.requests_retry),
                        onAction = actions.onRetry,
                    )
                state.visible.isEmpty() ->
                    VirGoCard {
                        EmptyState(
                            title = stringResource(emptyTitle(state.tab)),
                            text = stringResource(R.string.requests_empty_hint),
                            iconVector = Icons.Outlined.Inbox,
                        )
                    }
                else -> RequestList(state.visible, actions.onOpen)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Tabs(
    selected: RequestsTab,
    onTab: (RequestsTab) -> Unit,
) {
    val label = stringResource(R.string.requests_tabs)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.semantics { contentDescription = label },
    ) {
        RequestsTab.entries.forEach { tab ->
            FilterChip(
                selected = tab == selected,
                onClick = { onTab(tab) },
                label = { Text(stringResource(tabLabel(tab))) },
            )
        }
    }
}

@Composable
private fun RequestList(
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
                            R.string.requests_item,
                            quote.clientName,
                            quote.eventDate?.let(::formatDate).orEmpty(),
                            quote.guests,
                        ),
                    icon = Icons.Outlined.RequestQuote,
                    tone = VirGoTone.VIOLET,
                    standalone = false,
                    onClick = { onOpen(quote.id) },
                    trailing = { QuoteStatusBadge(quote.status) },
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

@Composable
private fun RequestDetail(
    detail: RequestDetailState,
    actions: OwnerRequestsActions,
) {
    val quote = detail.quote
    when {
        quote != null -> AnswerSection(quote, detail, actions)
        detail.failed ->
            ErrorBanner(
                message = stringResource(R.string.requests_detail_error),
                actionLabel = stringResource(R.string.requests_retry),
                onAction = { actions.onRetryDetail(detail.quoteId) },
            )
        else -> LoadingState()
    }
}

private fun tabLabel(tab: RequestsTab): Int =
    when (tab) {
        RequestsTab.NEW -> R.string.requests_tab_new
        RequestsTab.ANSWERED -> R.string.requests_tab_answered
        RequestsTab.ALL -> R.string.requests_tab_all
    }

private fun emptyTitle(tab: RequestsTab): Int =
    when (tab) {
        RequestsTab.NEW -> R.string.requests_empty_new
        RequestsTab.ANSWERED -> R.string.requests_empty_answered
        RequestsTab.ALL -> R.string.requests_empty_all
    }
