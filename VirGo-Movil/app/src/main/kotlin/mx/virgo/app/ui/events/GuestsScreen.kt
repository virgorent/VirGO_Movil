package mx.virgo.app.ui.events

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.quotes.eventTypeLabel
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.EventGuest
import mx.virgo.core.domain.GuestField
import mx.virgo.core.domain.GuestStatus
import mx.virgo.core.domain.GuestValidator

/** D32 + C31: invitados del evento y compartir la invitación (016). */
@Composable
fun GuestsScreen(
    event: ClientEvent,
    onBack: () -> Unit,
    viewModel: GuestsViewModel = hiltViewModel(),
) {
    LaunchedEffect(event.id) { viewModel.start(event.id) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.guests_share_title)
    val shareText = invitationText(event)
    GuestsContent(
        state,
        event.name,
        GuestsActions(
            onAdd = { viewModel.edit(null) },
            onEdit = { viewModel.edit(it) },
            onCancel = { viewModel.edit(null, close = true) },
            onEditor = viewModel::updateEditor,
            onSave = viewModel::save,
            onDelete = { viewModel.delete(it) },
            onConfirmDelete = { viewModel.delete(it, confirm = true) },
            onRetry = viewModel::refresh,
            onShare = {
                val send =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                context.startActivity(Intent.createChooser(send, shareTitle))
            },
            onBack = onBack,
        ),
    )
}

@Composable
internal fun GuestsContent(
    state: GuestsUiState,
    eventName: String,
    actions: GuestsActions,
) {
    VirGoPageBody {
        Text(
            eventName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        val editor = state.editor
        when {
            editor != null -> GuestForm(editor, state.saving, actions)
            state.loading && state.guests.isEmpty() -> LoadingState(message = stringResource(R.string.guests_loading))
            state.failed ->
                ErrorBanner(
                    message = stringResource(R.string.guests_error),
                    actionLabel = stringResource(R.string.events_retry),
                    onAction = actions.onRetry,
                )
            else -> GuestList(state, actions)
        }
        state.error?.let { ErrorBanner(message = stringResource(eventErrorText(it))) }
        SecondaryAction(stringResource(R.string.event_back), actions.onBack)
    }
    state.deleting?.let { guest ->
        ConfirmDelete(
            title = stringResource(R.string.guests_delete_title, guest.name),
            onConfirm = { actions.onConfirmDelete(guest) },
            onDismiss = { actions.onDelete(null) },
        )
    }
}

@Composable
private fun GuestList(
    state: GuestsUiState,
    actions: GuestsActions,
) {
    val totals = state.totals
    Text(stringResource(R.string.guests_totals, totals.confirmed, totals.pending, totals.declined))
    VirGoButton(onClick = actions.onAdd, text = stringResource(R.string.guests_add))
    SecondaryAction(stringResource(R.string.guests_share), actions.onShare)
    if (state.guests.isEmpty()) {
        VirGoCard { EmptyState(title = stringResource(R.string.guests_empty), iconVector = Icons.Outlined.Person) }
        return
    }
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            state.guests.forEachIndexed { index, guest ->
                if (index > 0) VirGoRowDivider()
                GuestRow(guest, actions)
            }
        }
    }
}

@Composable
private fun GuestRow(
    guest: EventGuest,
    actions: GuestsActions,
) {
    val details =
        listOfNotNull(
            guest.phone,
            stringResource(R.string.guests_item_companions, guest.companions),
            guest.table,
        ).joinToString(" · ")
    VirGoRow(
        title = guest.name,
        subtitle = details,
        icon = Icons.Outlined.Person,
        tone = statusTone(guest.status),
        standalone = false,
        onClick = { actions.onEdit(guest) },
        trailing = { VirGoBadge(text = stringResource(statusLabel(guest.status)), tone = statusTone(guest.status)) },
        subtitleMaxLines = Int.MAX_VALUE,
    )
    SecondaryAction(stringResource(R.string.guests_delete)) { actions.onDelete(guest) }
}

@Composable
private fun GuestForm(
    editor: GuestEditor,
    saving: Boolean,
    actions: GuestsActions,
) {
    Text(
        stringResource(if (editor.guestId == null) R.string.guests_form_new else R.string.guests_form_edit),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    LabeledInput(
        stringResource(R.string.guests_form_name),
        editor.name,
        errorIf(GuestField.NAME in editor.errors, R.string.guests_error_name, GuestValidator.MAX_NAME),
    ) { actions.onEditor(editor.copy(name = it)) }
    LabeledInput(
        stringResource(R.string.guests_form_phone),
        editor.phone,
        errorIf(GuestField.PHONE in editor.errors, R.string.guests_error_phone, GuestValidator.MAX_PHONE),
        keyboardType = KeyboardType.Phone,
    ) { actions.onEditor(editor.copy(phone = it)) }
    LabeledInput(
        stringResource(R.string.guests_form_companions),
        editor.companions,
        if (GuestField.COMPANIONS in editor.errors) stringResource(R.string.guests_error_companions) else null,
        keyboardType = KeyboardType.Number,
    ) { actions.onEditor(editor.copy(companions = it.filter(Char::isDigit))) }
    LabeledInput(
        stringResource(R.string.guests_form_table),
        editor.table,
        errorIf(GuestField.TABLE in editor.errors, R.string.guests_error_table, GuestValidator.MAX_TABLE),
    ) { actions.onEditor(editor.copy(table = it)) }
    ChipChoice(
        label = stringResource(R.string.guests_form_status),
        options = GuestStatus.selectable,
        selected = editor.status,
        errorText = null,
        optionLabel = { stringResource(statusLabel(it)) },
    ) { actions.onEditor(editor.copy(status = it)) }
    VirGoButton(
        onClick = actions.onSave,
        enabled = !saving,
        text = stringResource(if (saving) R.string.event_form_saving else R.string.guests_form_save),
    )
    SecondaryAction(stringResource(R.string.guests_form_cancel), actions.onCancel)
}

@Composable
private fun errorIf(
    condition: Boolean,
    text: Int,
    limit: Int,
): String? = if (condition) stringResource(text, limit) else null

/** Texto de la invitación con los datos reales del evento (016 RF-05). */
@Composable
private fun invitationText(event: ClientEvent): String {
    val type = event.eventType?.let { stringResource(eventTypeLabel(it)) }.orEmpty()
    val base = stringResource(R.string.guests_share_text, event.name, type, event.date?.let(::formatDate).orEmpty())
    val venue = event.venueName?.let { stringResource(R.string.guests_share_venue, it) }
    return listOfNotNull(base, venue).joinToString(" ")
}

internal fun statusLabel(status: GuestStatus): Int =
    when (status) {
        GuestStatus.CONFIRMED -> R.string.guests_status_confirmed
        GuestStatus.PENDING -> R.string.guests_status_pending
        GuestStatus.DECLINED -> R.string.guests_status_declined
        GuestStatus.UNKNOWN -> R.string.guests_status_unknown
    }

private fun statusTone(status: GuestStatus): VirGoTone =
    when (status) {
        GuestStatus.CONFIRMED -> VirGoTone.GREEN
        GuestStatus.PENDING -> VirGoTone.AMBER
        GuestStatus.DECLINED -> VirGoTone.RED
        GuestStatus.UNKNOWN -> VirGoTone.GRAY
    }
