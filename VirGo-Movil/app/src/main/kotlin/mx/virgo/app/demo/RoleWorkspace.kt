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
import mx.virgo.app.ui.beta.BetaCatalogScreen
import mx.virgo.app.ui.beta.BetaSectionScreen
import mx.virgo.app.ui.beta.BetaSectionsMenu
import mx.virgo.app.ui.beta.betaTitle
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
) {
    key(accountKey, role) {
        BetaWorkspace(role, accountName, accountEmail, onLogout, accountScreen)
    }
}

@Composable
private fun BetaWorkspace(
    role: DemoRole,
    accountName: String,
    accountEmail: String,
    onLogout: () -> Unit,
    accountScreen: @Composable (SessionIdentity, () -> Unit) -> Unit,
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
            // El catálogo tiene su propio encabezado (035 RF-08); el resto usa el del espacio.
            if (sections || !(role == DemoRole.CLIENT && route == "explore")) {
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
            when {
                sections -> BetaSectionsMenu(role, navigate)
                role == DemoRole.CLIENT && route == "explore" -> BetaCatalogScreen()
                route == "profile" -> accountScreen(SessionIdentity(accountName, accountEmail, role.label), onLogout)
                else -> BetaSectionScreen(role, route, accountName, accountEmail)
            }
        }
    }
}
