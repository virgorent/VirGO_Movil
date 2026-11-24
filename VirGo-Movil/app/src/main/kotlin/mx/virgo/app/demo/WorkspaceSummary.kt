package mx.virgo.app.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

@Composable
internal fun workspaceState(): WorkspaceUiState? =
    LocalWorkspace.current
        ?.uiState
        ?.collectAsState()
        ?.value

@Composable
internal fun workspaceRecord(section: WorkspaceSection): WorkspaceRecord? {
    val workspace = LocalWorkspace.current ?: return null
    val state by workspace.uiState.collectAsState()
    return state.latest(section)
}

@Composable
internal fun WorkspaceSummary(screen: String) {
    val workspace = LocalWorkspace.current ?: return
    val state by workspace.uiState.collectAsState()
    val sections = workspaceScreenSections[screen].orEmpty()
    val records = sections.flatMap { state.records[it].orEmpty() }
    if (records.isNotEmpty()) {
        DemoNotice(stringResource(R.string.workspace_local_changes))
        records.forEach { record -> DemoCard(record.name, record.summary) }
    }
}

private val workspaceScreenSections =
    mapOf(
        "explore" to listOf(WorkspaceSection.FILTERS),
        "venue" to listOf(WorkspaceSection.VENUE_DATE),
        "studio" to listOf(WorkspaceSection.BUDGET),
        "budget" to listOf(WorkspaceSection.BUDGET, WorkspaceSection.EVENT),
        "event" to listOf(WorkspaceSection.EVENT, WorkspaceSection.TEAM),
        "profile" to listOf(WorkspaceSection.PROFILE, WorkspaceSection.PREFERENCES, WorkspaceSection.METHOD),
        "payment" to listOf(WorkspaceSection.METHOD),
        "payments" to listOf(WorkspaceSection.PAYMENTS),
        "guests" to listOf(WorkspaceSection.GUESTS),
        "tasks" to listOf(WorkspaceSection.TASKS),
        "reminders" to listOf(WorkspaceSection.REMINDERS),
        "share" to listOf(WorkspaceSection.COLLABORATORS),
        "ownerCalendar" to listOf(WorkspaceSection.BLOCKS),
        "providerAgenda" to listOf(WorkspaceSection.AGENDA),
    )
