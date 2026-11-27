package mx.virgo.app.ui.ownertools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.BookingStatusBadge
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.finance.Stat
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.domain.MonthStats
import mx.virgo.core.domain.OwnerPayments
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OwnerInsights(
    insight: OwnerInsight,
    viewModel: OwnerInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    InsightsContent(insight, state, onMonths = viewModel::selectMonths, onRetry = viewModel::refresh)
}

@Composable
internal fun InsightsContent(
    insight: OwnerInsight,
    state: OwnerInsightsUiState,
    onMonths: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    VirGoPageBody {
        val payments = state.payments
        when {
            state.loading && payments == null -> LoadingState()
            state.failed ->
                ErrorBanner(
                    stringResource(R.string.tools_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = onRetry,
                )
            payments == null -> Unit
            insight == OwnerInsight.PAYMENTS -> PaymentsBody(payments)
            else -> StatsBody(state.months, state.stats, onMonths)
        }
    }
}

@Composable
private fun PaymentsBody(payments: OwnerPayments) {
    Text(stringResource(R.string.tools_payments_notice), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Stat(formatMxn(payments.received.toDouble()), stringResource(R.string.tools_received), Modifier.weight(1f))
        Stat(formatMxn(payments.pending.toDouble()), stringResource(R.string.tools_to_collect), Modifier.weight(1f))
    }
    if (payments.bookings.isEmpty()) {
        Text(stringResource(R.string.owner_home_next_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            payments.bookings.forEachIndexed { index, booking ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = booking.clientName.ifBlank { booking.venueName },
                    subtitle =
                        stringResource(
                            R.string.tools_payment_line,
                            booking.folio,
                            booking.date?.let(::formatDate).orEmpty(),
                            formatMxn((booking.deposit ?: java.math.BigDecimal.ZERO).toDouble()),
                        ),
                    standalone = false,
                    subtitleMaxLines = Int.MAX_VALUE,
                    trailing = { BookingStatusBadge(booking.status) },
                )
            }
        }
    }
}

@Composable
private fun StatsBody(
    months: Int,
    stats: List<MonthStats>,
    onMonths: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(SHORT_RANGE, LONG_RANGE).forEach { option ->
            FilterChip(
                selected = months == option,
                onClick = { onMonths(option) },
                label = { Text(stringResource(R.string.tools_months, option)) },
            )
        }
    }
    Text(stringResource(R.string.tools_visits_out), style = MaterialTheme.typography.bodySmall)
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            stats.forEachIndexed { index, month ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = month.month.format(MONTH).replaceFirstChar { it.titlecase(SPANISH) },
                    subtitle =
                        stringResource(
                            R.string.tools_stats_line,
                            month.requests,
                            month.bookings,
                            month.conversion,
                        ),
                    standalone = false,
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

private const val SHORT_RANGE = 3
private const val LONG_RANGE = 6
private val SPANISH = Locale.forLanguageTag("es-MX")
private val MONTH = DateTimeFormatter.ofPattern("MMMM yyyy", SPANISH)
