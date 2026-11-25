package mx.virgo.app.ui.venuedetail

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import mx.virgo.app.ui.beta.BetaCatalogScreen

/**
 * Explorar del cliente con sesión: catálogo y, al tocar «Ver detalle», el detalle del salón.
 * El salón abierto sobrevive a la rotación.
 */
@Composable
fun ClientExplore(
    catalog: @Composable ((String) -> Unit) -> Unit = { open -> BetaCatalogScreen(onOpenVenue = open) },
    detail: @Composable (String, () -> Unit) -> Unit = { id, back ->
        VenueDetailScreen(id, VenueDetailActions(onBack = back))
    },
) {
    var openVenue by rememberSaveable { mutableStateOf<String?>(null) }
    val venueId = openVenue
    if (venueId == null) {
        catalog { openVenue = it }
    } else {
        BackHandler { openVenue = null }
        detail(venueId) { openVenue = null }
    }
}
