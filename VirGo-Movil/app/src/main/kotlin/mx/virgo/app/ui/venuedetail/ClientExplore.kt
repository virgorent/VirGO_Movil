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
import mx.virgo.app.ui.quotes.QuoteRequestActions
import mx.virgo.app.ui.quotes.QuoteRequestScreen

/**
 * Explorar del cliente con sesión: catálogo → detalle (006) → cotizar (008) o reservar (009).
 * El paso abierto sobrevive a la rotación.
 */
@Composable
fun ClientExplore(
    onMyBookings: () -> Unit = {},
    onMyQuotes: () -> Unit = {},
    catalog: @Composable ((String) -> Unit) -> Unit = { open -> BetaCatalogScreen(onOpenVenue = open) },
    detail: @Composable (String, () -> Unit, ExploreStep) -> Unit = { id, back, step ->
        VenueDetailScreen(
            id,
            VenueDetailActions(onBack = back, onQuote = { step.quote() }, onBook = { step.book() }),
        )
    },
    quote: @Composable (String, () -> Unit) -> Unit = { id, back ->
        QuoteRequestScreen(id, QuoteRequestActions(onBack = back, onMyQuotes = onMyQuotes))
    },
    booking: @Composable (String, () -> Unit, () -> Unit) -> Unit = { id, back, toCatalog ->
        BookingConfirmScreen(
            id,
            BookingConfirmActions(onBack = back, onMyBookings = onMyBookings, onCatalog = toCatalog),
        )
    },
) {
    var openVenue by rememberSaveable { mutableStateOf<String?>(null) }
    var step by rememberSaveable { mutableStateOf<String?>(null) }
    val venueId = openVenue
    when {
        venueId == null -> catalog { openVenue = it }
        step == STEP_BOOK -> {
            BackHandler { step = null }
            booking(venueId, { step = null }) {
                step = null
                openVenue = null
            }
        }
        step == STEP_QUOTE -> {
            BackHandler { step = null }
            quote(venueId) { step = null }
        }
        else -> {
            BackHandler { openVenue = null }
            detail(venueId, { openVenue = null }, ExploreStep({ step = STEP_QUOTE }, { step = STEP_BOOK }))
        }
    }
}

/** Siguiente paso desde el detalle del salón. */
class ExploreStep(
    val quote: () -> Unit,
    val book: () -> Unit,
)

private const val STEP_QUOTE = "quote"
private const val STEP_BOOK = "book"
