package mx.virgo.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.EventPlanningRepository
import mx.virgo.core.domain.EventTask
import mx.virgo.core.domain.TaskField
import mx.virgo.core.domain.TaskGroups
import mx.virgo.core.domain.TaskInput
import mx.virgo.core.domain.TaskValidator
import java.time.LocalDate
import javax.inject.Inject

/** Callbacks de C27, agrupados para probar el contenido sin `ViewModel`. */
data class TasksActions(
    val onAdd: () -> Unit = {},
    val onEdit: (EventTask) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onEditor: (TaskEditor) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggle: (EventTask) -> Unit = {},
    val onDelete: (EventTask?) -> Unit = {},
    val onConfirmDelete: (EventTask) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/** Pendiente en captura; `taskId` nulo es uno nuevo (017 RF-03). */
data class TaskEditor(
    val taskId: String? = null,
    val title: String = "",
    val dueDate: LocalDate? = null,
    val done: Boolean = false,
    val errors: Set<TaskField> = emptySet(),
)

data class TasksUiState(
    val eventId: String? = null,
    val loading: Boolean = true,
    val tasks: List<EventTask> = emptyList(),
    val failed: Boolean = false,
    val editor: TaskEditor? = null,
    val saving: Boolean = false,
    val error: EventError? = null,
    val deleting: EventTask? = null,
) {
    fun groups(today: LocalDate): TaskGroups = TaskGroups.of(tasks, today)
}

@HiltViewModel
class TasksViewModel
    @Inject
    constructor(
        private val repository: EventPlanningRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(TasksUiState())
        val state: StateFlow<TasksUiState> = mutableState.asStateFlow()

        fun start(eventId: String) {
            if (mutableState.value.eventId == eventId) return
            mutableState.value = TasksUiState(eventId = eventId)
            refresh()
        }

        fun refresh() {
            val eventId = mutableState.value.eventId ?: return
            mutableState.value = mutableState.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = repository.tasks(eventId)) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(loading = false, tasks = result.value)
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value = mutableState.value.copy(loading = false, failed = true)
                        }
                }
            }
        }

        fun edit(
            task: EventTask?,
            close: Boolean = false,
        ) {
            val editor =
                if (close) {
                    null
                } else {
                    task?.let { TaskEditor(it.id, it.title, it.dueDate, it.done) }
                        ?: TaskEditor()
                }
            mutableState.value = mutableState.value.copy(editor = editor, error = null)
        }

        fun updateEditor(editor: TaskEditor) {
            mutableState.value = mutableState.value.copy(editor = editor.copy(errors = emptySet()), error = null)
        }

        /** Marcar o desmarcar: `PATCH` con todos los campos (017 RF-02). */
        fun toggle(task: EventTask) {
            val dueDate = task.dueDate ?: return
            persist(task.id, TaskInput(task.title, dueDate, !task.done), closeEditor = false)
        }

        fun save() {
            val current = mutableState.value
            val editor = current.editor ?: return
            if (current.saving) return
            val (input, errors) = TaskValidator.validate(editor.title, editor.dueDate, editor.done)
            mutableState.value = current.copy(editor = editor.copy(errors = errors))
            if (input != null) persist(editor.taskId, input, closeEditor = true)
        }

        fun delete(
            task: EventTask?,
            confirm: Boolean = false,
        ) {
            val eventId = mutableState.value.eventId ?: return
            if (!confirm || task == null) {
                mutableState.value = mutableState.value.copy(deleting = task)
                return
            }
            mutableState.value = mutableState.value.copy(deleting = null)
            viewModelScope.launch {
                when (val result = repository.deleteTask(eventId, task.id)) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(tasks = mutableState.value.tasks.filterNot { it.id == task.id })
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value = mutableState.value.copy(error = result.error.toEventError())
                        }
                }
            }
        }

        private fun persist(
            taskId: String?,
            input: TaskInput,
            closeEditor: Boolean,
        ) {
            val eventId = mutableState.value.eventId ?: return
            mutableState.value = mutableState.value.copy(saving = true, error = null)
            viewModelScope.launch {
                when (val result = repository.saveTask(eventId, taskId, input)) {
                    is AppResult.Success -> {
                        val saved = result.value
                        val tasks = mutableState.value.tasks
                        val updated =
                            if (tasks.any { it.id == saved.id }) {
                                tasks.map {
                                    if (it.id ==
                                        saved.id
                                    ) {
                                        saved
                                    } else {
                                        it
                                    }
                                }
                            } else {
                                tasks + saved
                            }
                        mutableState.value =
                            mutableState.value.copy(
                                saving = false,
                                tasks = updated,
                                editor = if (closeEditor) null else mutableState.value.editor,
                            )
                    }
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value =
                                mutableState.value.copy(saving = false, error = result.error.toEventError())
                        }
                }
            }
        }
    }
