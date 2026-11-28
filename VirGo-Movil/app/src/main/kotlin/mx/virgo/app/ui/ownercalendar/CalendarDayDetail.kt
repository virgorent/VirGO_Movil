package mx.virgo.app.ui.ownercalendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.BookingStatusBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.BlockReason
import mx.virgo.core.domain.CalendarDay
import mx.virgo.core.domain.OwnerCalendar
import java.time.LocalDate

/** `023` RF-07–RF-11: reservaciones del día, empalme, bloqueo y sus acciones. */
@Composable
internal fun DayDetail(
    state: OwnerCalendarUiState,
    day: CalendarDay,
    actions: OwnerCalendarActions,
    today: LocalDate,
) {
    Text(
        day.date.format(DAY_FORMAT),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
    if (day.overlapping) {
        VirGoCard {
            VirGoRow(
                stringResource(R.string.owner_calendar_overlap),
                icon = Icons.Outlined.WarningAmber,
                tone = VirGoTone.AMBER,
                standalone = false,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
    if (day.booked) DayBookings(day)
    BlockSection(state, day, actions, today)
}

@Composable
private fun BlockSection(
    state: OwnerCalendarUiState,
    day: CalendarDay,
    actions: OwnerCalendarActions,
    today: LocalDate,
) {
    day.block?.let { block ->
        VirGoCard {
            VirGoRow(
                title =
                    stringResource(
                        R.string.owner_calendar_blocked_title,
                        stringResource(reasonLabel(block.reason)),
                    ),
                subtitle = block.note,
                icon = Icons.Outlined.Block,
                tone = VirGoTone.GRAY,
                standalone = false,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        if (block.blockId != null) {
            VirGoOutlinedButton(
                onClick = actions.onUnblock,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.owner_calendar_unblock))
            }
        }
    }
    val form = state.blockForm
    when {
        form != null -> BlockFormCard(form, state.saving, actions)
        day.free && !day.date.isBefore(today) ->
            VirGoOutlinedButton(onClick = actions.onOpenBlock, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.owner_calendar_block))
            }
        day.free ->
            Text(
                stringResource(R.string.owner_calendar_free_past),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
    state.blockError?.let { error ->
        Text(
            stringResource(blockErrorText(error)),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun DayBookings(day: CalendarDay) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            day.bookings.forEachIndexed { index, booking ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = booking.clientName.ifBlank { booking.folio },
                    subtitle = stringResource(R.string.owner_calendar_booking_line, booking.folio, booking.hours),
                    icon = Icons.Outlined.EventAvailable,
                    standalone = false,
                    trailing = { BookingStatusBadge(booking.status) },
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

@Composable
private fun BlockFormCard(
    form: BlockForm,
    saving: Boolean,
    actions: OwnerCalendarActions,
) {
    VirGoCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.owner_calendar_block_reason), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BlockReason.entries.forEach { reason ->
                    FilterChip(
                        selected = form.reason == reason,
                        onClick = { actions.onBlockForm(form.copy(reason = reason)) },
                        label = { Text(stringResource(reasonLabel(reason))) },
                    )
                }
            }
            VirGoTextField(
                value = form.note,
                onValueChange = { actions.onBlockForm(form.copy(note = it)) },
                placeholder = stringResource(R.string.owner_calendar_block_note, OwnerCalendar.NOTE_MAX),
                singleLine = false,
            )
            VirGoButton(
                onClick = actions.onConfirmBlock,
                enabled = form.reason != null && !saving,
                text = stringResource(R.string.owner_calendar_block_confirm),
                modifier = Modifier.fillMaxWidth(),
            )
            VirGoOutlinedButton(
                onClick = actions.onCancelBlock,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.owner_calendar_block_cancel))
            }
        }
    }
}

internal fun reasonLabel(reason: BlockReason?): Int =
    when (reason) {
        BlockReason.MAINTENANCE -> R.string.owner_calendar_reason_maintenance
        BlockReason.OWN_EVENT -> R.string.owner_calendar_reason_own_event
        BlockReason.OTHER, null -> R.string.owner_calendar_reason_other
    }

private fun blockErrorText(error: BlockError): Int =
    when (error) {
        BlockError.NOT_FREE -> R.string.owner_calendar_error_not_free
        BlockError.INVALID -> R.string.owner_calendar_error_invalid
        BlockError.GONE -> R.string.owner_calendar_error_gone
        BlockError.FAILED -> R.string.owner_calendar_error_failed
    }
