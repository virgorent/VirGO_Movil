package mx.virgo.app.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Scoped by the caller to one account AND role. Never persists or calls a backend. */
class WorkspaceViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = mutableState.asStateFlow()

    fun initializeProfile(
        name: String,
        email: String,
    ) {
        mutableState.update { state ->
            if (state.accountProfile == null) {
                state.copy(accountProfile = WorkspaceProfile(name, email))
            } else {
                state
            }
        }
    }

    fun clear() {
        mutableState.value = WorkspaceUiState()
    }

    fun confirm(
        section: WorkspaceSection,
        record: WorkspaceRecord,
    ): Boolean {
        if (!record.isValid()) return false
        mutableState.update { state ->
            val previous = if (section in singleRecordSections) emptyList() else state.records[section].orEmpty()
            state.copy(records = state.records + (section to (previous + record.copy(name = record.name.trim()))))
        }
        return true
    }

    fun toggleFavorite() {
        mutableState.update { it.copy(favorite = !it.favorite) }
    }

    fun markExamplePaid() {
        mutableState.update { it.copy(paid = true) }
    }
}

/** Null preserves isolated legacy previews/tests; the real workspace supplies an instance. */
val LocalWorkspace = staticCompositionLocalOf<WorkspaceViewModel?> { null }

/** Include the role in accountKey. Removing the workspace discards all local records. */
@Composable
fun rememberWorkspaceViewModel(accountKey: String): WorkspaceViewModel = remember(accountKey) { WorkspaceViewModel() }

private val singleRecordSections =
    setOf(
        WorkspaceSection.FILTERS,
        WorkspaceSection.VENUE_DATE,
        WorkspaceSection.BUDGET,
        WorkspaceSection.EVENT,
        WorkspaceSection.PROFILE,
        WorkspaceSection.PREFERENCES,
        WorkspaceSection.METHOD,
    )
