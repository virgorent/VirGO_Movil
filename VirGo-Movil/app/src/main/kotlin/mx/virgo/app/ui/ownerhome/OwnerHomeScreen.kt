package mx.virgo.app.ui.ownerhome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.BookingStatusBadge
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
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.OwnerDashboard

@Composable
fun OwnerHome(
    name: String,
    actions: OwnerHomeActions,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    OwnerHomeContent(state, name, actions, onRetry = viewModel::refresh)
}

@Composable
internal fun OwnerHomeContent(
    state: OwnerHomeUiState,
    name: String,
    actions: OwnerHomeActions,
    onRetry: () -> Unit,
) {
    VirGoPageBody {
        Text(
            stringResource(R.string.owner_home_greeting, name.substringBefore(' ')),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        val dashboard = state.dashboard
        when {
            state.loading && dashboard == null -> LoadingState(message = stringResource(R.string.owner_home_loading))
            state.failed ->
                ErrorBanner(
                    message = stringResource(R.string.owner_home_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = onRetry,
                )
            dashboard == null -> Unit
            dashboard.registeredVenues == 0 ->
                EmptyState(
                    title = stringResource(R.string.owner_home_empty_title),
                    text = stringResource(R.string.owner_home_empty_text),
                    iconVector = Icons.Outlined.Storefront,
                    action = {
                        VirGoButton(
                            onClick = actions.onVenues,
                            text = stringResource(R.string.owner_venues_register),
                        )
                    },
                )
            else -> DashboardBody(dashboard, actions)
        }
    }
}

@Composable
private fun DashboardBody(
    dashboard: OwnerDashboard,
    actions: OwnerHomeActions,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Metric(
            value =
                stringResource(
                    R.string.owner_home_venues_value,
                    dashboard.publishedVenues,
                    dashboard.registeredVenues,
                ),
            label = stringResource(R.string.owner_home_venues_label),
            modifier = Modifier.weight(1f),
        )
        Metric(
            dashboard.pendingRequests.toString(),
            stringResource(R.string.owner_home_requests_label),
            Modifier.weight(1f),
        )
        Metric(
            dashboard.upcomingBookings.toString(),
            stringResource(R.string.owner_home_bookings_label),
            Modifier.weight(1f),
        )
    }
    VirGoSection(kicker = stringResource(R.string.owner_home_next_title)) {
        if (dashboard.nextBookings.isEmpty()) {
            Text(stringResource(R.string.owner_home_next_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            NextBookings(dashboard.nextBookings, actions.onCalendar)
        }
    }
    VirGoSection(kicker = stringResource(R.string.owner_home_shortcuts)) {
        VirGoRow(
            stringResource(R.string.owner_home_go_venues),
            icon = Icons.Outlined.Storefront,
            onClick = actions.onVenues,
        )
        VirGoRow(
            stringResource(R.string.owner_home_go_requests),
            icon = Icons.Outlined.Inbox,
            tone = VirGoTone.AMBER,
            onClick = actions.onRequests,
        )
        VirGoRow(
            stringResource(R.string.owner_home_go_calendar),
            icon = Icons.Outlined.CalendarMonth,
            tone = VirGoTone.GREEN,
            onClick = actions.onCalendar,
        )
    }
}

@Composable
private fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    VirGoCard(modifier.clearAndSetSemantics { contentDescription = "$value $label" }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NextBookings(
    bookings: List<Booking>,
    onOpen: () -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            bookings.forEachIndexed { index, booking ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = booking.venueName,
                    subtitle =
                        stringResource(
                            R.string.bookings_item,
                            booking.folio,
                            booking.date?.let(::formatDate).orEmpty(),
                        ),
                    icon = Icons.Outlined.EventAvailable,
                    standalone = false,
                    onClick = onOpen,
                    trailing = { BookingStatusBadge(booking.status) },
                )
            }
        }
    }
}
