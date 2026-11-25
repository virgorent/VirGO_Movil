package mx.virgo.app.ui.venuedetail

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import mx.virgo.app.ui.beta.BetaCatalogScreen
import mx.virgo.app.ui.bookings.BookingConfirmActions
import mx.virgo.app.ui.bookings.BookingConfirmScreen

/**
 * Explorar del cliente con sesión: catálogo → detalle (006) → reservar (009). El paso
 * abierto sobrevive a la rotación.
 */
@Composable
fun ClientExplore(
    onMyBookings: () -> Unit = {},
    catalog: @Composable ((String) -> Unit) -> Unit = { open -> BetaCatalogScreen(onOpenVenue = open) },
    detail: @Composable (String, () -> Unit, (String) -> Unit) -> Unit = { id, back, book ->
        VenueDetailScreen(id, VenueDetailActions(onBack = back, onBook = book))
    },
    booking: @Composable (String, () -> Unit, () -> Unit) -> Unit = { id, back, toCatalog ->
        BookingConfirmScreen(
            id,
            BookingConfirmActions(onBack = back, onMyBookings = onMyBookings, onCatalog = toCatalog),
        )
    },
) {
    var openVenue by rememberSaveable { mutableStateOf<String?>(null) }
    var booking by rememberSaveable { mutableStateOf(false) }
    val venueId = openVenue
    when {
        venueId == null -> catalog { openVenue = it }
        booking -> {
            BackHandler { booking = false }
            booking(venueId, { booking = false }) {
                booking = false
                openVenue = null
            }
        }
        else -> {
            BackHandler { openVenue = null }
            detail(venueId, { openVenue = null }) { booking = true }
        }
    }
}
