package mx.virgo.app.ui.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.EventDateField
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.EventTask
import mx.virgo.core.domain.TaskField
import mx.virgo.core.domain.TaskValidator
import java.time.LocalDate

/** C27: pendientes del evento agrupados (017). */
@Composable
fun TasksScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    LaunchedEffect(eventId) { viewModel.start(eventId) }
    val state by viewModel.state.collectAsState()
    TasksContent(
        state,
        LocalDate.now(),
        TasksActions(
            onAdd = { viewModel.edit(null) },
            onEdit = { viewModel.edit(it) },
            onCancel = { viewModel.edit(null, close = true) },
            onEditor = viewModel::updateEditor,
            onSave = viewModel::save,
            onToggle = viewModel::toggle,
            onDelete = { viewModel.delete(it) },
            onConfirmDelete = { viewModel.delete(it, confirm = true) },
            onRetry = viewModel::refresh,
            onBack = onBack,
        ),
    )
}

@Composable
internal fun TasksContent(
    state: TasksUiState,
    today: LocalDate,
    actions: TasksActions,
) {
    VirGoPageBody {
        Text(
            stringResource(R.string.event_action_tasks),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        val editor = state.editor
        when {
            editor != null -> TaskForm(editor, state.saving, actions)
            state.loading && state.tasks.isEmpty() -> LoadingState(message = stringResource(R.string.tasks_loading))
            state.failed ->
                ErrorBanner(
                    message = stringResource(R.string.tasks_error),
                    actionLabel = stringResource(R.string.events_retry),
                    onAction = actions.onRetry,
                )
            else -> TaskGroupsList(state, today, actions)
        }
        state.error?.let { ErrorBanner(message = stringResource(eventErrorText(it))) }
        SecondaryAction(stringResource(R.string.event_back), actions.onBack)
    }
    state.deleting?.let { task ->
        ConfirmDelete(
            title = stringResource(R.string.tasks_delete_title, task.title),
            onConfirm = { actions.onConfirmDelete(task) },
            onDismiss = { actions.onDelete(null) },
        )
    }
}

@Composable
private fun TaskGroupsList(
    state: TasksUiState,
    today: LocalDate,
    actions: TasksActions,
) {
    VirGoButton(onClick = actions.onAdd, text = stringResource(R.string.tasks_add))
    if (state.tasks.isEmpty()) {
        VirGoCard { EmptyState(title = stringResource(R.string.tasks_empty), iconVector = Icons.Outlined.Checklist) }
        return
    }
    val groups = state.groups(today)
    TaskGroup(R.string.tasks_overdue, groups.overdue, VirGoTone.RED, actions)
    TaskGroup(R.string.tasks_upcoming, groups.upcoming, VirGoTone.VIOLET, actions)
    TaskGroup(R.string.tasks_done, groups.done, VirGoTone.GREEN, actions)
}

@Composable
private fun TaskGroup(
    title: Int,
    tasks: List<EventTask>,
    tone: VirGoTone,
    actions: TasksActions,
) {
    if (tasks.isEmpty()) return
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            tasks.forEachIndexed { index, task ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = task.title,
                    subtitle =
                        task.dueDate?.let { stringResource(R.string.tasks_due, formatDate(it)) }
                            ?: stringResource(R.string.tasks_no_date),
                    icon = if (task.done) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                    tone = tone,
                    standalone = false,
                    onClick = { actions.onEdit(task) },
                    trailing = {
                        // Sin nombre, TalkBack solo diría «casilla»: se nombra con el pendiente.
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { actions.onToggle(task) },
                            modifier = Modifier.semantics { contentDescription = task.title },
                        )
                    },
                    subtitleMaxLines = Int.MAX_VALUE,
                )
                SecondaryAction(stringResource(R.string.guests_delete)) { actions.onDelete(task) }
            }
        }
    }
}

@Composable
private fun TaskForm(
    editor: TaskEditor,
    saving: Boolean,
    actions: TasksActions,
) {
    Text(
        stringResource(if (editor.taskId == null) R.string.tasks_form_new else R.string.tasks_form_edit),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    LabeledInput(
        stringResource(R.string.tasks_form_title),
        editor.title,
        if (TaskField.TITLE in
            editor.errors
        ) {
            stringResource(R.string.tasks_error_title, TaskValidator.MAX_TITLE)
        } else {
            null
        },
        singleLine = false,
    ) { actions.onEditor(editor.copy(title = it)) }
    EventDateField(
        stringResource(R.string.tasks_form_due),
        editor.dueDate,
        if (TaskField.DUE_DATE in editor.errors) stringResource(R.string.tasks_error_due) else null,
    ) { actions.onEditor(editor.copy(dueDate = it)) }
    VirGoButton(
        onClick = actions.onSave,
        enabled = !saving,
        text = stringResource(if (saving) R.string.event_form_saving else R.string.tasks_form_save),
    )
    SecondaryAction(stringResource(R.string.guests_form_cancel), actions.onCancel)
}
