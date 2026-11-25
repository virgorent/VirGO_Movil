package mx.virgo.app.ui.quotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
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
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteStatus

/** C7: datos de la solicitud y, si el salón respondió, sus montos tal cual (008 RF-10/RF-11). */
@Composable
internal fun QuoteDetailBody(
    quote: Quote,
    onBook: (Quote) -> Unit,
) {
    Text(
        text = quote.venueName,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
    QuoteStatusBadge(quote.status)
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            DetailRow(R.string.quote_detail_type, quote.eventType?.let { stringResource(eventTypeLabel(it)) }.orEmpty())
            DetailRow(R.string.quote_detail_date, quote.eventDate?.let(::formatDate).orEmpty())
            DetailRow(
                R.string.quote_detail_guests,
                stringResource(R.string.quote_guests_value, quote.guests),
                last = quote.message == null,
            )
            quote.message?.let { DetailRow(R.string.quote_detail_message, it, last = true) }
        }
    }
    val total = quote.total
    if (total == null) {
        VirGoCard {
            VirGoRow(
                title = stringResource(R.string.quote_detail_waiting),
                icon = Icons.Outlined.HourglassEmpty,
                tone = VirGoTone.AMBER,
                standalone = false,
            )
        }
    } else {
        VirGoCard {
            Column(Modifier.padding(horizontal = 16.dp)) {
                DetailRow(R.string.quote_detail_total, formatMxn(total.toDouble()))
                quote.deposit?.let { DetailRow(R.string.quote_detail_deposit, formatMxn(it.toDouble())) }
                DetailRow(R.string.quote_detail_answer, quote.responseMessage.orEmpty(), last = true)
            }
        }
    }
    if (quote.canBook) VirGoButton(onClick = { onBook(quote) }, text = stringResource(R.string.quote_detail_book))
}

@Composable
internal fun QuoteStatusBadge(status: QuoteStatus) {
    val (text, tone) =
        when (status) {
            QuoteStatus.SENT -> R.string.quote_status_sent to VirGoTone.VIOLET
            QuoteStatus.ANSWERED -> R.string.quote_status_answered to VirGoTone.GREEN
            QuoteStatus.ACCEPTED -> R.string.quote_status_accepted to VirGoTone.GREEN
            QuoteStatus.REJECTED -> R.string.quote_status_rejected to VirGoTone.RED
            QuoteStatus.EXPIRED -> R.string.quote_status_expired to VirGoTone.GRAY
            QuoteStatus.UNKNOWN -> R.string.quote_status_unknown to VirGoTone.GRAY
        }
    VirGoBadge(text = stringResource(text), tone = tone)
}

/** Etiqueta arriba y valor completo abajo: sin cortes a 320 dp/fuente 2.0. */
@Composable
private fun DetailRow(
    label: Int,
    value: String,
    last: Boolean = false,
) {
    if (value.isEmpty()) return
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
