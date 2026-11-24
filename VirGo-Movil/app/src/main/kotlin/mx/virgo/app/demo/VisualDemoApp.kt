package mx.virgo.app.demo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import mx.virgo.app.R

@Composable
fun VisualDemoApp(onExit: () -> Unit) {
    val workspace = rememberWorkspaceViewModel("preview")
    DisposableEffect(workspace) {
        onDispose { workspace.clear() }
    }
    CompositionLocalProvider(LocalWorkspace provides workspace) {
        PreviewContent(onExit)
    }
}

@Composable
private fun PreviewContent(onExit: () -> Unit) {
    val workspace = LocalWorkspace.current
    var roleName by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf("") }
    var showIndex by rememberSaveable { mutableStateOf(false) }
    val role = DemoRole.entries.firstOrNull { it.name == roleName }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notice: (String) -> Unit = { text -> scope.launch { snackbar.showSnackbar(text) } }
    val navigate: (String) -> Unit = { target ->
        check(role != null && DemoDestinations.allows(role, target))
        history = pushDemoRoute(history, target)
        showIndex = false
    }
    val back: () -> Unit = {
        when {
            showIndex -> showIndex = false
            history.contains("|") -> history = history.substringBeforeLast("|")
            else -> {
                workspace?.clear()
                roleName = ""
            }
        }
    }
    BackHandler(enabled = role != null, onBack = back)
    Scaffold(
        topBar = {
            PreviewTopBar(role, showIndex, history, notice, back, onChooseRole = {
                workspace?.clear()
                roleName = ""
            }, onExit = onExit)
        },
        bottomBar = {
            if (role != null) {
                DemoBottomNavigation(
                    role,
                    history.substringAfterLast("|"),
                    navigate,
                    onIndex = { showIndex = !showIndex },
                    systemInsets = false,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            PreviewBody(role, history, showIndex, workspace, navigate, notice) { chosen ->
                roleName = chosen.name
                history = chosen.home
                showIndex = false
            }
        }
    }
}

@Composable
private fun PreviewTopBar(
    role: DemoRole?,
    showIndex: Boolean,
    history: String,
    notice: (String) -> Unit,
    onBack: () -> Unit,
    onChooseRole: () -> Unit,
    onExit: () -> Unit,
) {
    Column {
        DemoToolbar(role, onBack = onBack, onChooseRole = onChooseRole, onExit = onExit)
        if (role != null && !showIndex) {
            DemoSheetActions(role, history.substringAfterLast("|"), notice)
        }
    }
}

@Composable
private fun PreviewBody(
    role: DemoRole?,
    history: String,
    showIndex: Boolean,
    workspace: WorkspaceViewModel?,
    navigate: (String) -> Unit,
    notice: (String) -> Unit,
    onChoose: (DemoRole) -> Unit,
) {
    when {
        role == null ->
            DemoRoleChooser { chosen ->
                workspace?.clear()
                onChoose(chosen)
            }
        showIndex -> DemoScreenIndex(role, navigate)
        else -> DemoRoleContent(role, history.substringAfterLast("|"), navigate, notice)
    }
}

internal fun pushDemoRoute(
    history: String,
    target: String,
): String {
    if (history.substringAfterLast("|") == target) return history
    return (history.split("|") + target).takeLast(DEMO_HISTORY_LIMIT).joinToString("|")
}

private const val DEMO_HISTORY_LIMIT = 50

@Composable
private fun DemoToolbar(
    role: DemoRole?,
    onBack: () -> Unit,
    onChooseRole: () -> Unit,
    onExit: () -> Unit,
) {
    Column {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                stringResource(R.string.demo_banner),
                modifier =
                    Modifier.fillMaxWidth().padding(
                        horizontal = DemoMetrics.spacing,
                        vertical = DemoMetrics.compactSpacing,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DemoMetrics.toolbarSpacing),
        ) {
            if (role != null) {
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = DemoMetrics.touchTarget)) {
                    Text(stringResource(R.string.demo_back))
                }
                TextButton(onClick = onChooseRole, modifier = Modifier.heightIn(min = DemoMetrics.touchTarget)) {
                    Text(stringResource(R.string.demo_role_switch))
                }
            }
            TextButton(onClick = onExit, modifier = Modifier.heightIn(min = DemoMetrics.touchTarget)) {
                Text(stringResource(R.string.demo_exit))
            }
        }
    }
}

@Composable
private fun DemoRoleChooser(onChoose: (DemoRole) -> Unit) {
    DemoPage(
        title = stringResource(R.string.demo_brand),
        subtitle = stringResource(R.string.demo_welcome),
    ) {
        Text(stringResource(R.string.demo_welcome_body), style = MaterialTheme.typography.bodyLarge)
        val descriptions =
            listOf(
                R.string.demo_role_client_description,
                R.string.demo_role_owner_description,
                R.string.demo_role_provider_description,
            )
        DemoRole.entries.forEachIndexed { index, role ->
            DemoCard(
                title = stringResource(role.label),
                subtitle = stringResource(descriptions[index]),
                onClick = { onChoose(role) },
            )
        }
        DemoNotice(stringResource(R.string.demo_help_real_answer))
    }
}

@Composable
internal fun DemoScreenIndex(
    role: DemoRole,
    onNavigate: (String) -> Unit,
) {
    DemoPage(
        title = stringResource(role.label),
        subtitle = stringResource(R.string.demo_index_body),
    ) {
        DemoDestinations.forRole(role).forEach { destination ->
            DemoCard(
                title = stringResource(destination.title),
                onClick = { onNavigate(destination.route) },
            )
        }
    }
}

@Composable
internal fun DemoRoleContent(
    role: DemoRole,
    route: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    check(DemoDestinations.allows(role, route))
    if (DemoDestinations.shared.any { it.route == route }) {
        SharedDemoScreen(role, route, onNavigate, onNotice)
    } else {
        when (role) {
            DemoRole.CLIENT -> ClientDemoScreen(route, onNavigate, onNotice)
            DemoRole.OWNER -> OwnerDemoScreen(route, onNavigate, onNotice)
            DemoRole.PROVIDER -> ProviderDemoScreen(route, onNavigate, onNotice)
        }
    }
}
