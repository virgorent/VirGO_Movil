package mx.virgo.app.ui.events

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.EventDateField
import mx.virgo.app.ui.quotes.eventTypeLabel
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.EventField
import mx.virgo.core.domain.EventProblem
import mx.virgo.core.domain.EventValidator
import mx.virgo.core.domain.QuoteEventType

/** C12: crear (`eventId` nulo) o editar un evento (007). */
@Composable
fun EventFormScreen(
    eventId: String?,
    onBack: () -> Unit,
    onSaved: (ClientEvent) -> Unit,
    viewModel: EventFormViewModel = hiltViewModel(),
) {
    LaunchedEffect(eventId) { viewModel.start(eventId) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { state.saved?.let(onSaved) }
    EventFormContent(
        state,
        EventFormActions(
            onForm = viewModel::update,
            onSave = viewModel::save,
            onRetry = { eventId?.let(viewModel::load) },
            onBack = onBack,
        ),
    )
}

@Composable
internal fun EventFormContent(
    state: EventFormUiState,
    actions: EventFormActions,
) {
    VirGoPageBody {
        Text(
            stringResource(if (state.eventId == null) R.string.event_form_create else R.string.event_form_edit),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        when {
            state.loading -> LoadingState()
            state.eventId != null && state.original == null ->
                ErrorBanner(
                    message = stringResource(eventErrorText(state.error ?: EventError.SERVER)),
                    actionLabel = stringResource(R.string.events_retry),
                    onAction = actions.onRetry,
                )
            else -> FormFields(state, actions)
        }
        SecondaryAction(stringResource(R.string.guests_form_cancel), actions.onBack)
    }
}

@Composable
private fun FormFields(
    state: EventFormUiState,
    actions: EventFormActions,
) {
    val form = state.form
    LabeledInput(stringResource(R.string.event_form_name), form.name, state.errorFor(EventField.NAME)) {
        actions.onForm(form.copy(name = it))
    }
    ChipChoice(
        label = stringResource(R.string.event_form_type),
        options = QuoteEventType.entries,
        selected = form.eventType,
        errorText = state.errorFor(EventField.TYPE),
        optionLabel = { stringResource(eventTypeLabel(it)) },
    ) { actions.onForm(form.copy(eventType = it)) }
    EventDateField(stringResource(R.string.event_form_date), form.date, state.errorFor(EventField.DATE)) {
        actions.onForm(form.copy(date = it))
    }
    LabeledInput(
        stringResource(R.string.event_form_guests),
        form.guests,
        state.errorFor(EventField.GUESTS),
        keyboardType = KeyboardType.Number,
    ) { actions.onForm(form.copy(guests = it.filter(Char::isDigit))) }
    LabeledInput(
        stringResource(R.string.event_form_hours),
        form.hours,
        state.errorFor(EventField.HOURS),
        keyboardType = KeyboardType.Number,
    ) { actions.onForm(form.copy(hours = it.filter(Char::isDigit))) }
    LabeledInput(
        stringResource(R.string.event_form_budget),
        form.budget,
        state.errorFor(EventField.BUDGET),
        hint = stringResource(R.string.event_form_budget_hint),
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Done,
    ) { actions.onForm(form.copy(budget = it)) }
    state.original?.venueName?.let {
        Text(stringResource(R.string.event_form_venue_kept, it), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    state.error?.let { ErrorBanner(message = stringResource(eventErrorText(it))) }
    VirGoButton(
        onClick = actions.onSave,
        enabled = !state.saving,
        text = stringResource(if (state.saving) R.string.event_form_saving else R.string.event_form_save),
    )
}

/** El mensaje del servidor tiene prioridad: ya viene en español. */
@Composable
private fun EventFormUiState.errorFor(field: EventField): String? =
    serverErrors[field] ?: fieldErrors[field]?.let { problem ->
        when {
            field == EventField.NAME && problem == EventProblem.TOO_LONG ->
                stringResource(R.string.event_error_name_long, EventValidator.MAX_NAME)
            field == EventField.NAME -> stringResource(R.string.event_error_name_required)
            field == EventField.TYPE -> stringResource(R.string.event_error_type_required)
            field == EventField.DATE && problem == EventProblem.PAST_DATE ->
                stringResource(
                    R.string.event_error_date_past,
                )
            field == EventField.DATE -> stringResource(R.string.event_error_date_required)
            field == EventField.BUDGET -> stringResource(R.string.event_error_budget)
            else -> stringResource(R.string.event_error_number)
        }
    }
