package mx.virgo.app.ui.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.EventDateField
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.designsystem.theme.pageBackground
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.QuoteField
import mx.virgo.core.domain.QuoteProblem
import mx.virgo.core.domain.QuoteValidator

/** Solicitud a un salón (`008`) o a un proveedor (`011` RF-04). */
@Composable
fun QuoteRequestScreen(
    target: QuoteTarget,
    actions: QuoteRequestActions,
    viewModel: QuoteRequestViewModel = hiltViewModel(),
) {
    LaunchedEffect(target) { viewModel.start(target) }
    val state by viewModel.state.collectAsState()
    QuoteRequestContent(
        state,
        actions.copy(
            onEventType = viewModel::setEventType,
            onDate = viewModel::setDate,
            onGuests = viewModel::setGuests,
            onMessage = viewModel::setMessage,
            onSend = viewModel::send,
            onRetryVenue = viewModel::loadVenue,
        ),
    )
}

@Composable
internal fun QuoteRequestContent(
    state: QuoteRequestUiState,
    actions: QuoteRequestActions,
) {
    Column(Modifier.fillMaxSize().background(pageBackground())) {
        VirGoPageHeader(
            title = stringResource(if (state.sent != null) R.string.quote_sent_title else R.string.quote_request_title),
            kicker = stringResource(R.string.quote_kicker),
            onBack = actions.onBack,
            backContentDescription = stringResource(R.string.quote_back),
        )
        VirGoPageBody {
            when {
                state.sent != null -> Sent(actions.onMyQuotes)
                state.error == QuoteError.NOT_FOUND && state.venue == null ->
                    EmptyState(title = stringResource(R.string.quote_error_not_found))
                state.loadingVenue -> LoadingState()
                state.venueFailed ->
                    ErrorBanner(
                        message = stringResource(R.string.quote_venue_error),
                        actionLabel = stringResource(R.string.quote_retry),
                        onAction = actions.onRetryVenue,
                    )
                else -> RequestForm(state, actions)
            }
        }
    }
}

@Composable
private fun Sent(onMyQuotes: () -> Unit) {
    VirGoCard {
        EmptyState(
            title = stringResource(R.string.quote_sent_title),
            text = stringResource(R.string.quote_sent_text),
            iconVector = Icons.Outlined.MarkEmailRead,
            action = { VirGoButton(onClick = onMyQuotes, text = stringResource(R.string.quote_my_quotes)) },
        )
    }
}

@Composable
private fun RequestForm(
    state: QuoteRequestUiState,
    actions: QuoteRequestActions,
) {
    val venue = state.venue
    val name = venue?.name ?: state.draft.targetName ?: return
    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    EventTypeField(state.eventType, state.errorFor(QuoteField.EVENT_TYPE), actions.onEventType)
    EventDateField(
        label = stringResource(R.string.quote_date),
        date = state.date,
        errorText = state.errorFor(QuoteField.DATE),
        onDate = actions.onDate,
    )
    QuoteTextField(
        label = stringResource(R.string.quote_guests),
        value = state.draft.guests,
        errorText = state.errorFor(QuoteField.GUESTS),
        onValueChange = actions.onGuests,
        keyboardType = KeyboardType.Number,
    )
    if (venue != null && state.capacityWarning) {
        VirGoCard {
            VirGoRow(
                title = stringResource(R.string.quote_capacity_warning, venue.capacity),
                icon = Icons.Outlined.Groups,
                tone = VirGoTone.AMBER,
                standalone = false,
            )
        }
    }
    QuoteTextField(
        label = stringResource(R.string.quote_message),
        value = state.draft.message,
        errorText = state.errorFor(QuoteField.MESSAGE),
        onValueChange = actions.onMessage,
        hint =
            stringResource(R.string.quote_message_count, state.draft.message.length, QuoteValidator.MAX_MESSAGE),
        singleLine = false,
    )
    if (state.rejected) ErrorBanner(message = stringResource(R.string.quote_error_rejected))
    state.error?.let { ErrorBanner(message = stringResource(quoteErrorText(it))) }
    VirGoButton(
        onClick = actions.onSend,
        enabled = !state.sending,
        text = stringResource(if (state.sending) R.string.quote_sending else R.string.quote_send),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventTypeField(
    selected: QuoteEventType?,
    errorText: String?,
    onSelect: (QuoteEventType) -> Unit,
) {
    val label = stringResource(R.string.quote_event_type)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics { if (errorText != null) error(errorText) },
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuoteEventType.entries.forEach { type ->
                FilterChip(
                    selected = type == selected,
                    onClick = { onSelect(type) },
                    label = { Text(stringResource(eventTypeLabel(type))) },
                )
            }
        }
        errorText?.let { FieldError(it) }
    }
}

@Composable
private fun QuoteTextField(
    label: String,
    value: String,
    errorText: String?,
    onValueChange: (String) -> Unit,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        VirGoTextField(
            value = value,
            onValueChange = onValueChange,
            isError = errorText != null,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier =
                Modifier.semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
        )
        when {
            errorText != null -> FieldError(errorText)
            hint != null ->
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

@Composable
private fun FieldError(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

/** El mensaje del servidor tiene prioridad: ya viene en español (008 RF-06). */
@Composable
private fun QuoteRequestUiState.errorFor(field: QuoteField): String? =
    serverErrors[field] ?: fieldErrors[field]?.let { problem ->
        when {
            field == QuoteField.EVENT_TYPE -> stringResource(R.string.quote_error_type_required)
            field == QuoteField.MESSAGE ->
                stringResource(R.string.quote_error_message_long, QuoteValidator.MAX_MESSAGE)
            field == QuoteField.GUESTS -> stringResource(R.string.quote_error_guests_required)
            problem == QuoteProblem.PAST_DATE -> stringResource(R.string.quote_error_date_past)
            else -> stringResource(R.string.quote_error_date_required)
        }
    }

internal fun eventTypeLabel(type: QuoteEventType): Int =
    when (type) {
        QuoteEventType.WEDDING -> R.string.quote_type_wedding
        QuoteEventType.QUINCEANERA -> R.string.quote_type_quinceanera
        QuoteEventType.BIRTHDAY -> R.string.quote_type_birthday
        QuoteEventType.CORPORATE -> R.string.quote_type_corporate
        QuoteEventType.OTHER -> R.string.quote_type_other
    }

private fun quoteErrorText(error: QuoteError): Int =
    when (error) {
        QuoteError.NETWORK -> R.string.quote_error_network
        QuoteError.NOT_FOUND -> R.string.quote_error_not_found
        QuoteError.FORBIDDEN -> R.string.quote_error_forbidden
        QuoteError.SERVER -> R.string.quote_error_server
    }
