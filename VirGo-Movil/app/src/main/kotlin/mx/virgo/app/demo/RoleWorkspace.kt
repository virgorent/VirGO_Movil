package mx.virgo.app.demo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R
import mx.virgo.app.ui.account.AccountProfileScreen
import mx.virgo.app.ui.account.SessionIdentity
import mx.virgo.app.ui.beta.BetaSectionScreen
import mx.virgo.app.ui.beta.BetaSectionsMenu
import mx.virgo.app.ui.beta.betaTitle
import mx.virgo.app.ui.bookings.ClientBookings
import mx.virgo.app.ui.events.ClientEvents
import mx.virgo.app.ui.events.EventStep
import mx.virgo.app.ui.ownerrequests.OwnerRequests
import mx.virgo.app.ui.ownervenues.OwnerVenuesRoute
import mx.virgo.app.ui.quotes.ClientQuotes
import mx.virgo.app.ui.venuedetail.ClientExplore
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.theme.pageBackground

/** The beta uses only the authenticated identity and never grants a selectable role. */
@Composable
fun RoleWorkspace(
    role: DemoRole,
    accountKey: String,
    onLogout: () -> Unit,
    accountName: String = "",
    accountEmail: String = "",
    accountScreen: @Composable (SessionIdentity, () -> Unit) -> Unit = { identity, logout ->
        AccountProfileScreen(identity, logout)
    },
    ownerVenuesScreen: @Composable () -> Unit = { OwnerVenuesRoute() },
) {
    key(accountKey, role) {
        BetaWorkspace(role, accountName, accountEmail, onLogout, accountScreen, ownerVenuesScreen)
    }
}

@Composable
private fun BetaWorkspace(
    role: DemoRole,
    accountName: String,
    accountEmail: String,
    onLogout: () -> Unit,
    accountScreen: @Composable (SessionIdentity, () -> Unit) -> Unit,
    ownerVenuesScreen: @Composable () -> Unit,
) {
    var history by rememberSaveable(role) { mutableStateOf(role.home) }
    var sections by rememberSaveable { mutableStateOf(false) }
    val route = history.substringAfterLast("|")
    val navigate: (String) -> Unit = {
        check(DemoDestinations.allows(role, it))
        history = pushDemoRoute(history, it)
        sections = false
    }
    val back: () -> Unit = {
        if (sections) sections = false else history = history.substringBeforeLast("|", role.home)
    }
    BackHandler(enabled = sections || history.contains("|"), onBack = back)
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            // El catálogo y las cotizaciones tienen su propio encabezado (035 RF-08); el resto usa el del espacio.
            if (sections || !(role == DemoRole.CLIENT && route in OWN_HEADER_ROUTES)) {
                VirGoPageHeader(
                    title = stringResource(if (sections) R.string.beta_sections else betaTitle(role, route)),
                    kicker = stringResource(role.label),
                    onBack = if (sections || history.contains("|")) back else null,
                    backContentDescription = stringResource(R.string.demo_back),
                )
            }
        },
        bottomBar = {
            DemoBottomNavigation(
                role,
                route,
                navigate,
                onIndex = { sections = !sections },
                systemInsets = false,
                indexLabel = R.string.beta_sections,
            )
        },
    ) { padding ->
        Surface(
            Modifier.padding(padding),
            color = pageBackground(),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            WorkspaceBody(
                screen = if (sections) WorkspaceScreen.SECTIONS else workspaceScreen(role, route),
                role = role,
                route = route,
                navigate = navigate,
                identity = SessionIdentity(accountName, accountEmail, role.label),
                onLogout = onLogout,
                accountScreen = accountScreen,
                ownerVenuesScreen = ownerVenuesScreen,
                back = back,
            )
        }
    }
}

/** «Mi Evento», «Invitados» y «Pendientes» abren el evento real; las dos últimas, ese paso (014 RF-04). */
private val EVENT_STEPS = mapOf("event" to null, "guests" to EventStep.GUESTS, "tasks" to EventStep.TASKS)
private val EVENT_ROUTES = EVENT_STEPS.keys

/** Pantallas del cliente que dibujan su propio encabezado con pasos internos. */
private val OWN_HEADER_ROUTES = setOf("explore", "quotes") + EVENT_ROUTES

/** Inicio y «Mis salones» del propietario muestran sus salones reales (022 RF-01). */
private val OWNER_VENUE_ROUTES = setOf("ownerHome", "ownerVenues")

private enum class WorkspaceScreen {
    SECTIONS,
    CATALOG,
    BOOKINGS,
    QUOTES,
    EVENTS,
    OWNER_VENUES,
    OWNER_REQUESTS,
    ACCOUNT,
    SECTION,
}

@Composable
private fun WorkspaceBody(
    screen: WorkspaceScreen,
    role: DemoRole,
    route: String,
    navigate: (String) -> Unit,
    identity: SessionIdentity,
    onLogout: () -> Unit,
    accountScreen: @Composable (SessionIdentity, () -> Unit) -> Unit,
    ownerVenuesScreen: @Composable () -> Unit,
    back: () -> Unit,
) {
    when (screen) {
        WorkspaceScreen.SECTIONS -> BetaSectionsMenu(role, navigate)
        WorkspaceScreen.CATALOG ->
            ClientExplore(onMyBookings = { navigate("bookings") }, onMyQuotes = { navigate("quotes") })
        WorkspaceScreen.BOOKINGS -> ClientBookings()
        WorkspaceScreen.EVENTS -> ClientEvents(EVENT_STEPS[route], betaTitle(role, route), onBack = back)
        WorkspaceScreen.QUOTES ->
            ClientQuotes(onCatalog = { navigate("explore") }, onMyBookings = { navigate("bookings") }, onBack = back)
        WorkspaceScreen.OWNER_VENUES -> ownerVenuesScreen()
        WorkspaceScreen.OWNER_REQUESTS -> OwnerRequests()
        WorkspaceScreen.ACCOUNT -> accountScreen(identity, onLogout)
        WorkspaceScreen.SECTION -> BetaSectionScreen(role, route, identity.name, identity.email)
    }
}

/** Rutas con servicio conectado por rol; el resto es la sección «Próximamente». */
private val CONNECTED_SCREENS: Map<DemoRole, Map<String, WorkspaceScreen>> =
    mapOf(
        DemoRole.CLIENT to
            mapOf(
                "explore" to WorkspaceScreen.CATALOG,
                "bookings" to WorkspaceScreen.BOOKINGS,
                "quotes" to WorkspaceScreen.QUOTES,
            ) + EVENT_ROUTES.associateWith { WorkspaceScreen.EVENTS },
        DemoRole.OWNER to
            OWNER_VENUE_ROUTES.associateWith { WorkspaceScreen.OWNER_VENUES } +
            ("ownerRequests" to WorkspaceScreen.OWNER_REQUESTS),
    )

private fun workspaceScreen(
    role: DemoRole,
    route: String,
): WorkspaceScreen =
    CONNECTED_SCREENS[role]?.get(route)
        ?: if (route == "profile") WorkspaceScreen.ACCOUNT else WorkspaceScreen.SECTION
