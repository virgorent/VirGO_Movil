package mx.virgo.app.demo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.app.ui.explore.ExploreScreen

/** The authenticated role comes from the validated session, never from a role picker. */
@Composable
fun RoleWorkspace(
    role: DemoRole,
    accountKey: String,
    onLogout: () -> Unit,
    accountName: String = "",
    accountEmail: String = "",
) {
    key(accountKey, role) {
        val workspace = rememberWorkspaceViewModel("$accountKey:${role.name}")
        LaunchedEffect(workspace, accountName, accountEmail) {
            workspace.initializeProfile(accountName, accountEmail)
        }
        DisposableEffect(workspace) {
            onDispose { workspace.clear() }
        }
        CompositionLocalProvider(LocalWorkspace provides workspace) {
            AuthenticatedWorkspace(role) {
                workspace.clear()
                onLogout()
            }
        }
    }
}

@Composable
private fun AuthenticatedWorkspace(
    role: DemoRole,
    onLogout: () -> Unit,
) {
    var history by rememberSaveable(role) { mutableStateOf(role.home) }
    var index by rememberSaveable { mutableStateOf(false) }
    val route = history.substringAfterLast("|")
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notice: (String) -> Unit = { text -> scope.launch { snackbar.showSnackbar(text) } }
    val navigate: (String) -> Unit = {
        check(DemoDestinations.allows(role, it))
        history = pushDemoRoute(history, it)
        index = false
    }
    val back: () -> Unit = {
        if (index) index = false else history = history.substringBeforeLast("|", role.home)
    }
    BackHandler(enabled = index || history.contains("|"), onBack = back)
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            WorkspaceToolbar(role, route, back, onLogout, notice)
        },
        bottomBar = {
            DemoBottomNavigation(role, route, navigate, onIndex = { index = !index }, systemInsets = false)
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Surface(Modifier.padding(padding)) {
            CompositionLocalProvider(LocalSnackbarHost provides snackbar) {
                WorkspaceContent(role, route, index, navigate, notice)
            }
        }
    }
}

@Composable
private fun WorkspaceToolbar(
    role: DemoRole,
    route: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNotice: (String) -> Unit,
) {
    Column {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                stringResource(R.string.unified_workspace_notice),
                Modifier.fillMaxWidth().padding(DemoMetrics.compactSpacing),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState(),
                ).padding(horizontal = DemoMetrics.rowSpacing),
            horizontalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
        ) {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = DemoMetrics.touchTarget)) {
                Text(stringResource(R.string.demo_back))
            }
            TextButton(onClick = onLogout, modifier = Modifier.heightIn(min = DemoMetrics.touchTarget)) {
                Text(stringResource(R.string.unified_logout))
            }
        }
        if (route != "explore") DemoSheetActions(role, route, onNotice)
    }
}

@Composable
private fun WorkspaceContent(
    role: DemoRole,
    route: String,
    index: Boolean,
    navigate: (String) -> Unit,
    notice: (String) -> Unit,
) {
    when {
        index -> DemoScreenIndex(role, navigate)
        role == DemoRole.CLIENT && route == "explore" ->
            Column {
                TextButton(onClick = { navigate("venue") }) {
                    Text(stringResource(R.string.unified_sample_detail))
                }
                ExploreScreen(onNavigate = navigate)
            }
        else -> DemoRoleContent(role, route, navigate, notice)
    }
}
