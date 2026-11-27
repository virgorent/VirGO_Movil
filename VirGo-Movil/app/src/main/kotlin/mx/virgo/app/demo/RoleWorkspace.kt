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
import mx.virgo.app.ui.assistant.ClientAssistant
import mx.virgo.app.ui.beta.BetaSectionScreen
import mx.virgo.app.ui.beta.BetaSectionsMenu
import mx.virgo.app.ui.beta.betaTitle
import mx.virgo.app.ui.bookings.ClientBookings
import mx.virgo.app.ui.events.ClientEvents
import mx.virgo.app.ui.events.EventStep
import mx.virgo.app.ui.favorites.ClientFavorites
import mx.virgo.app.ui.finance.ClientBudget
import mx.virgo.app.ui.finance.ClientBudgetSuggestion
import mx.virgo.app.ui.finance.ClientPayments
import mx.virgo.app.ui.finance.FinanceLinks
import mx.virgo.app.ui.messages.MessagesInbox
import mx.virgo.app.ui.ownercalendar.OwnerCalendar
import mx.virgo.app.ui.ownerhome.OwnerHome
import mx.virgo.app.ui.ownerhome.OwnerHomeActions
import mx.virgo.app.ui.ownerrequests.OwnerRequests
import mx.virgo.app.ui.ownertools.OwnerInsight
import mx.virgo.app.ui.ownertools.OwnerInsights
import mx.virgo.app.ui.ownertools.OwnerReviews
import mx.virgo.app.ui.ownertools.OwnerVenueTool
import mx.virgo.app.ui.ownertools.ReviewForm
import mx.virgo.app.ui.ownertools.VenueTool
import mx.virgo.app.ui.ownervenues.OwnerVenuesRoute
import mx.virgo.app.ui.quotes.ClientQuotes
import mx.virgo.app.ui.studio.ClientStudio
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
    ownerHomeScreen: @Composable (String, OwnerHomeActions) -> Unit = { name, actions -> OwnerHome(name, actions) },
) {
    key(accountKey, role) {
        BetaWorkspace(
            role,
            accountName,
            accountEmail,
            onLogout,
            accountScreen,
            OwnerSlots(ownerVenuesScreen, ownerHomeScreen),
        )
    }
}

@Composable
private fun BetaWorkspace(
    role: DemoRole,
    accountName: String,
    accountEmail: String,
    onLogout: () -> Unit,
    accountScreen: @Composable (SessionIdentity, () -> Unit) -> Unit,
    owner: OwnerSlots,
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
                owner = owner,
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

/** «Mis salones» del propietario muestra sus salones reales (022 RF-01); «Inicio» es su tablero (023). */
private val OWNER_VENUE_ROUTES = setOf("ownerVenues")

private enum class WorkspaceScreen {
    SECTIONS,
    CATALOG,
    BOOKINGS,
    QUOTES,
    EVENTS,
    STUDIO,
    FAVORITES,
    MESSAGES,
    OWNER_MESSAGES,
    OWNER_PRICING,
    OWNER_SETUPS,
    OWNER_PAYMENTS,
    OWNER_STATS,
    OWNER_REVIEWS,
    BUDGET,
    BUDGET_SUGGESTION,
    PAYMENTS,
    ASSISTANT,
    OWNER_HOME,
    OWNER_CALENDAR,
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
    owner: OwnerSlots,
    back: () -> Unit,
) {
    when (screen) {
        WorkspaceScreen.SECTIONS -> BetaSectionsMenu(role, navigate)
        WorkspaceScreen.ACCOUNT -> accountScreen(identity, onLogout)
        WorkspaceScreen.SECTION -> BetaSectionScreen(role, route, identity.name, identity.email)
        in OWNER_SCREENS -> OwnerScreen(screen, identity.name, navigate, owner)
        else -> ClientScreen(screen, betaTitle(role, route), route, navigate, back)
    }
}

private val OWNER_SCREENS =
    setOf(
        WorkspaceScreen.OWNER_HOME,
        WorkspaceScreen.OWNER_CALENDAR,
        WorkspaceScreen.OWNER_VENUES,
        WorkspaceScreen.OWNER_REQUESTS,
        WorkspaceScreen.OWNER_MESSAGES,
        WorkspaceScreen.OWNER_PRICING,
        WorkspaceScreen.OWNER_SETUPS,
        WorkspaceScreen.OWNER_PAYMENTS,
        WorkspaceScreen.OWNER_STATS,
        WorkspaceScreen.OWNER_REVIEWS,
    )

/** Pantallas conectadas del cliente (`005`–`021`). */
@Composable
private fun ClientScreen(
    screen: WorkspaceScreen,
    title: Int,
    route: String,
    navigate: (String) -> Unit,
    back: () -> Unit,
) {
    val toEvent = { navigate("event") }
    when (screen) {
        WorkspaceScreen.CATALOG ->
            ClientExplore(onMyBookings = { navigate("bookings") }, onMyQuotes = { navigate("quotes") })
        WorkspaceScreen.BOOKINGS ->
            ClientBookings(reviewSlot = { booking ->
                ReviewForm(booking.venueId.orEmpty(), booking.id)
            })
        WorkspaceScreen.STUDIO -> ClientStudio(onAssistant = { navigate("assistant") })
        WorkspaceScreen.ASSISTANT -> ClientAssistant()
        WorkspaceScreen.FAVORITES -> ClientFavorites()
        WorkspaceScreen.MESSAGES -> MessagesInbox()
        WorkspaceScreen.BUDGET ->
            ClientBudget(
                FinanceLinks(toEvent, onSuggestion = { navigate("budgetAI") }, onPayments = { navigate("payments") }),
            )
        WorkspaceScreen.BUDGET_SUGGESTION -> ClientBudgetSuggestion(onEvent = toEvent)
        WorkspaceScreen.PAYMENTS -> ClientPayments(onEvent = toEvent)
        WorkspaceScreen.EVENTS -> ClientEvents(EVENT_STEPS[route], title, onBack = back)
        WorkspaceScreen.QUOTES ->
            ClientQuotes(onCatalog = { navigate("explore") }, onMyBookings = { navigate("bookings") }, onBack = back)
        else -> Unit
    }
}

/** Pantallas conectadas del propietario (`022`–`025`). */
@Composable
private fun OwnerScreen(
    screen: WorkspaceScreen,
    name: String,
    navigate: (String) -> Unit,
    owner: OwnerSlots,
) {
    when (screen) {
        WorkspaceScreen.OWNER_HOME ->
            owner.home(
                name,
                OwnerHomeActions(
                    onVenues = { navigate("ownerVenues") },
                    onRequests = { navigate("ownerRequests") },
                    onCalendar = { navigate("ownerCalendar") },
                ),
            )
        WorkspaceScreen.OWNER_CALENDAR -> OwnerCalendar(onVenues = { navigate("ownerVenues") })
        WorkspaceScreen.OWNER_VENUES -> owner.venues()
        WorkspaceScreen.OWNER_REQUESTS -> OwnerRequests()
        WorkspaceScreen.OWNER_MESSAGES -> MessagesInbox()
        else -> OwnerToolScreen(screen)
    }
}

/** Pantallas del propietario que las pruebas sin Hilt sustituyen. */
private class OwnerSlots(
    val venues: @Composable () -> Unit,
    val home: @Composable (String, OwnerHomeActions) -> Unit,
)

/** Herramientas del propietario (`025`). */
@Composable
private fun OwnerToolScreen(screen: WorkspaceScreen) {
    when (screen) {
        WorkspaceScreen.OWNER_PRICING -> OwnerVenueTool(VenueTool.PRICING)
        WorkspaceScreen.OWNER_SETUPS -> OwnerVenueTool(VenueTool.SETUPS)
        WorkspaceScreen.OWNER_PAYMENTS -> OwnerInsights(OwnerInsight.PAYMENTS)
        WorkspaceScreen.OWNER_STATS -> OwnerInsights(OwnerInsight.STATS)
        WorkspaceScreen.OWNER_REVIEWS -> OwnerReviews()
        else -> Unit
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
                "studio" to WorkspaceScreen.STUDIO,
                "favorites" to WorkspaceScreen.FAVORITES,
                "messages" to WorkspaceScreen.MESSAGES,
                "chat" to WorkspaceScreen.MESSAGES,
                "budget" to WorkspaceScreen.BUDGET,
                "budgetAI" to WorkspaceScreen.BUDGET_SUGGESTION,
                "payments" to WorkspaceScreen.PAYMENTS,
                "assistant" to WorkspaceScreen.ASSISTANT,
            ) + EVENT_ROUTES.associateWith { WorkspaceScreen.EVENTS },
        DemoRole.OWNER to
            OWNER_VENUE_ROUTES.associateWith { WorkspaceScreen.OWNER_VENUES } +
            ("ownerRequests" to WorkspaceScreen.OWNER_REQUESTS) +
            ("ownerHome" to WorkspaceScreen.OWNER_HOME) +
            ("ownerCalendar" to WorkspaceScreen.OWNER_CALENDAR) +
            ("messages" to WorkspaceScreen.OWNER_MESSAGES) +
            ("ownerPricing" to WorkspaceScreen.OWNER_PRICING) +
            ("ownerSetups" to WorkspaceScreen.OWNER_SETUPS) +
            ("ownerPayments" to WorkspaceScreen.OWNER_PAYMENTS) +
            ("ownerStats" to WorkspaceScreen.OWNER_STATS) +
            ("ownerReviews" to WorkspaceScreen.OWNER_REVIEWS),
    )

private fun workspaceScreen(
    role: DemoRole,
    route: String,
): WorkspaceScreen =
    CONNECTED_SCREENS[role]?.get(route)
        ?: if (route == "profile") WorkspaceScreen.ACCOUNT else WorkspaceScreen.SECTION
