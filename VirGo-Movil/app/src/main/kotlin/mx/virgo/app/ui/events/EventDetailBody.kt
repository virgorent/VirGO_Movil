package mx.virgo.app.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.quotes.eventTypeLabel
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.ClientEvent
import java.time.LocalDate

/** D27: datos clave del evento y accesos (014 RF-02/RF-03). */
@Composable
internal fun EventDetailBody(
    detail: EventDetailState,
    actions: EventDetailActions,
    today: LocalDate = LocalDate.now(),
) {
    val event = detail.event
    when {
        event != null -> EventSummary(event, actions, today)
        detail.error == EventError.NOT_FOUND ->
            VirGoCard { EmptyState(title = stringResource(R.string.event_error_not_found)) }
        detail.error != null ->
            ErrorBanner(
                message = stringResource(R.string.event_detail_error),
                actionLabel = stringResource(R.string.events_retry),
                onAction = actions.onRetry,
            )
        else -> LoadingState()
    }
    SecondaryAction(stringResource(R.string.event_back), actions.onBack)
}

@Composable
private fun EventSummary(
    event: ClientEvent,
    actions: EventDetailActions,
    today: LocalDate,
) {
    event.eventType?.let { VirGoBadge(text = stringResource(eventTypeLabel(it)), tone = VirGoTone.VIOLET) }
    Text(
        event.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
    countdown(event, today)?.let {
        Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            SummaryRow(R.string.event_detail_date, event.date?.let(::formatDate).orEmpty())
            SummaryRow(R.string.event_detail_venue, event.venueName ?: stringResource(R.string.event_detail_no_venue))
            SummaryRow(R.string.event_detail_guests, event.guests.toString())
            SummaryRow(R.string.event_detail_hours, event.hours.toString())
            SummaryRow(R.string.event_detail_budget, formatMxn(event.budget.toDouble()), last = true)
        }
    }
    VirGoButton(onClick = actions.onEdit, text = stringResource(R.string.event_action_edit))
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            VirGoRow(
                title = stringResource(R.string.event_action_guests),
                subtitle = stringResource(R.string.event_action_guests_hint),
                icon = Icons.Outlined.Groups,
                standalone = false,
                onClick = actions.onGuests,
                subtitleMaxLines = Int.MAX_VALUE,
            )
            VirGoRowDivider()
            VirGoRow(
                title = stringResource(R.string.event_action_tasks),
                subtitle = stringResource(R.string.event_action_tasks_hint),
                icon = Icons.Outlined.Checklist,
                tone = VirGoTone.GREEN,
                standalone = false,
                onClick = actions.onTasks,
                subtitleMaxLines = Int.MAX_VALUE,
            )
        }
    }
}

@Composable
private fun countdown(
    event: ClientEvent,
    today: LocalDate,
): String? {
    val days = event.daysUntil(today) ?: return null
    return when {
        days < 0 -> stringResource(R.string.event_past)
        days == 0L -> stringResource(R.string.event_today)
        days == 1L -> stringResource(R.string.event_tomorrow)
        else -> stringResource(R.string.event_days_left, days.toInt())
    }
}

@Composable
private fun SummaryRow(
    label: Int,
    value: String,
    last: Boolean = false,
) {
    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
    if (!last) VirGoRowDivider()
}
