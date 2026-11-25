package mx.virgo.app.ui.venuedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.ownervenues.venueCategoryLabel
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.VenueDetail
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Contenido de B13 en el orden del plan: identidad, precio, descripción, datos y acciones. */
@Composable
internal fun VenueDetailBody(
    venue: VenueDetail,
    viewer: DetailViewer,
    actions: VenueDetailActions,
) {
    if (venue.verified) VirGoBadge(text = stringResource(R.string.venue_detail_verified), tone = VirGoTone.GREEN)
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            VirGoRow(
                title = ratingText(venue),
                icon = Icons.Outlined.Star,
                tone = if (venue.ratingCount > 0) VirGoTone.AMBER else VirGoTone.GRAY,
                standalone = false,
            )
            VirGoRowDivider()
            VirGoRow(
                title = stringResource(R.string.venue_detail_price),
                subtitle = priceText(venue, viewer),
                icon = Icons.Outlined.Payments,
                tone = VirGoTone.VIOLET,
                standalone = false,
            )
        }
    }
    venue.description?.let { description ->
        VirGoSection(title = stringResource(R.string.venue_detail_about)) {
            Text(description, style = MaterialTheme.typography.bodyLarge)
        }
    }
    InfoSection(venue)
    TagSection(R.string.venue_detail_categories, venue.categories.map { venueCategoryLabel(it) })
    TagSection(R.string.venue_detail_amenities, venue.amenities)
    DetailActions(venue.id, viewer, actions)
}

@Composable
private fun InfoSection(venue: VenueDetail) {
    val rows =
        buildList {
            venue.zone?.let { add(Triple(Icons.Outlined.Place, R.string.venue_detail_zone, it)) }
            venue.address?.let { add(Triple(Icons.Outlined.Storefront, R.string.venue_detail_address, it)) }
        }
    val capacity = venue.capacity.takeIf { it > 0 }
    val minimumHours = venue.minimumHours?.takeIf { it > 0 }
    if (rows.isEmpty() && capacity == null && minimumHours == null) return
    VirGoSection(title = stringResource(R.string.venue_detail_info)) {
        VirGoCard {
            Column(Modifier.padding(horizontal = 16.dp)) {
                rows.forEach { (icon, label, value) ->
                    // Valor completo: una dirección larga no se corta (VirGoRow limita el subtítulo).
                    VirGoRow(title = stringResource(label), icon = icon, standalone = false)
                    Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 12.dp))
                    VirGoRowDivider()
                }
                capacity?.let {
                    VirGoRow(
                        stringResource(R.string.venue_detail_capacity, it),
                        icon = Icons.Outlined.Groups,
                        standalone = false,
                    )
                }
                minimumHours?.let {
                    VirGoRow(
                        stringResource(R.string.venue_detail_min_hours, it),
                        icon = Icons.Outlined.Schedule,
                        standalone = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun TagSection(
    title: Int,
    values: List<String>,
) {
    if (values.isEmpty()) return
    VirGoSection(title = stringResource(title)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { VirGoBadge(text = it, tone = VirGoTone.GRAY) }
        }
    }
}

@Composable
private fun ratingText(venue: VenueDetail): String =
    if (venue.ratingCount > 0) {
        stringResource(R.string.venue_detail_rating, "%.1f".format(venue.ratingAverage), venue.ratingCount)
    } else {
        stringResource(R.string.venue_detail_no_reviews)
    }

/** Sin sesión nunca se muestra precio, aunque llegara (006 ADR-02). */
@Composable
internal fun priceText(
    venue: VenueDetail,
    viewer: DetailViewer,
): String {
    val amount = venue.priceBase
    val unit = venue.priceUnit
    return when {
        viewer == DetailViewer.GUEST -> stringResource(R.string.venue_detail_price_login)
        amount == null || unit == null -> stringResource(R.string.venue_detail_price_quote)
        unit == PriceUnit.DAY -> stringResource(R.string.venue_detail_price_day, formatMxn(amount))
        else -> stringResource(R.string.venue_detail_price_hour, formatMxn(amount))
    }
}

/** Importe en MXN con la configuración regional del dispositivo (006 RF-12). */
internal fun formatMxn(
    amount: Double,
    locale: Locale = Locale.getDefault(),
): String =
    NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            currency = Currency.getInstance("MXN")
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            minimumFractionDigits = maximumFractionDigits
        }.format(amount)
