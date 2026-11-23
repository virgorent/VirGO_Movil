package mx.virgo.app.demo

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R

@Composable
internal fun clientText(
    @StringRes id: Int,
): String = stringResource(id)

@Composable
fun ClientDemoScreen(
    screen: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    var favorite by rememberSaveable { mutableStateOf(true) }
    var paid by rememberSaveable { mutableStateOf(false) }
    when (screen) {
        in clientCatalogRoutes -> ClientDemoCatalog(screen, favorite, { favorite = !favorite }, onNavigate)
        in clientPurchaseRoutes -> ClientDemoPurchase(screen, paid, { paid = true }, onNavigate, onNotice)
        in clientPlanningRoutes -> ClientDemoPlanning(screen, onNavigate, onNotice)
        in clientSocialRoutes -> ClientDemoSocial(screen, onNavigate, onNotice)
        else -> error(screen)
    }
}

private val clientSocialRoutes =
    setOf(
        "reminders",
        "share",
        "invitation",
        "venueReviews",
        "review",
        "assistant",
    )
private val clientCatalogRoutes =
    setOf(
        "explore",
        "favorites",
        "venue",
        "providers",
        "provider",
        "compare",
        "recommended",
    )
private val clientPurchaseRoutes =
    setOf(
        "quoteNew",
        "quotes",
        "quote",
        "booking",
        "payment",
        "receipt",
        "bookings",
        "manageBooking",
    )
private val clientPlanningRoutes =
    setOf(
        "event",
        "studio",
        "budget",
        "budgetAI",
        "payments",
        "guests",
        "tasks",
        "timeline",
    )

@Composable
internal fun ClientDemoDisclaimer() {
    DemoNotice(clientText(R.string.demo_client_disclaimer))
}

internal val ClientDemoTouchTarget = 48.dp

@Composable
internal fun ClientDemoLink(
    @StringRes label: Int,
    route: String,
    onNavigate: (String) -> Unit,
) {
    DemoAction(clientText(label)) { onNavigate(route) }
}
