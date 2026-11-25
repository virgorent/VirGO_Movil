package mx.virgo.app.demo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R
import mx.virgo.app.ui.beta.BetaCatalogScreen
import mx.virgo.app.ui.beta.BetaSectionScreen
import mx.virgo.app.ui.beta.BetaSectionsMenu
import mx.virgo.core.designsystem.components.VirGoOutlinedButton

/** The beta uses only the authenticated identity and never grants a selectable role. */
@Composable
fun RoleWorkspace(
    role: DemoRole,
    accountKey: String,
    onLogout: () -> Unit,
    accountName: String = "",
    accountEmail: String = "",
) {
    key(accountKey, role) {
        BetaWorkspace(role, accountName, accountEmail, onLogout)
    }
}

@Composable
private fun BetaWorkspace(
    role: DemoRole,
    accountName: String,
    accountEmail: String,
    onLogout: () -> Unit,
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
            Column(
                Modifier.fillMaxWidth().padding(horizontal = DemoMetrics.pagePadding),
                verticalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
            ) {
                Text(
                    stringResource(R.string.beta_label),
                    Modifier.padding(top = DemoMetrics.spacing),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (sections || history.contains("|")) {
                    VirGoOutlinedButton(onClick = back) {
                        Text(stringResource(R.string.demo_back))
                    }
                }
                VirGoOutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.beta_local_logout))
                }
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
        Surface(Modifier.padding(padding)) {
            when {
                sections -> BetaSectionsMenu(role, navigate)
                role == DemoRole.CLIENT && route == "explore" -> BetaCatalogScreen()
                else -> BetaSectionScreen(role, route, accountName, accountEmail)
            }
        }
    }
}
