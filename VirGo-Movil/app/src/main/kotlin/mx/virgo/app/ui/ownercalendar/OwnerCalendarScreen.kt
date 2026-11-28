package mx.virgo.app.ui.ownercalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.domain.CalendarDay
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun OwnerCalendar(
    onVenues: () -> Unit,
    viewModel: OwnerCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    OwnerCalendarContent(
        state,
        OwnerCalendarActions(
            onRetry = viewModel::refresh,
            onVenue = viewModel::selectVenue,
            onMonth = viewModel::changeMonth,
            onDay = viewModel::selectDay,
            onOpenBlock = viewModel::openBlock,
            onBlockForm = viewModel::updateBlock,
            onCancelBlock = { viewModel.updateBlock(null) },
            onConfirmBlock = viewModel::confirmBlock,
            onUnblock = viewModel::unblock,
            onVenues = onVenues,
        ),
        today = viewModel.today(),
    )
}

@Composable
internal fun OwnerCalendarContent(
    state: OwnerCalendarUiState,
    actions: OwnerCalendarActions,
    today: java.time.LocalDate,
) {
    VirGoPageBody {
        when {
            state.loading && state.venues.isEmpty() ->
                LoadingState(
                    message = stringResource(R.string.owner_calendar_loading),
                )
            state.failed ->
                ErrorBanner(
                    message = stringResource(R.string.owner_calendar_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = actions.onRetry,
                )
            state.venues.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.owner_calendar_empty_title),
                    text = stringResource(R.string.owner_calendar_empty_text),
                    iconVector = Icons.Outlined.Storefront,
                    action = {
                        VirGoButton(
                            onClick = actions.onVenues,
                            text = stringResource(R.string.owner_home_go_venues),
                        )
                    },
                )
            else -> CalendarBody(state, actions, today)
        }
    }
}

@Composable
private fun CalendarBody(
    state: OwnerCalendarUiState,
    actions: OwnerCalendarActions,
    today: java.time.LocalDate,
) {
    if (state.venues.size > 1) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.venues.forEach { venue ->
                FilterChip(
                    selected = venue.id == state.venueId,
                    onClick = { actions.onVenue(venue.id) },
                    label = { Text(venue.draft.name) },
                )
            }
        }
    }
    MonthHeader(state.month, actions.onMonth)
    if (state.daysFailed) {
        ErrorBanner(
            message = stringResource(R.string.owner_calendar_days_error),
            actionLabel = stringResource(R.string.booking_retry),
            onAction = { actions.onMonth(0) },
        )
    }
    MonthGrid(state.month, state.days, state.selected, actions.onDay, today)
    Legend()
    state.selectedDay?.let { DayDetail(state, it, actions, today) }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onMonth: (Long) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onMonth(-1) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, stringResource(R.string.owner_calendar_previous))
        }
        Text(
            month.format(MONTH_FORMAT).replaceFirstChar { it.titlecase(SPANISH) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        IconButton(onClick = { onMonth(1) }) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, stringResource(R.string.owner_calendar_next))
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    days: List<CalendarDay>,
    selected: java.time.LocalDate?,
    onDay: (java.time.LocalDate?) -> Unit,
    today: java.time.LocalDate,
) {
    val leading = month.atDay(1).dayOfWeek.value - 1
    val cells: List<CalendarDay?> = List(leading) { null } + days
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            DayOfWeek.entries.forEach { weekday ->
                Text(
                    weekday.getDisplayName(TextStyle.NARROW, SPANISH).uppercase(SPANISH),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        cells.chunked(WEEK).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    Box(Modifier.weight(1f)) {
                        if (day != null) DayCell(day, day.date == selected, onDay, today)
                    }
                }
                repeat(WEEK - week.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onDay: (java.time.LocalDate?) -> Unit,
    today: java.time.LocalDate,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) =
        when {
            day.booked -> colors.primary to colors.onPrimary
            day.block != null -> colors.surfaceVariant to colors.onSurfaceVariant
            day.date.isBefore(today) -> colors.surface to colors.onSurfaceVariant
            else -> colors.surface to colors.onSurface
        }
    val status = stringResource(dayStatus(day, today))
    val label = stringResource(R.string.owner_calendar_day_cd, day.date.format(DAY_FORMAT), status)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .heightIn(min = 40.dp)
            .clip(DayShape)
            .background(container)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.primary else colors.outlineVariant, DayShape)
            .clickable { onDay(if (isSelected) null else day.date) }
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Button
                selected = isSelected
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day.date.dayOfMonth.toString(), color = content, fontWeight = FontWeight.SemiBold)
            if (day.block != null && !day.booked) {
                Text(
                    stringResource(R.string.owner_calendar_blocked_short),
                    color = content,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LegendItem(MaterialTheme.colorScheme.primary, stringResource(R.string.owner_calendar_status_booked))
        LegendItem(MaterialTheme.colorScheme.surfaceVariant, stringResource(R.string.owner_calendar_status_blocked))
        LegendItem(MaterialTheme.colorScheme.surface, stringResource(R.string.owner_calendar_status_free))
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(12.dp)
                .clip(DayShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, DayShape),
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

/** Un día pasado sin reserva ni bloqueo ya no está «Libre»: no se puede reservar. */
internal fun dayStatus(
    day: CalendarDay,
    today: java.time.LocalDate,
): Int =
    when {
        day.booked -> R.string.owner_calendar_status_booked
        day.block != null -> R.string.owner_calendar_status_blocked
        day.date.isBefore(today) -> R.string.owner_calendar_status_past
        else -> R.string.owner_calendar_status_free
    }

private const val WEEK = 7
private val SPANISH = Locale.forLanguageTag("es-MX")
private val MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", SPANISH)
internal val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", SPANISH)
private val DayShape = RoundedCornerShape(12.dp)
