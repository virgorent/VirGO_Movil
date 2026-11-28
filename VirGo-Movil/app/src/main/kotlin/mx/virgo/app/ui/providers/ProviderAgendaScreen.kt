package mx.virgo.app.ui.providers

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.Gig
import mx.virgo.core.domain.ProviderDashboard

class ProviderAgendaActions(
    val onWeek: (Long) -> Unit = {},
    val onOpen: (Gig?) -> Unit = {},
    val onForm: (GigForm) -> Unit = {},
    val onClose: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun ProviderAgenda(viewModel: ProviderAgendaViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.form != null) BackHandler(onBack = viewModel::closeForm)
    ProviderAgendaContent(
        state,
        ProviderAgendaActions(
            onWeek = viewModel::moveWeek,
            onOpen = viewModel::openForm,
            onForm = viewModel::editForm,
            onClose = viewModel::closeForm,
            onSave = viewModel::save,
            onRetry = viewModel::refresh,
        ),
    )
}

@Composable
internal fun ProviderAgendaContent(
    state: ProviderAgendaUiState,
    actions: ProviderAgendaActions,
) {
    VirGoPageBody {
        val form = state.form
        when {
            form != null -> GigFormBody(form, state, actions)
            state.load == Load.LOADING -> LoadingState()
            state.load == Load.UNAVAILABLE -> ComingSoon(R.string.provider_agenda_unavailable)
            state.load == Load.FAILED ->
                ErrorBanner(
                    stringResource(R.string.providers_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = actions.onRetry,
                )
            else -> Week(state, actions)
        }
    }
}

@Composable
private fun Week(
    state: ProviderAgendaUiState,
    actions: ProviderAgendaActions,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { actions.onWeek(-1) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, stringResource(R.string.provider_week_previous))
        }
        Text(
            stringResource(R.string.provider_week, formatDate(state.monday)),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { actions.onWeek(1) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, stringResource(R.string.provider_week_next))
        }
    }
    val week = state.week
    if (week.isEmpty()) {
        EmptyState(title = stringResource(R.string.provider_week_empty), iconVector = Icons.Outlined.Event)
    } else {
        val overlaps = state.overlaps
        VirGoCard {
            Column(Modifier.padding(horizontal = 16.dp)) {
                week.forEachIndexed { index, gig ->
                    if (index > 0) VirGoRowDivider()
                    GigRow(gig, overlaps[gig].orEmpty(), actions.onOpen)
                }
            }
        }
    }
    VirGoButton(
        onClick = { actions.onOpen(null) },
        text = stringResource(R.string.provider_gig_add),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun GigRow(
    gig: Gig,
    clashes: List<Gig>,
    onOpen: (Gig?) -> Unit,
) {
    val schedule =
        listOfNotNull(
            gig.date?.let(::formatDate),
            if (gig.start != null && gig.end != null) "${gig.start}–${gig.end}" else null,
            gig.place.takeIf { it.isNotBlank() },
            gig.clientName.takeIf { it.isNotBlank() },
        ).joinToString(" · ")
    val clash = clashes.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.eventName }
    VirGoRow(
        title = gig.eventName,
        subtitle = clash?.let { schedule + "\n" + stringResource(R.string.provider_gig_overlap, it) } ?: schedule,
        icon = if (clash != null) Icons.Outlined.WarningAmber else Icons.Outlined.EventAvailable,
        tone = if (clash != null) VirGoTone.RED else VirGoTone.VIOLET,
        standalone = false,
        onClick = { onOpen(gig) },
        subtitleMaxLines = Int.MAX_VALUE,
    )
}

@Composable
private fun GigFormBody(
    form: GigForm,
    state: ProviderAgendaUiState,
    actions: ProviderAgendaActions,
) {
    LabeledField(R.string.provider_gig_event, form.eventName, false) { actions.onForm(form.copy(eventName = it)) }
    LabeledField(R.string.provider_gig_client, form.clientName, false) { actions.onForm(form.copy(clientName = it)) }
    LabeledField(R.string.provider_gig_place, form.place, false) { actions.onForm(form.copy(place = it)) }
    LabeledField(R.string.provider_gig_date, form.date, false) { actions.onForm(form.copy(date = it)) }
    LabeledField(R.string.provider_gig_start, form.start, false) { actions.onForm(form.copy(start = it)) }
    LabeledField(R.string.provider_gig_end, form.end, false) { actions.onForm(form.copy(end = it)) }
    LabeledField(R.string.provider_gig_amount, form.amount, false, keyboardType = KeyboardType.Number) {
        actions.onForm(form.copy(amount = it.filter(Char::isDigit)))
    }
    state.notice?.let { notice ->
        Text(
            stringResource(
                when (notice) {
                    GigNotice.INVALID -> R.string.provider_gig_invalid
                    GigNotice.UNAVAILABLE -> R.string.provider_save_unavailable
                    GigNotice.FAILED -> R.string.provider_save_failed
                },
            ),
            color = MaterialTheme.colorScheme.error,
        )
    }
    VirGoButton(
        onClick = actions.onSave,
        enabled = !state.saving,
        text = stringResource(R.string.provider_save),
        modifier = Modifier.fillMaxWidth(),
    )
    TextButton(onClick = actions.onClose) { Text(stringResource(R.string.provider_cancel)) }
}

class ProviderHomeActions(
    val onRequests: () -> Unit = {},
    val onAgenda: () -> Unit = {},
    val onProfile: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun ProviderHome(
    actions: ProviderHomeActions,
    viewModel: ProviderHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ProviderHomeContent(
        state,
        ProviderHomeActions(actions.onRequests, actions.onAgenda, actions.onProfile, viewModel::refresh),
    )
}

@Composable
internal fun ProviderHomeContent(
    state: ProviderHomeUiState,
    actions: ProviderHomeActions,
) {
    VirGoPageBody {
        val dashboard = state.dashboard
        when {
            dashboard != null -> HomeDashboard(dashboard, actions)
            state.load == Load.UNAVAILABLE -> ComingSoon(R.string.provider_agenda_unavailable)
            state.load == Load.FAILED ->
                ErrorBanner(
                    stringResource(R.string.providers_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = actions.onRetry,
                )
            else -> LoadingState()
        }
    }
}

@Composable
private fun HomeDashboard(
    dashboard: ProviderDashboard,
    actions: ProviderHomeActions,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            VirGoRow(
                title = stringResource(R.string.provider_home_new, dashboard.newRequests),
                icon = Icons.Outlined.Inbox,
                standalone = false,
                onClick = actions.onRequests,
            )
            VirGoRowDivider()
            VirGoRow(
                title =
                    stringResource(
                        R.string.provider_home_quotes,
                        dashboard.answered,
                        dashboard.accepted,
                    ),
                icon = Icons.Outlined.RequestQuote,
                standalone = false,
                onClick = actions.onRequests,
            )
        }
    }
    VirGoSection(title = stringResource(R.string.provider_home_upcoming)) {
        if (dashboard.upcoming.isEmpty()) {
            Text(
                stringResource(R.string.provider_week_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            VirGoCard {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    dashboard.upcoming.forEachIndexed { index, gig ->
                        if (index > 0) VirGoRowDivider()
                        GigRow(gig, emptyList()) { actions.onAgenda() }
                    }
                }
            }
        }
    }
    VirGoButton(
        onClick = actions.onAgenda,
        text = stringResource(R.string.demo_title_agenda),
        modifier = Modifier.fillMaxWidth(),
    )
    VirGoButton(
        onClick = actions.onProfile,
        text = stringResource(R.string.demo_title_provider_profile),
        modifier = Modifier.fillMaxWidth(),
    )
}
