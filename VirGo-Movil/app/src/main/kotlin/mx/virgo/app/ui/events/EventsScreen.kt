package mx.virgo.app.ui.events

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
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
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.quotes.eventTypeLabel
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
import mx.virgo.core.domain.ClientEvent

/**
 * «Mi evento» del cliente (014): lista → detalle → editar (007), invitados (016) o pendientes
 * (017). `openStep` abre ese paso al elegir un evento (rutas «Invitados» y «Pendientes»).
 */
@Composable
fun ClientEvents(
    openStep: EventStep? = null,
    @StringRes title: Int = R.string.demo_title_event,
    onBack: (() -> Unit)? = null,
    viewModel: EventsViewModel = hiltViewModel(),
    form: @Composable (String?, () -> Unit, (ClientEvent) -> Unit) -> Unit = { id, back, saved ->
        EventFormScreen(id, onBack = back, onSaved = saved)
    },
    guests: @Composable (ClientEvent, () -> Unit) -> Unit = { event, back -> GuestsScreen(event, back) },
    tasks: @Composable (String, () -> Unit) -> Unit = { id, back -> TasksScreen(id, back) },
) {
    val state by viewModel.state.collectAsState()
    var step by rememberSaveable { mutableStateOf<EventStep?>(null) }
    val detail = state.detail
    val back = { step = null }
    val saved: (ClientEvent) -> Unit = {
        step = null
        viewModel.refresh()
        viewModel.open(it.id)
    }
    val event = detail?.event
    // Encabezado propio: su flecha deshace el paso interno antes de salir de «Mi Evento».
    val innerBack: (() -> Unit)? =
        if (step != null) {
            back
        } else if (detail != null) {
            viewModel::closeDetail
        } else {
            null
        }
    Column(Modifier.fillMaxSize()) {
        EventsHeader(title, innerBack ?: onBack)
        when {
            step == EventStep.CREATE -> {
                BackHandler(onBack = back)
                form(null, back, saved)
            }
            detail != null && step == EventStep.EDIT -> {
                BackHandler(onBack = back)
                form(detail.eventId, back, saved)
            }
            event != null && step == EventStep.GUESTS -> {
                BackHandler(onBack = back)
                guests(event, back)
            }
            detail != null && step == EventStep.TASKS -> {
                BackHandler(onBack = back)
                tasks(detail.eventId, back)
            }
            detail != null -> {
                BackHandler(onBack = viewModel::closeDetail)
                EventDetailStep(detail, viewModel) { step = it }
            }
            else ->
                EventsListContent(
                    state,
                    onOpen = {
                        viewModel.open(it)
                        step = openStep
                    },
                    onCreate = { step = EventStep.CREATE },
                    onRetry = viewModel::refresh,
                )
        }
    }
}

@Composable
private fun EventsHeader(
    @StringRes title: Int,
    onBack: (() -> Unit)?,
) = VirGoPageHeader(
    title = stringResource(title),
    kicker = stringResource(R.string.demo_role_client),
    onBack = onBack,
    backContentDescription = stringResource(R.string.demo_back),
)

@Composable
private fun EventDetailStep(
    detail: EventDetailState,
    viewModel: EventsViewModel,
    onStep: (EventStep) -> Unit,
) {
    VirGoPageBody {
        EventDetailBody(
            detail,
            EventDetailActions(
                onEdit = { onStep(EventStep.EDIT) },
                onGuests = { onStep(EventStep.GUESTS) },
                onTasks = { onStep(EventStep.TASKS) },
                onRetry = { viewModel.open(detail.eventId) },
                onBack = viewModel::closeDetail,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventsListContent(
    state: EventsUiState,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onRetry: () -> Unit,
) {
    PullToRefreshBox(isRefreshing = state.loading && state.events.isNotEmpty(), onRefresh = onRetry) {
        VirGoPageBody {
            when {
                state.loading && state.events.isEmpty() ->
                    LoadingState(
                        message = stringResource(R.string.events_loading),
                    )
                state.failed ->
                    ErrorBanner(
                        message = stringResource(R.string.events_error),
                        actionLabel = stringResource(R.string.events_retry),
                        onAction = onRetry,
                    )
                state.events.isEmpty() ->
                    VirGoCard {
                        EmptyState(
                            title = stringResource(R.string.events_empty),
                            text = stringResource(R.string.events_empty_hint),
                            iconVector = Icons.Outlined.Celebration,
                            action = { VirGoButton(onClick = onCreate, text = stringResource(R.string.events_create)) },
                        )
                    }
                else -> {
                    EventList(state.events, onOpen)
                    VirGoButton(onClick = onCreate, text = stringResource(R.string.events_create))
                }
            }
        }
    }
}

@Composable
private fun EventList(
    events: List<ClientEvent>,
    onOpen: (String) -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            events.forEachIndexed { index, event ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = event.name,
                    subtitle =
                        stringResource(
                            R.string.events_item,
                            event.eventType?.let { stringResource(eventTypeLabel(it)) }.orEmpty(),
                            event.date?.let(::formatDate).orEmpty(),
                            event.guests,
                        ),
                    icon = Icons.Outlined.Celebration,
                    tone = VirGoTone.VIOLET,
                    standalone = false,
                    onClick = { onOpen(event.id) },
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}
