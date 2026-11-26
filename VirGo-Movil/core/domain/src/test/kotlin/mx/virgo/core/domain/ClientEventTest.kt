package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ClientEventTest {
    private val today = LocalDate.parse("2026-11-25")
    private val valid =
        EventForm("  Boda de Ana  ", QuoteEventType.WEDDING, today.plusDays(30), "120", "150,000.50", "6")

    @Test
    fun validFormBuildsTheInputAndKeepsTheVenue() {
        val (input, errors) = EventValidator.validate(valid, today, venueId = "v1")
        assertTrue(errors.isEmpty())
        assertEquals("Boda de Ana", input!!.name)
        assertEquals(120, input.guests)
        assertEquals(6, input.hours)
        assertEquals(0, BigDecimal("150000.50").compareTo(input.budget))
        assertEquals("v1", input.venueId)
        assertTrue(EventValidator.validate(valid.copy(budget = "0", date = today), today).second.isEmpty())
    }

    @Test
    fun eachFieldIsChecked() {
        val errors = EventValidator.validate(EventForm(), today).second
        EventField.entries.forEach { assertEquals(EventProblem.REQUIRED, errors[it]) }
        val bad =
            EventValidator
                .validate(
                    valid.copy(
                        name = "a".repeat(101),
                        date = today.minusDays(1),
                        guests = "0",
                        hours = "x",
                        budget = "-1",
                    ),
                    today,
                ).second
        assertEquals(EventProblem.TOO_LONG, bad[EventField.NAME])
        assertEquals(EventProblem.PAST_DATE, bad[EventField.DATE])
        assertEquals(EventProblem.INVALID, bad[EventField.GUESTS])
        assertEquals(EventProblem.INVALID, bad[EventField.HOURS])
        assertEquals(EventProblem.INVALID, bad[EventField.BUDGET])
        assertNull(EventValidator.validate(valid.copy(budget = "1.234"), today).first)
    }

    @Test
    fun aPastDateThatDidNotChangeCanBeKeptWhenEditing() {
        val past = today.minusDays(3)
        assertTrue(EventValidator.validate(valid.copy(date = past), today, originalDate = past).second.isEmpty())
        assertEquals(
            EventProblem.PAST_DATE,
            EventValidator
                .validate(
                    valid.copy(date = past.minusDays(1)),
                    today,
                    originalDate = past,
                ).second[EventField.DATE],
        )
    }

    @Test
    fun countdownAndFieldNames() {
        val event = ClientEvent("e", "x", null, today.plusDays(3), 1, BigDecimal.ONE, 1, null, null)
        assertEquals(3L, event.daysUntil(today))
        assertEquals(-1L, event.copy(date = today.minusDays(1)).daysUntil(today))
        assertNull(event.copy(date = null).daysUntil(today))
        assertEquals(EventField.TYPE, EventField.fromWire("eventType"))
        assertEquals(EventField.BUDGET, EventField.fromWire("budget"))
        assertEquals(EventField.NAME, EventField.fromWire("name"))
        assertEquals(EventField.DATE, EventField.fromWire("date"))
        assertEquals(EventField.GUESTS, EventField.fromWire("guests"))
        assertEquals(EventField.HOURS, EventField.fromWire("hours"))
        assertNull(EventField.fromWire("venueId"))
    }

    @Test
    fun guestsValidationStatusesAndTotals() {
        val (input, errors) = GuestValidator.validate(" Ana ", " 442 123 ", "", GuestStatus.CONFIRMED, "  ")
        assertTrue(errors.isEmpty())
        assertEquals(GuestInput("Ana", "442 123", 0, GuestStatus.CONFIRMED, null), input)
        val bad = GuestValidator.validate("", "1".repeat(17), "-1", GuestStatus.PENDING, "t".repeat(21)).second
        assertEquals(GuestField.entries.toSet(), bad)
        assertTrue(GuestField.COMPANIONS in GuestValidator.validate("a", "1", "x", GuestStatus.PENDING, "").second)
        assertEquals(GuestStatus.DECLINED, GuestStatus.fromWire("declined"))
        assertEquals(GuestStatus.UNKNOWN, GuestStatus.fromWire("maybe"))
        assertEquals(GuestStatus.UNKNOWN, GuestStatus.fromWire(""))
        val guests =
            listOf(
                EventGuest("1", "a", "1", 2, GuestStatus.CONFIRMED, null),
                EventGuest("2", "b", "1", 0, GuestStatus.PENDING, null),
                EventGuest("3", "c", "1", 1, GuestStatus.DECLINED, null),
                EventGuest("4", "d", "1", 0, GuestStatus.UNKNOWN, null),
            )
        assertEquals(GuestTotals(3, 1, 2), GuestTotals.of(guests))
    }

    @Test
    fun tasksAreValidatedAndGrouped() {
        assertEquals(TaskInput("Flores", today, false), TaskValidator.validate(" Flores ", today, false).first)
        assertEquals(setOf(TaskField.TITLE, TaskField.DUE_DATE), TaskValidator.validate(" ", null, false).second)
        assertTrue(TaskField.TITLE in TaskValidator.validate("a".repeat(151), today, false).second)
        val tasks =
            listOf(
                EventTask("late", "a", today.minusDays(1), false),
                EventTask("soon2", "b", today.plusDays(9), false),
                EventTask("soon1", "c", today, false),
                EventTask("none", "d", null, false),
                EventTask("done", "e", today.minusDays(5), true),
            )
        val groups = TaskGroups.of(tasks, today)
        assertEquals(listOf("late"), groups.overdue.map { it.id })
        assertEquals(listOf("soon1", "soon2", "none"), groups.upcoming.map { it.id })
        assertEquals(listOf("done"), groups.done.map { it.id })
    }
}
