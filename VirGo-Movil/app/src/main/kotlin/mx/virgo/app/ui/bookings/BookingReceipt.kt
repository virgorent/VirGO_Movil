package mx.virgo.app.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
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
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.BookingStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** C23: datos del servidor; repite que no se cobró y que la fecha no está apartada (009 RF-06/RF-07). */
@Composable
internal fun BookingReceiptBody(
    booking: Booking,
    onMyBookings: (() -> Unit)?,
    onCatalog: (() -> Unit)?,
) {
    Text(
        text = stringResource(R.string.booking_receipt_folio, booking.folio),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
    BookingStatusBadge(booking.status)
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            ReceiptRow(R.string.booking_receipt_venue, booking.venueName)
            ReceiptRow(R.string.booking_receipt_date, booking.date?.let(::formatDate).orEmpty())
            ReceiptRow(R.string.booking_receipt_hours, stringResource(R.string.booking_hours_value, booking.hours))
            ReceiptRow(R.string.booking_receipt_total, amount(booking.total))
            ReceiptRow(R.string.booking_receipt_deposit, amount(booking.deposit), last = true)
        }
    }
    VirGoCard {
        VirGoRow(
            title = stringResource(R.string.booking_receipt_notice),
            icon = Icons.Outlined.Info,
            tone = VirGoTone.AMBER,
            standalone = false,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        onMyBookings?.let { VirGoButton(onClick = it, text = stringResource(R.string.booking_receipt_my_bookings)) }
        onCatalog?.let {
            VirGoOutlinedButton(onClick = it, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.booking_receipt_catalog))
            }
        }
    }
}

@Composable
internal fun BookingStatusBadge(status: BookingStatus) {
    val (text, tone) =
        when (status) {
            BookingStatus.PENDING_PAYMENT -> R.string.booking_status_pending to VirGoTone.AMBER
            BookingStatus.CONFIRMED -> R.string.booking_status_confirmed to VirGoTone.GREEN
            BookingStatus.CANCELLED -> R.string.booking_status_cancelled to VirGoTone.RED
            BookingStatus.UNKNOWN -> R.string.booking_status_unknown to VirGoTone.GRAY
        }
    VirGoBadge(text = stringResource(text), tone = tone)
}

/** Etiqueta arriba y valor completo abajo: sin cortes a 320 dp/fuente 2.0. */
@Composable
private fun ReceiptRow(
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

@Composable
private fun amount(value: BigDecimal?): String =
    value?.let { formatMxn(it.toDouble()) } ?: stringResource(R.string.booking_unknown_amount)

/** Fecha en formato local (009 RF-10). */
internal fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
