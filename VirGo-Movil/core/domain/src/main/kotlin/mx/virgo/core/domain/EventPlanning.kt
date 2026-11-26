package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.time.LocalDate

/** Estado de confirmación con los valores del contrato (016 RF-02). */
enum class GuestStatus(
    val wire: String,
) {
    CONFIRMED("confirmed"),
    PENDING("pending"),
    DECLINED("declined"),
    UNKNOWN(""),
    ;

    companion object {
        fun fromWire(value: String?): GuestStatus = entries.firstOrNull { it != UNKNOWN && it.wire == value } ?: UNKNOWN

        /** Los que se pueden elegir al capturar. */
        val selectable: List<GuestStatus> = listOf(CONFIRMED, PENDING, DECLINED)
    }
}

data class EventGuest(
    val id: String,
    val name: String,
    val phone: String,
    val companions: Int,
    val status: GuestStatus,
    val table: String?,
) {
    /** El invitado más sus acompañantes. */
    val people: Int get() = 1 + companions
}

/** Totales por estado, en personas (016 RF-03). */
data class GuestTotals(
    val confirmed: Int,
    val pending: Int,
    val declined: Int,
) {
    companion object {
        fun of(guests: List<EventGuest>): GuestTotals =
            GuestTotals(
                confirmed = guests.filter { it.status == GuestStatus.CONFIRMED }.sumOf { it.people },
                pending = guests.filter { it.status == GuestStatus.PENDING }.sumOf { it.people },
                declined = guests.filter { it.status == GuestStatus.DECLINED }.sumOf { it.people },
            )
    }
}

data class GuestInput(
    val name: String,
    val phone: String,
    val companions: Int,
    val status: GuestStatus,
    val table: String?,
)

enum class GuestField {
    NAME,
    PHONE,
    COMPANIONS,
    TABLE,
}

object GuestValidator {
    const val MAX_NAME = 100
    const val MAX_PHONE = 16
    const val MAX_TABLE = 20

    fun validate(
        name: String,
        phone: String,
        companions: String,
        status: GuestStatus,
        table: String,
    ): Pair<GuestInput?, Set<GuestField>> {
        val errors = mutableSetOf<GuestField>()
        if (name.isBlank() || name.trim().length > MAX_NAME) errors += GuestField.NAME
        if (phone.isBlank() || phone.trim().length > MAX_PHONE) errors += GuestField.PHONE
        val count = if (companions.isBlank()) 0 else companions.trim().toIntOrNull()
        if (count == null || count < 0) errors += GuestField.COMPANIONS
        if (table.trim().length > MAX_TABLE) errors += GuestField.TABLE
        if (errors.isNotEmpty() || count == null) return null to errors
        val input = GuestInput(name.trim(), phone.trim(), count, status, table.trim().ifEmpty { null })
        return input to errors
    }
}

data class EventTask(
    val id: String,
    val title: String,
    val dueDate: LocalDate?,
    val done: Boolean,
)

/** Grupos de C27 (017 RF-01). */
data class TaskGroups(
    val overdue: List<EventTask>,
    val upcoming: List<EventTask>,
    val done: List<EventTask>,
) {
    companion object {
        fun of(
            tasks: List<EventTask>,
            today: LocalDate,
        ): TaskGroups {
            val sorted = tasks.sortedWith(compareBy(nullsLast()) { it.dueDate })
            val pending = sorted.filter { !it.done }
            return TaskGroups(
                overdue = pending.filter { it.dueDate?.isBefore(today) == true },
                upcoming = pending.filter { it.dueDate?.isBefore(today) != true },
                done = sorted.filter { it.done },
            )
        }
    }
}

data class TaskInput(
    val title: String,
    val dueDate: LocalDate,
    val done: Boolean,
)

enum class TaskField {
    TITLE,
    DUE_DATE,
}

object TaskValidator {
    const val MAX_TITLE = 150

    fun validate(
        title: String,
        dueDate: LocalDate?,
        done: Boolean,
    ): Pair<TaskInput?, Set<TaskField>> {
        val errors = mutableSetOf<TaskField>()
        if (title.isBlank() || title.trim().length > MAX_TITLE) errors += TaskField.TITLE
        if (dueDate == null) errors += TaskField.DUE_DATE
        if (errors.isNotEmpty() || dueDate == null) return null to errors
        return TaskInput(title.trim(), dueDate, done) to errors
    }
}

/** Invitados y pendientes de un evento (016, 017). */
interface EventPlanningRepository {
    suspend fun guests(eventId: String): AppResult<List<EventGuest>>

    suspend fun saveGuest(
        eventId: String,
        guestId: String?,
        input: GuestInput,
    ): AppResult<EventGuest>

    suspend fun deleteGuest(
        eventId: String,
        guestId: String,
    ): AppResult<Unit>

    suspend fun tasks(eventId: String): AppResult<List<EventTask>>

    suspend fun saveTask(
        eventId: String,
        taskId: String?,
        input: TaskInput,
    ): AppResult<EventTask>

    suspend fun deleteTask(
        eventId: String,
        taskId: String,
    ): AppResult<Unit>
}
